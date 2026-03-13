package sk.emaa.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sk.emaa.dto.StudentDto;
import sk.emaa.dto.StudentMartialArtDto;
import sk.emaa.model.entity.tables.CreditTransaction;
import sk.emaa.model.entity.tables.MartialArt;
import sk.emaa.model.entity.tables.Student;
import sk.emaa.model.entity.tables.StudentMartialArt;
import sk.emaa.model.entity.tables.records.CreditTransactionRecord;
import sk.emaa.model.entity.tables.records.StudentRecord;
import sk.emaa.util.AppConstants;

@Service
@RequiredArgsConstructor
public class StudentService {
	
    private DateTimeFormatter birthdateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
	
    private final DSLContext dsl;
	
    public List<StudentDto> loadStudents(int schoolId) {
        List<StudentRecord> students = dsl.selectFrom(Student.STUDENT)
                .where(Student.STUDENT.SCHOOL_ID.eq(schoolId))
                .orderBy(Student.STUDENT.CREATED_AT.asc())
                .fetch();
		
        return students.stream()
            .map(this::mapToDto)
            .toList();
    }

    public void createStudent(StudentDto student) {
        StudentRecord studentRecord = mapToRecord(student);
        studentRecord.changed(Student.STUDENT.ID, false);

        dsl.transaction(configuration -> {
            DSLContext ctx = DSL.using(configuration);

            ctx.insertInto(Student.STUDENT)
               .set(studentRecord)
               .execute();

            Integer studentId = ctx.select(DSL.max(Student.STUDENT.ID))
                                   .from(Student.STUDENT)
                                   .fetchOne(0, Integer.class);

            saveStudentMartialArts(ctx, studentId, student);
        });
    }

    public void updateStudent(StudentDto student) {
        StudentRecord studentRecord = mapToRecord(student);
        dsl.transaction(configuration -> {
            DSLContext ctx = DSL.using(configuration);

            ctx.update(Student.STUDENT)
               .set(studentRecord)
               .where(Student.STUDENT.ID.eq(student.id()))
               .execute();

            saveStudentMartialArts(ctx, student.id(), student);
        });
    }

    private void saveStudentMartialArts(DSLContext ctx, int studentId, StudentDto dto) {
        if (dto.martialArts() == null) return;

        for (StudentMartialArtDto sma : dto.martialArts()) {
            if (sma.martialArtId() == null) continue;

            boolean exists = ctx.fetchExists(
                ctx.selectOne()
                   .from(StudentMartialArt.STUDENT_MARTIAL_ART)
                   .where(StudentMartialArt.STUDENT_MARTIAL_ART.STUDENT_ID.eq(studentId))
                   .and(StudentMartialArt.STUDENT_MARTIAL_ART.MARTIAL_ART_ID.eq(sma.martialArtId()))
            );

            if (exists) {
                ctx.update(StudentMartialArt.STUDENT_MARTIAL_ART)
                   .set(StudentMartialArt.STUDENT_MARTIAL_ART.ACTIVE, sma.active())
                   .set(StudentMartialArt.STUDENT_MARTIAL_ART.GRADE, sma.grade())
                   .set(StudentMartialArt.STUDENT_MARTIAL_ART.BASE_PAYMENT_AMOUNT, sma.basePaymentAmount())
                   .set(StudentMartialArt.STUDENT_MARTIAL_ART.PAYMENT_TYPE, sma.paymentType())
                   .where(StudentMartialArt.STUDENT_MARTIAL_ART.STUDENT_ID.eq(studentId))
                   .and(StudentMartialArt.STUDENT_MARTIAL_ART.MARTIAL_ART_ID.eq(sma.martialArtId()))
                   .execute();
            } else {
                ctx.insertInto(StudentMartialArt.STUDENT_MARTIAL_ART)
                   .set(StudentMartialArt.STUDENT_MARTIAL_ART.STUDENT_ID, studentId)
                   .set(StudentMartialArt.STUDENT_MARTIAL_ART.MARTIAL_ART_ID, sma.martialArtId())
                   .set(StudentMartialArt.STUDENT_MARTIAL_ART.ACTIVE, sma.active())
                   .set(StudentMartialArt.STUDENT_MARTIAL_ART.GRADE, sma.grade())
                   .set(StudentMartialArt.STUDENT_MARTIAL_ART.BASE_PAYMENT_AMOUNT, sma.basePaymentAmount())
                   .set(StudentMartialArt.STUDENT_MARTIAL_ART.PAYMENT_TYPE, sma.paymentType())
                   .set(StudentMartialArt.STUDENT_MARTIAL_ART.CREDIT, sma.credit() != null ? sma.credit() : 0)
                   .execute();
            }
        }
    }
    
    public StudentDto getStudent(int id) {
        StudentRecord student = dsl.selectFrom(Student.STUDENT)
                .where(Student.STUDENT.ID.eq(id))
                .fetchOne();
        return student != null ? mapToDto(student) : null;
    }
	
    public StudentDto addCredit(int studentId, int martialArtId, int amountToAdd, int basePaymentAmount) {
        return dsl.<StudentDto>transactionResult(configuration -> {
            DSLContext ctx = DSL.using(configuration);

            // 1. Zvýš kredit v student_martial_art
            ctx.update(StudentMartialArt.STUDENT_MARTIAL_ART)
               .set(StudentMartialArt.STUDENT_MARTIAL_ART.CREDIT,
                    StudentMartialArt.STUDENT_MARTIAL_ART.CREDIT.plus(amountToAdd))
               .where(StudentMartialArt.STUDENT_MARTIAL_ART.STUDENT_ID.eq(studentId))
               .and(StudentMartialArt.STUDENT_MARTIAL_ART.MARTIAL_ART_ID.eq(martialArtId))
               .execute();

            // 2. Záznam o dobití do CREDIT_TRANSACTION
            CreditTransactionRecord tx = ctx.newRecord(CreditTransaction.CREDIT_TRANSACTION);
            tx.setStudentId(studentId);
            tx.setMartialArtId(martialArtId);
            tx.setAmount(amountToAdd * basePaymentAmount);
            tx.setDescription("DOBITIE KREDITU");
            tx.setPaymentType(AppConstants.paymentType_credit);
            tx.store();

            // 3. Načítaj aktualizovaného študenta
            StudentRecord updated = ctx.selectFrom(Student.STUDENT)
                    .where(Student.STUDENT.ID.eq(studentId))
                    .fetchOne();

            return updated != null ? mapToDto(updated) : null;
        });
    }
	
    private StudentDto mapToDto(StudentRecord record) {
        // Načítaj všetky bojové umenia pre študenta vrátane per-BU údajov
        List<StudentMartialArtDto> martialArts = dsl
            .select(
                MartialArt.MARTIAL_ART.ID,
                MartialArt.MARTIAL_ART.CODE,
                MartialArt.MARTIAL_ART.NAME,
                StudentMartialArt.STUDENT_MARTIAL_ART.ACTIVE,
                StudentMartialArt.STUDENT_MARTIAL_ART.GRADE,
                StudentMartialArt.STUDENT_MARTIAL_ART.BASE_PAYMENT_AMOUNT,
                StudentMartialArt.STUDENT_MARTIAL_ART.PAYMENT_TYPE,
                StudentMartialArt.STUDENT_MARTIAL_ART.CREDIT
            )
            .from(StudentMartialArt.STUDENT_MARTIAL_ART)
            .join(MartialArt.MARTIAL_ART)
                .on(StudentMartialArt.STUDENT_MARTIAL_ART.MARTIAL_ART_ID.eq(MartialArt.MARTIAL_ART.ID))
            .where(StudentMartialArt.STUDENT_MARTIAL_ART.STUDENT_ID.eq(record.getId()))
            .fetch(r -> new StudentMartialArtDto(
                r.get(MartialArt.MARTIAL_ART.ID),
                r.get(MartialArt.MARTIAL_ART.CODE),
                r.get(MartialArt.MARTIAL_ART.NAME),
                r.get(StudentMartialArt.STUDENT_MARTIAL_ART.ACTIVE),
                r.get(StudentMartialArt.STUDENT_MARTIAL_ART.GRADE),
                r.get(StudentMartialArt.STUDENT_MARTIAL_ART.BASE_PAYMENT_AMOUNT),
                r.get(StudentMartialArt.STUDENT_MARTIAL_ART.PAYMENT_TYPE),
                r.get(StudentMartialArt.STUDENT_MARTIAL_ART.CREDIT)
            ));

        return new StudentDto(
                record.getId(),
                record.getFirstname(),
                record.getLastname(),
                record.getGender(),
                record.getIdCard(),
                record.getStreet(),
                record.getStreetNo(),
                record.getCity(),
                record.getZipCode(),
                record.getMobil(),
                record.getEmail(),
                record.getSchoolId(),
                record.getVegetarian(),
                record.getGlutenFree(),
                record.getActive(),
                record.getBirthdate() != null ? record.getBirthdate().format(birthdateFormatter) : null,
                record.getNationalId(),
                record.getStudentType(),
                martialArts
        );
    }
	
    private StudentRecord mapToRecord(StudentDto dto) {
        StudentRecord record = dsl.newRecord(Student.STUDENT);
        record.setId(dto.id());
        record.setFirstname(dto.firstname());
        record.setLastname(dto.lastname());
        record.setGender(dto.gender());
        record.setIdCard(dto.idCard());
        record.setStreet(dto.street());
        record.setStreetNo(dto.streetNo());
        record.setCity(dto.city());
        record.setZipCode(dto.zipCode());
        record.setMobil(dto.mobil());
        record.setEmail(dto.email());
        record.setSchoolId(dto.schoolId());
        record.setVegetarian(dto.vegetarian());
        record.setGlutenFree(dto.glutenFree());
        record.setActive(dto.active());
        record.setBirthdate(dto.birthdate() != null && !dto.birthdate().isBlank()
            ? LocalDate.parse(dto.birthdate(), birthdateFormatter) : null);
        record.setNationalId(dto.nationalId());
        record.setStudentType(dto.studentType());
        return record;
    }
}
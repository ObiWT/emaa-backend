package sk.emaa.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sk.emaa.dto.StudentDto;
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

        // ID nezahrnúť do INSERT-u
        studentRecord.changed(Student.STUDENT.ID, false);

        dsl.transaction(configuration -> {
            DSLContext ctx = DSL.using(configuration);

            // 1. Insert študenta
            ctx.insertInto(Student.STUDENT)
               .set(studentRecord)
               .execute();

            // 2. Získať ID nového študenta
            Integer studentId = ctx.select(DSL.max(Student.STUDENT.ID))
                                   .from(Student.STUDENT)
                                   .fetchOne(0, Integer.class);

            // 3. Uložiť bojové umenia podľa checkboxov
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

            // update bojových umení
            saveStudentMartialArts(ctx, student.id(), student);
        });
    }

    private void saveStudentMartialArts(DSLContext ctx, int studentId, StudentDto dto) {
        // Mapovanie checkbox -> martial_art.code
        Map<String, Boolean> martialArtsMap = Map.of(
                "WING_TSUN", dto.wingTsun(),
                "WING_TSUN_KIDS", dto.wingTsunKids(),
                "CHI_KUNG", dto.chiKung(),
                "ESCRIMA", dto.escrima(),
                "CHANBARA", dto.chanbara()
        );

        for (Map.Entry<String, Boolean> entry : martialArtsMap.entrySet()) {
            // zisti školu pre dané martial art
            Integer martialArtId = ctx.select(MartialArt.MARTIAL_ART.ID)
                                      .from(MartialArt.MARTIAL_ART)
                                      .where(MartialArt.MARTIAL_ART.CODE.eq(entry.getKey()))
                                      .fetchOne(0, Integer.class);
            if (martialArtId == null) continue;

            // skontroluj, či už existuje záznam
            boolean exists = ctx.fetchExists(
                    ctx.selectOne()
                       .from(StudentMartialArt.STUDENT_MARTIAL_ART)
                       .where(StudentMartialArt.STUDENT_MARTIAL_ART.STUDENT_ID.eq(studentId))
                       .and(StudentMartialArt.STUDENT_MARTIAL_ART.MARTIAL_ART_ID.eq(martialArtId))
            );

            if (exists) {
                // update existujúceho
                ctx.update(StudentMartialArt.STUDENT_MARTIAL_ART)
                   .set(StudentMartialArt.STUDENT_MARTIAL_ART.ACTIVE, entry.getValue())
                   .where(StudentMartialArt.STUDENT_MARTIAL_ART.STUDENT_ID.eq(studentId))
                   .and(StudentMartialArt.STUDENT_MARTIAL_ART.MARTIAL_ART_ID.eq(martialArtId))
                   .execute();
            } else {
                // insert nového
                ctx.insertInto(StudentMartialArt.STUDENT_MARTIAL_ART)
                   .set(StudentMartialArt.STUDENT_MARTIAL_ART.STUDENT_ID, studentId)
                   .set(StudentMartialArt.STUDENT_MARTIAL_ART.MARTIAL_ART_ID, martialArtId)
                   .set(StudentMartialArt.STUDENT_MARTIAL_ART.ACTIVE, entry.getValue())
                   .execute();
            }
        }
    }
    
    public StudentDto getStudent(int id) {
		StudentRecord student = dsl.selectFrom(Student.STUDENT)
                .where(Student.STUDENT.ID.eq(id))
                .fetchOne();
		return student != null ? mapToDto(student) : null; // alebo Optional<StudentDto>
	}
	
	public StudentDto addCredit(int studentId, int amountToAdd, int basePaymentAmount) {
        return dsl.<StudentDto>transactionResult(configuration -> {
            DSLContext ctx = DSL.using(configuration);

            // 1️. Zvýš kredit študenta
            ctx.update(Student.STUDENT)
               .set(Student.STUDENT.CREDIT, Student.STUDENT.CREDIT.plus(amountToAdd))
               .where(Student.STUDENT.ID.eq(studentId))
               .execute();

            // 2️. Záznam o dobití do CREDIT_TRANSACTION
            CreditTransactionRecord tx = ctx.newRecord(CreditTransaction.CREDIT_TRANSACTION);
            tx.setStudentId(studentId);
            tx.setAmount(amountToAdd * basePaymentAmount);
            tx.setDescription("DOBITIE KREDITU");
            tx.setPaymentType(AppConstants.paymentType_credit);
            //tx.setCreatedAt(LocalDateTime.now()); // môžeš aj vynechať, keďže má DEFAULT NOW()
            tx.store(); 

            // 3️. Načítaj aktualizovaného študenta
            StudentRecord updated = ctx.selectFrom(Student.STUDENT)
                    .where(Student.STUDENT.ID.eq(studentId))
                    .fetchOne();

            return updated != null ? mapToDto(updated) : null;
        });
    }
	
    private StudentDto mapToDto(StudentRecord record) {
        // najprv načítame aktívne bojové umenia pre študenta
        Map<String, Boolean> martialArtsMap = dsl.select(MartialArt.MARTIAL_ART.CODE, StudentMartialArt.STUDENT_MARTIAL_ART.ACTIVE)
                .from(StudentMartialArt.STUDENT_MARTIAL_ART)
                .join(MartialArt.MARTIAL_ART)
                .on(StudentMartialArt.STUDENT_MARTIAL_ART.MARTIAL_ART_ID.eq(MartialArt.MARTIAL_ART.ID))
                .where(StudentMartialArt.STUDENT_MARTIAL_ART.STUDENT_ID.eq(record.getId()))
                .fetchMap(MartialArt.MARTIAL_ART.CODE, StudentMartialArt.STUDENT_MARTIAL_ART.ACTIVE);

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
                record.getCredit(),
                record.getBirthdate() != null ? record.getBirthdate().format(birthdateFormatter) : null,
                record.getPaymentType(),
                record.getBasePaymentAmount(),
                record.getGrade(),
                record.getNationalId(),
                record.getStudentType(),
                martialArtsMap.getOrDefault("WING_TSUN", false),
                martialArtsMap.getOrDefault("WING_TSUN_KIDS", false),
                martialArtsMap.getOrDefault("CHI_KUNG", false),
                martialArtsMap.getOrDefault("ESCRIMA", false),
                martialArtsMap.getOrDefault("CHANBARA", false)
        );
    }
	
    private StudentRecord mapToRecord(StudentDto dto) {
        StudentRecord record = dsl.newRecord(Student.STUDENT);
        record.setId(dto.id()); // pri create môže byť null
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
        if ("CREDIT".equals(dto.paymentType())) {
            record.setCredit(dto.credit());
        } else {
            record.setCredit(0);
        }
        record.setBirthdate(dto.birthdate() != null && !dto.birthdate().isBlank() ? LocalDate.parse(dto.birthdate(), birthdateFormatter) : null);
        record.setPaymentType(dto.paymentType());
        record.setBasePaymentAmount(dto.basePaymentAmount());
        record.setGrade(dto.grade());
        record.setNationalId(dto.nationalId());
        record.setStudentType(dto.studentType());
        return record;
    }

}

package sk.emaa.service;

import java.time.LocalDate;
import java.util.List;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sk.emaa.dto.StudentDto;
import sk.emaa.model.entity.tables.CreditTransaction;
import sk.emaa.model.entity.tables.Student;
import sk.emaa.model.entity.tables.records.CreditTransactionRecord;
import sk.emaa.model.entity.tables.records.StudentRecord;
import sk.emaa.util.AppConstants;

@Service
@RequiredArgsConstructor
public class StudentService {
	
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
		
		dsl.insertInto(Student.STUDENT)
		   .set(studentRecord)
	       .execute();
	}
	
	public void updateStudent(StudentDto student) {
		StudentRecord studentRecord = mapToRecord(student);
	    dsl.update(Student.STUDENT)
	       .set(studentRecord)
	       .where(Student.STUDENT.ID.eq(student.id()))
	       .execute();
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
	    StudentDto dto = new StudentDto(
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
		    record.getBirthdate() != null ? record.getBirthdate().toString() : null,
		    record.getPaymentType(),
		    record.getBasePaymentAmount(),
		    record.getGrade()
		);
	    return dto;
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
	    if (AppConstants.paymentType_credit.equals(dto.paymentType())) {
	        record.setCredit(dto.credit());
	    } else {
	        record.setCredit(0);
	    }
	    record.setBirthdate(dto.birthdate() != null && !dto.birthdate().isBlank() ? LocalDate.parse(dto.birthdate()) : null);
	    record.setPaymentType(dto.paymentType());
	    record.setBasePaymentAmount(dto.basePaymentAmount());
	    record.setGrade(dto.grade());
	    return record;
	}

}

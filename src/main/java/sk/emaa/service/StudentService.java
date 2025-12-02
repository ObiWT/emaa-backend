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
	       .where(Student.STUDENT.ID.eq(student.getId()))
	       .execute();
	}

	public StudentDto getStudent(int id) {
		StudentRecord student = dsl.selectFrom(Student.STUDENT)
                .where(Student.STUDENT.ID.eq(id))
                .fetchOne();
		return student != null ? mapToDto(student) : null; // alebo Optional<StudentDto>
	}
	
	public StudentDto addCredit(int studentId, int amountToAdd) {
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
            tx.setAmount(amountToAdd * AppConstants.creditPayment);
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
	    StudentDto dto = new StudentDto();
	    dto.setId(record.getId());
	    dto.setFirstname(record.getFirstname());
	    dto.setLastname(record.getLastname());
	    dto.setGender(record.getGender());
	    dto.setIdCard(record.getIdCard());
	    dto.setStreet(record.getStreet());
	    dto.setStreetNo(record.getStreetNo());
	    dto.setCity(record.getCity());
	    dto.setZipCode(record.getZipCode());
	    dto.setMobil(record.getMobil());
	    dto.setEmail(record.getEmail());
	    dto.setSchoolId(record.getSchoolId());
	    dto.setVegetarian(record.getVegetarian());
	    dto.setActive(record.getActive());
	    dto.setCredit(record.getCredit());
	    dto.setBirthdate(record.getBirthdate() != null ? record.getBirthdate().toString() : null);
	    dto.setPaymentType(record.getPaymentType());
	    return dto;
	}
	
	private StudentRecord mapToRecord(StudentDto dto) {
	    StudentRecord record = dsl.newRecord(Student.STUDENT);
	    record.setId(dto.getId()); // pri create môže byť null
	    record.setFirstname(dto.getFirstname());
	    record.setLastname(dto.getLastname());
	    record.setGender(dto.getGender());
	    record.setIdCard(dto.getIdCard());
	    record.setStreet(dto.getStreet());
	    record.setStreetNo(dto.getStreetNo());
	    record.setCity(dto.getCity());
	    record.setZipCode(dto.getZipCode());
	    record.setMobil(dto.getMobil());
	    record.setEmail(dto.getEmail());
	    record.setSchoolId(dto.getSchoolId());
	    record.setVegetarian(dto.getVegetarian());
	    record.setActive(dto.getActive());
	    if ("CREDIT".equals(dto.getPaymentType())) {
	        record.setCredit(dto.getCredit());
	    } else {
	        record.setCredit(0);
	    }
	    record.setBirthdate(dto.getBirthdate() != null && !dto.getBirthdate().isBlank() ? LocalDate.parse(dto.getBirthdate()) : null);
	    record.setPaymentType(dto.getPaymentType());
	    return record;
	}

}

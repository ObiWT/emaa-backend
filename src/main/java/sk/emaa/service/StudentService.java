package sk.emaa.service;

import java.time.LocalDate;
import java.util.List;

import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sk.emaa.dto.StudentDto;
import sk.emaa.model.entity.tables.Student;
import sk.emaa.model.entity.tables.records.StudentRecord;

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
		
		// 👇 povieme jOOQ, že ID sa nemá zahrnúť do INSERT-u
		studentRecord.changed(Student.STUDENT.ID, false);
		
		dsl.insertInto(Student.STUDENT)
		   .set(studentRecord)
		   /*
	       .set(Student.STUDENT.FIRSTNAME, studentRecord.getFirstname())
	       .set(Student.STUDENT.LASTNAME, studentRecord.getLastname())
	       .set(Student.STUDENT.GENDER, studentRecord.getGender())
	       .set(Student.STUDENT.PAYMENT_TYPE, studentRecord.getPaymentType())
	       .set(Student.STUDENT.CREDIT, studentRecord.getCredit())
	       .set(Student.STUDENT.ACTIVE, studentRecord.getActive())
	       .set(Student.STUDENT.ID_CARD, studentRecord.getIdCard())
	       .set(Student.STUDENT.STREET, studentRecord.getStreet())
	       .set(Student.STUDENT.STREET_NO, studentRecord.getStreetNo())
	       .set(Student.STUDENT.CITY, studentRecord.getCity())
	       .set(Student.STUDENT.ZIP_CODE, studentRecord.getZipCode())
	       .set(Student.STUDENT.MOBIL, studentRecord.getMobil())
	       .set(Student.STUDENT.EMAIL, studentRecord.getEmail())
	       .set(Student.STUDENT.SCHOOL_ID, studentRecord.getSchoolId())
	       .set(Student.STUDENT.VEGETARIAN, studentRecord.getVegetarian())
	       .set(Student.STUDENT.BIRTHDATE, studentRecord.getBirthdate())
	       */
	       .execute();
	}
	
	public void updateStudent(StudentDto student) {
		StudentRecord studentRecord = mapToRecord(student);
	    dsl.update(Student.STUDENT)
	       .set(studentRecord)
	       /*
	       .set(Student.STUDENT.FIRSTNAME, student.getFirstname())
	       .set(Student.STUDENT.LASTNAME, student.getLastname())
	       .set(Student.STUDENT.GENDER, student.getGender())
	       .set(Student.STUDENT.ID_CARD, student.getIdCard())
	       .set(Student.STUDENT.STREET, student.getStreet())
	       .set(Student.STUDENT.STREET_NO, student.getStreetNo())
	       .set(Student.STUDENT.ZIP_CODE, student.getZipCode())
	       .set(Student.STUDENT.CITY, student.getCity())
	       .set(Student.STUDENT.MOBIL, student.getMobil())
	       .set(Student.STUDENT.EMAIL, student.getEmail())
	       .set(Student.STUDENT.SCHOOL_ID, student.getSchoolId())
	       .set(Student.STUDENT.VEGETARIAN, student.getVegetarian())
	       .set(Student.STUDENT.ACTIVE, student.getActive())
	       .set(Student.STUDENT.CREDIT, student.getCredit())
	       .set(Student.STUDENT.BIRTHDATE, student.getBirthdate())
	       .set(Student.STUDENT.PAYMENT_TYPE, student.getPaymentType())
	       */
	       .where(Student.STUDENT.ID.eq(student.getId()))
	       .execute();
	}

	public StudentDto getStudent(int id) {
		StudentRecord student = dsl.selectFrom(Student.STUDENT)
                .where(Student.STUDENT.ID.eq(id))
                .fetchOne();
		return student != null ? mapToDto(student) : null; // alebo Optional<StudentDto>
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
	    record.setBirthdate(dto.getBirthdate() != null ? LocalDate.parse(dto.getBirthdate()) : null);
	    record.setPaymentType(dto.getPaymentType());
	    return record;
	}


}

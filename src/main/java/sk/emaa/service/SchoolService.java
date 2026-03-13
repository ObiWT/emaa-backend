package sk.emaa.service;

import org.jooq.DSLContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import sk.emaa.dto.SchoolDto;
import sk.emaa.model.entity.tables.School;
import sk.emaa.model.entity.tables.records.SchoolRecord;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SchoolService {
	
	private final DSLContext dsl;

    public SchoolDto getSchool(int schoolId) {
    	SchoolRecord school = dsl.selectFrom(School.SCHOOL)
                .where(School.SCHOOL.ID.eq(schoolId))
                .fetchOne();
		return school != null ? mapToDto(school) : null; // alebo Optional<StudentDto>
    }

    
    private SchoolDto mapToDto(SchoolRecord record) {
    	SchoolDto dto = new SchoolDto(
	    	record.getId(),
	    	record.getName(),
		    record.getAddress()
		);
	    return dto;
	}

}

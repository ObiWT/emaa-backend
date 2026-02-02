package sk.emaa.dto;

import java.time.LocalDate;

public record TrainingAttendanceDto(Integer trainingId, LocalDate date) implements Comparable<TrainingAttendanceDto> {
	
	@Override
    public int compareTo(TrainingAttendanceDto o) {
        return this.date.compareTo(o.date); // zoradenie podľa dátumu
    }
	
}

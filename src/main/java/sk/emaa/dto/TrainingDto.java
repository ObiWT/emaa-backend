package sk.emaa.dto;

import java.time.LocalDate;

public record TrainingDto(LocalDate date, Integer schoolId, Integer martialArtId) {

}

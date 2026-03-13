package sk.emaa.dto;

import java.time.LocalDateTime;

public record CreditTransactionDto (int id, int studentId, int martialArtId, int amount, String description, LocalDateTime createdAt) {

}

package sk.emaa.dto;

public record StudentMartialArtDto(
    Integer martialArtId,
    String martialArtCode,
    String martialArtName,
    Boolean active,
    Integer grade,
    Integer basePaymentAmount,
    String paymentType,
    Integer credit
) {}
package sk.emaa.dto;

public record MartialArtDto(
    int id,
    String code,
    String name,
    String programType,
    String variant,
    int schoolId,
    boolean active,
    Integer creditPayment,
    Integer monthlyPayment,
    Integer yearlyPayment,
    String color
) {}
package sk.emaa.dto;

public record StudentDto(
    Integer id,
    String firstname,
    String lastname,
    String gender, // 'M' | 'F'
    String idCard,
    String street,
    String streetNo,
    String city,
    String zipCode,
    String mobil,
    String email,
    Integer schoolId,
    Boolean vegetarian,
    Boolean glutenFree,
    Boolean active,
    Integer credit,
    String birthdate, // ISO string, napr. "2008-04-15"
    String paymentType, // 'MONTHLY' | 'YEARLY'  | 'CREDIT' | 'NO_PAYMENT'
    Integer basePaymentAmount,
    Integer grade,
    String nationalId
) {}


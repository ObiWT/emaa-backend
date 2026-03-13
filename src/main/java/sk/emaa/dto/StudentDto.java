package sk.emaa.dto;

import java.util.List;

public record StudentDto(
    Integer id,
    String firstname,
    String lastname,
    String gender,
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
    String birthdate,
    String nationalId,
    String studentType,
    List<StudentMartialArtDto> martialArts
) {}
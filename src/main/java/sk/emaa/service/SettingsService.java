package sk.emaa.service;

import org.jooq.DSLContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import sk.emaa.dto.ChangeAddressDto;
import sk.emaa.dto.ChangePaymentsDto;
import sk.emaa.dto.MartialArtDto;
import sk.emaa.model.entity.tables.MartialArt;
import sk.emaa.model.entity.tables.School;
import sk.emaa.model.entity.tables.UserAccount;
import sk.emaa.model.entity.tables.records.MartialArtRecord;
import sk.emaa.util.AppConstants;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SettingsService {

	private final DSLContext dsl;
	private final PasswordEncoder passwordEncoder;

	public void changePassword(String username, String newPassword) {
		String encodedPassword = passwordEncoder.encode(newPassword);
		int updated = dsl.update(UserAccount.USER_ACCOUNT).set(UserAccount.USER_ACCOUNT.PASSWORD, encodedPassword)
				.where(UserAccount.USER_ACCOUNT.USERNAME.eq(username)).execute();
		if (updated == 0) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Používateľ nenájdený: " + username);
		}
	}

	public void changePayments(ChangePaymentsDto newPayments) {
		int updated = dsl.update(MartialArt.MARTIAL_ART)
				.set(MartialArt.MARTIAL_ART.CREDIT_PAYMENT, newPayments.credit())
				.set(MartialArt.MARTIAL_ART.MONTHLY_PAYMENT, newPayments.monthly())
				.set(MartialArt.MARTIAL_ART.YEARLY_PAYMENT, newPayments.yearly())
				.where(MartialArt.MARTIAL_ART.ID.eq(newPayments.martialArtId())).execute();
		if (updated == 0) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,
					"Bojové umenie s id=" + newPayments.martialArtId() + " nenájdené");
		}
	}

	public void changeAddress(Integer schoolId, ChangeAddressDto newAddress) {
		int updated = dsl.update(School.SCHOOL).set(School.SCHOOL.ADDRESS, newAddress.newAddress())
				.where(School.SCHOOL.ID.eq(schoolId)).execute();
		if (updated == 0) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Škola s id=" + schoolId + " nenájdená");
		}
	}

	public List<MartialArtDto> getMartialArts(Integer schoolId) {
		return dsl.selectFrom(MartialArt.MARTIAL_ART).where(MartialArt.MARTIAL_ART.SCHOOL_ID.eq(schoolId))
				.and(MartialArt.MARTIAL_ART.PROGRAM_TYPE.eq(AppConstants.PROGRAM_TYPE_CONTINUOUS))
				.orderBy(MartialArt.MARTIAL_ART.ID.asc())
				.fetch(this::mapToDto);
	}

	public void updateMartialArtActive(Integer martialArtId, boolean active) {
		int updated = dsl.update(MartialArt.MARTIAL_ART).set(MartialArt.MARTIAL_ART.ACTIVE, active)
				.where(MartialArt.MARTIAL_ART.ID.eq(martialArtId)).execute();
		if (updated == 0) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,
					"Bojové umenie s id=" + martialArtId + " nenájdené");
		}
	}

	private MartialArtDto mapToDto(MartialArtRecord record) {
		return new MartialArtDto(record.getId(), record.getCode(), record.getName(), record.getProgramType(),
				record.getVariant(), record.getSchoolId(), record.getActive(), record.getCreditPayment(),
				record.getMonthlyPayment(), record.getYearlyPayment(), record.getColor());
	}
}
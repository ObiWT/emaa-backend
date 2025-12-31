package sk.emaa.service;

import org.jooq.DSLContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import sk.emaa.dto.ChangePaymentsDto;
import sk.emaa.model.entity.tables.School;
import sk.emaa.model.entity.tables.UserAccount;

@Service
@RequiredArgsConstructor
public class SettingsService {
	
	private final DSLContext dsl;
	private final PasswordEncoder passwordEncoder; // 👈 injektovaný bean zo SecurityConfig

    public void changePassword(String username, String newPassword) {
        // zašifrovanie hesla
        String encodedPassword = passwordEncoder.encode(newPassword);

        // update hesla v DB cez jOOQ
        int updated = dsl.update(UserAccount.USER_ACCOUNT)
                .set(UserAccount.USER_ACCOUNT.PASSWORD, encodedPassword)
                .where(UserAccount.USER_ACCOUNT.USERNAME.eq(username))
                .execute();

        if (updated == 0) {
        	throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Používateľ nenájdený: " + username);
        }
    }

	public void changePayments(Integer schoolId, ChangePaymentsDto newPayments) {
		int updated = dsl.update(School.SCHOOL)
                .set(School.SCHOOL.CREDIT_PAYMENT, newPayments.credit())
                .set(School.SCHOOL.MONTHLY_PAYMENT, newPayments.monthly())
                .set(School.SCHOOL.YEARLY_PAYMENT, newPayments.yearly())
                .where(School.SCHOOL.ID.eq(schoolId))
                .execute();

        if (updated == 0) {
        	throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Škola s id=" + schoolId + " nenájdená");
        }
		
	}

}

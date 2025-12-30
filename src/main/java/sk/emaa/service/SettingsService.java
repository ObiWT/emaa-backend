package sk.emaa.service;

import org.jooq.DSLContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
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
            throw new RuntimeException("Používateľ nenájdený: " + username);
        }
    }

}

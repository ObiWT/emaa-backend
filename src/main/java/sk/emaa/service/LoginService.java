package sk.emaa.service;

import org.jooq.DSLContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sk.emaa.model.entity.tables.School;
import sk.emaa.model.entity.tables.UserAccount;
import sk.emaa.security.JwtProvider;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final DSLContext dsl;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    
    public String login(String username, String password) {
    	var record = dsl.select(
                UserAccount.USER_ACCOUNT.ID,
                UserAccount.USER_ACCOUNT.USERNAME,
                UserAccount.USER_ACCOUNT.PASSWORD,
                UserAccount.USER_ACCOUNT.SCHOOL_ID,
                School.SCHOOL.NAME.as("school_name")
            )
            .from(UserAccount.USER_ACCOUNT)
            .join(School.SCHOOL)
                .on(UserAccount.USER_ACCOUNT.SCHOOL_ID.eq(School.SCHOOL.ID))
            .where(UserAccount.USER_ACCOUNT.USERNAME.eq(username))
            .fetchOne();


        if (record == null) {
            throw new RuntimeException("User not found");
        }

        // heslo overíš priamo z výsledku
        String encodedPassword = record.get(UserAccount.USER_ACCOUNT.PASSWORD);
        if (!passwordEncoder.matches(password, encodedPassword)) {
            throw new RuntimeException("Invalid password");
        }
        
        // extrahuj dáta pre token
        String usernameDb = record.get(UserAccount.USER_ACCOUNT.USERNAME);
        Integer schoolId = record.get(UserAccount.USER_ACCOUNT.SCHOOL_ID);
        String schoolName = record.get("school_name", String.class);

        return jwtProvider.createToken(usernameDb, "USER", schoolId, schoolName); // role môžeš pridať do tabuľky
    }
}

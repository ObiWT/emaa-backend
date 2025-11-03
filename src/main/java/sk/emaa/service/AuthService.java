package sk.emaa.service;

import org.jooq.DSLContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sk.emaa.model.entity.tables.UserAccount;
import sk.emaa.model.entity.tables.records.UserAccountRecord;
import sk.emaa.security.JwtTokenProvider;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final DSLContext dsl;
    private final PasswordEncoder passwordEncoder;;
    private final JwtTokenProvider jwtTokenProvider;
    
    public String login(String username, String password) {
        UserAccountRecord user = dsl.selectFrom(UserAccount.USER_ACCOUNT)
                .where(UserAccount.USER_ACCOUNT.USERNAME.eq(username))
                .fetchOne();

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return jwtTokenProvider.createToken(user.getUsername(), "USER"); // role môžeš pridať do tabuľky
    }
}

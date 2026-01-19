package sk.emaa.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGeneratorTest {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "Kungfupanda";
        String encodedPassword = encoder.encode(rawPassword);
        System.out.println(encodedPassword);
    }
}


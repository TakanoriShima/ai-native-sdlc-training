package com.example.salesmanagement.auth.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * db/migration-demo に埋め込まれているデモ用パスワードハッシュが、
 * 実際に採用している PasswordEncoder で DemoPass123! を検証できることを保証する回帰テスト。
 */
class DemoPasswordHashTest {

    private static final String DEMO_PASSWORD = "DemoPass123!";
    private static final String DEMO_PASSWORD_HASH =
            "{bcrypt}$2a$10$AP/MdUnZJk6OjR59puG2d.R7rbbY04y/Gk8KbxWMklqCn1E823aye";

    @Test
    void demoPasswordHashMatchesConfiguredEncoder() {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

        assertThat(encoder.matches(DEMO_PASSWORD, DEMO_PASSWORD_HASH)).isTrue();
    }
}

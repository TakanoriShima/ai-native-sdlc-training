package com.example.salesmanagement.auth;

import com.example.salesmanagement.support.PostgresTestcontainerSupport;
import com.example.salesmanagement.user.entity.Role;
import com.example.salesmanagement.user.entity.User;
import com.example.salesmanagement.user.repository.UserRepository;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

/**
 * dev プロファイルで db/migration と db/migration-demo の両方が適用され、
 * 開発・デモ用 6 ロール初期ユーザーが投入されることを検証する統合テスト（HD1, HD10, HD11, HD12, HD19）。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class DemoUserFlywayIntegrationTest extends PostgresTestcontainerSupport {

    private static final String DEMO_PASSWORD = "DemoPass123!";
    private static final PasswordEncoder ENCODER = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @org.junit.jupiter.api.Test
    void devProfile_seedsSixDemoUsers_withExpectedRolesAndValidPasswordHash() {
        List<User> users = userRepository.findAll();

        assertThat(users).hasSize(6);
        assertThat(users).extracting(User::getRole)
                .containsExactlyInAnyOrder(
                        Role.SALES, Role.SALES_MANAGER, Role.WORK_MANAGER,
                        Role.WORKER, Role.ACCOUNTING, Role.ADMIN);
        assertThat(users).allMatch(User::isEnabled);

        // password_hash が単なる固定文字列比較ではなく、実際に採用している
        // DelegatingPasswordEncoder で DemoPass123! を検証できる値であることを確認する（HD13）。
        assertThat(users).allMatch(u -> ENCODER.matches(DEMO_PASSWORD, u.getPasswordHash()));
    }

    @ParameterizedTest
    @CsvSource({
            "sales@example.com, SALES",
            "sales-manager@example.com, SALES_MANAGER",
            "work-manager@example.com, WORK_MANAGER",
            "worker@example.com, WORKER",
            "accounting@example.com, ACCOUNTING",
            "admin@example.com, ADMIN"
    })
    void devProfile_eachDemoUser_canLogInWithDemoPassword_andHasExpectedRole(String email, String roleName) throws Exception {
        // migration で実際に投入されたユーザーであることを前提にする（ハードコードした期待値だけに頼らない）。
        Optional<User> seededUser = userRepository.findByEmail(email);
        assertThat(seededUser).as("migration により email=%s のユーザーが投入されていること", email).isPresent();
        assertThat(seededUser.get().getRole()).isEqualTo(Role.valueOf(roleName));

        mockMvc.perform(formLogin("/login")
                        .user("email", email)
                        .password("password", DEMO_PASSWORD))
                .andExpect(authenticated().withUsername(email).withRoles(roleName))
                .andExpect(redirectedUrl("/dashboard"));
    }
}

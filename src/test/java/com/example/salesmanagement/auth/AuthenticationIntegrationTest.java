package com.example.salesmanagement.auth;

import com.example.salesmanagement.support.PostgresTestcontainerSupport;
import com.example.salesmanagement.user.entity.Role;
import com.example.salesmanagement.user.entity.User;
import com.example.salesmanagement.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ログイン〜Dashboard〜ログアウトの一連フローを、通常プロファイル（dev プロファイルなし）・
 * 実際の PostgreSQL（Testcontainers）・実際の Flyway migration に対して検証する統合テスト。
 *
 * 通常プロファイルでは db/migration-demo は適用されないため、デモユーザーは存在しない前提で、
 * このテスト自身が検証用ユーザーを1件だけ投入する。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class AuthenticationIntegrationTest extends PostgresTestcontainerSupport {

    private static final PasswordEncoder ENCODER = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    private static final String PASSWORD = "Password123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void normalProfile_hasNoDemoUsers() {
        assertThat(userRepository.count()).isZero();
    }

    @Test
    void login_dashboard_logout_fullFlow() throws Exception {
        userRepository.save(new User("結合太郎", "integration@example.com", ENCODER.encode(PASSWORD), Role.ADMIN, true));

        MvcResult loginResult = mockMvc.perform(formLogin("/login")
                        .user("email", "integration@example.com")
                        .password("password", PASSWORD))
                .andExpect(authenticated())
                .andExpect(redirectedUrl("/dashboard"))
                .andReturn();
        org.springframework.mock.web.MockHttpSession session =
                (org.springframework.mock.web.MockHttpSession) loginResult.getRequest().getSession(false);

        mockMvc.perform(get("/dashboard").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("結合太郎")))
                .andExpect(content().string(containsString("ADMIN")));

        mockMvc.perform(post("/logout")
                        .session(session)
                        .with(csrf()))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?logout"));

        // ログアウト後は同一セッションで/dashboardへアクセスしても再びログインを要求される（F4）
        mockMvc.perform(get("/dashboard").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void login_withoutCsrfToken_isRejected() throws Exception {
        userRepository.save(new User("CSRF太郎", "csrf-login@example.com", ENCODER.encode(PASSWORD), Role.SALES, true));

        mockMvc.perform(post("/login")
                        .param("email", "csrf-login@example.com")
                        .param("password", PASSWORD))
                .andExpect(status().isForbidden());
    }

    @Test
    void logout_withoutCsrfToken_isRejected_andSessionRemainsAuthenticated() throws Exception {
        userRepository.save(new User("CSRF次郎", "csrf-logout@example.com", ENCODER.encode(PASSWORD), Role.SALES, true));

        MvcResult loginResult = mockMvc.perform(formLogin("/login")
                        .user("email", "csrf-logout@example.com")
                        .password("password", PASSWORD))
                .andExpect(authenticated())
                .andReturn();
        org.springframework.mock.web.MockHttpSession session =
                (org.springframework.mock.web.MockHttpSession) loginResult.getRequest().getSession(false);

        mockMvc.perform(post("/logout").session(session))
                .andExpect(status().isForbidden());

        // CSRF拒否によりログアウトは成立せず、セッションは認証済みのまま
        mockMvc.perform(get("/dashboard").session(session))
                .andExpect(status().isOk());
    }

    @Test
    void login_fails_withInvalidPassword() throws Exception {
        userRepository.save(new User("結合太郎", "integration2@example.com", ENCODER.encode(PASSWORD), Role.SALES, true));

        mockMvc.perform(formLogin("/login")
                        .user("email", "integration2@example.com")
                        .password("password", "wrong-password"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void login_fails_withDisabledUser() throws Exception {
        userRepository.save(new User("無効太郎", "disabled-integration@example.com", ENCODER.encode(PASSWORD), Role.SALES, false));

        mockMvc.perform(formLogin("/login")
                        .user("email", "disabled-integration@example.com")
                        .password("password", PASSWORD))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void dashboard_redirectsToLogin_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}

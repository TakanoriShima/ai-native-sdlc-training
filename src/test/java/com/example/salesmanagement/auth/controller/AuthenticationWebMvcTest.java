package com.example.salesmanagement.auth.controller;

import com.example.salesmanagement.auth.config.SecurityConfig;
import com.example.salesmanagement.auth.service.AppUserDetailsService;
import com.example.salesmanagement.auth.service.AppUserPrincipal;
import com.example.salesmanagement.user.entity.Role;
import com.example.salesmanagement.user.entity.User;
import com.example.salesmanagement.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = {LoginController.class, DashboardController.class})
@Import({SecurityConfig.class, AppUserDetailsService.class})
class AuthenticationWebMvcTest {

    private static final PasswordEncoder ENCODER = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void loginPage_isAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void dashboard_redirectsToLogin_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void dashboard_showsNameAndRole_whenAuthenticated() throws Exception {
        User user = new User("テスト太郎", "taro@example.com", "hash", Role.SALES, true);

        mockMvc.perform(get("/dashboard").with(user(new AppUserPrincipal(user))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("テスト太郎")))
                .andExpect(content().string(containsString("SALES")));
    }

    @Test
    void login_succeeds_withValidCredentials() throws Exception {
        User user = new User("テスト太郎", "taro@example.com", ENCODER.encode("Password123!"), Role.SALES, true);
        when(userRepository.findByEmail("taro@example.com")).thenReturn(Optional.of(user));

        mockMvc.perform(formLogin("/login")
                        .user("email", "taro@example.com")
                        .password("password", "Password123!"))
                .andExpect(authenticated())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    void login_fails_withInvalidPassword() throws Exception {
        User user = new User("テスト太郎", "taro@example.com", ENCODER.encode("Password123!"), Role.SALES, true);
        when(userRepository.findByEmail("taro@example.com")).thenReturn(Optional.of(user));

        mockMvc.perform(formLogin("/login")
                        .user("email", "taro@example.com")
                        .password("password", "wrong-password"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void login_fails_withDisabledUser_sameRedirectAsInvalidCredentials() throws Exception {
        User user = new User("無効ユーザー", "disabled@example.com", ENCODER.encode("Password123!"), Role.SALES, false);
        when(userRepository.findByEmail("disabled@example.com")).thenReturn(Optional.of(user));

        mockMvc.perform(formLogin("/login")
                        .user("email", "disabled@example.com")
                        .password("password", "Password123!"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void loginPage_showsCommonErrorMessage_whenErrorParamPresent() throws Exception {
        mockMvc.perform(get("/login").param("error", ""))
                .andExpect(content().string(containsString("メールアドレスまたはパスワードが正しくありません")));
    }

    @Test
    void loginPage_showsLogoutMessage_whenLogoutParamPresent() throws Exception {
        mockMvc.perform(get("/login").param("logout", ""))
                .andExpect(content().string(containsString("ログアウトしました")));
    }

    @Test
    void logout_invalidatesAuthentication_andBlocksFurtherDashboardAccess() throws Exception {
        User user = new User("テスト太郎", "taro@example.com", ENCODER.encode("Password123!"), Role.SALES, true);
        when(userRepository.findByEmail("taro@example.com")).thenReturn(Optional.of(user));

        MvcResult loginResult = mockMvc.perform(formLogin("/login")
                        .user("email", "taro@example.com")
                        .password("password", "Password123!"))
                .andExpect(authenticated())
                .andReturn();
        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

        mockMvc.perform(post("/logout").session(session).with(csrf()))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?logout"));

        mockMvc.perform(get("/dashboard").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void login_withoutCsrfToken_isRejected() throws Exception {
        mockMvc.perform(post("/login")
                        .param("email", "taro@example.com")
                        .param("password", "Password123!"))
                .andExpect(status().isForbidden());
    }

    @Test
    void logout_withoutCsrfToken_isRejected_andSessionRemainsAuthenticated() throws Exception {
        User user = new User("テスト太郎", "taro@example.com", ENCODER.encode("Password123!"), Role.SALES, true);
        when(userRepository.findByEmail("taro@example.com")).thenReturn(Optional.of(user));

        MvcResult loginResult = mockMvc.perform(formLogin("/login")
                        .user("email", "taro@example.com")
                        .password("password", "Password123!"))
                .andExpect(authenticated())
                .andReturn();
        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

        mockMvc.perform(post("/logout").session(session))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/dashboard").session(session))
                .andExpect(status().isOk());
    }
}

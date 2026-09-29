package com.example.salesmanagement.auth.service;

import com.example.salesmanagement.user.entity.Role;
import com.example.salesmanagement.user.entity.User;
import com.example.salesmanagement.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AppUserDetailsService appUserDetailsService;

    @Test
    void loadUserByUsername_returnsPrincipal_whenUserExistsAndEnabled() {
        User user = new User("テスト太郎", "taro@example.com", "hash", Role.ADMIN, true);
        when(userRepository.findByEmail("taro@example.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = appUserDetailsService.loadUserByUsername("taro@example.com");

        assertThat(userDetails).isInstanceOf(AppUserPrincipal.class);
        assertThat(userDetails.getUsername()).isEqualTo("taro@example.com");
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.getAuthorities())
                .extracting(Object::toString)
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void loadUserByUsername_returnsDisabledPrincipal_whenUserIsDisabled() {
        User user = new User("無効ユーザー", "disabled@example.com", "hash", Role.SALES, false);
        when(userRepository.findByEmail("disabled@example.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = appUserDetailsService.loadUserByUsername("disabled@example.com");

        assertThat(userDetails.isEnabled()).isFalse();
    }

    @Test
    void loadUserByUsername_throws_whenUserDoesNotExist() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appUserDetailsService.loadUserByUsername("nobody@example.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}

package com.example.salesmanagement.user.repository;

import com.example.salesmanagement.support.PostgresTestcontainerSupport;
import com.example.salesmanagement.user.entity.Role;
import com.example.salesmanagement.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest extends PostgresTestcontainerSupport {

    @org.springframework.beans.factory.annotation.Autowired
    private UserRepository userRepository;

    @Test
    void findByEmail_returnsUser_whenEmailExists() {
        userRepository.save(new User("テスト太郎", "taro@example.com", "hash", Role.SALES, true));

        Optional<User> found = userRepository.findByEmail("taro@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("テスト太郎");
        assertThat(found.get().getRole()).isEqualTo(Role.SALES);
    }

    @Test
    void findByEmail_returnsEmpty_whenEmailDoesNotExist() {
        Optional<User> found = userRepository.findByEmail("nobody@example.com");

        assertThat(found).isEmpty();
    }
}

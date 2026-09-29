package com.example.salesmanagement.auth;

import com.example.salesmanagement.user.repository.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Flyway migration location の分離（HD12, HD19）を、プロファイルごとに新規かつ隔離された
 * Testcontainers PostgreSQL 上で検証する。
 *
 * それぞれの Nested クラスが専用のコンテナを持つため、他のテストクラス（{@code
 * PostgresTestcontainerSupport} を共有する既存テスト群を含む）が投入したデータや適用済み
 * migration の影響を受けない。データ削除等の後処理を行う前の、Flyway 適用直後の状態を
 * そのまま検証する（Codex Independent Review F1）。
 */
class FlywayProfileIsolationTest {

    @Nested
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
    @Testcontainers
    class NormalProfile {

        @Container
        @ServiceConnection
        static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(DockerImageName.parse("postgres:18"));

        @Autowired
        private JdbcTemplate jdbcTemplate;

        @Autowired
        private UserRepository userRepository;

        @Test
        void onlyNormalMigrationIsApplied_andNoDemoUsersExist() {
            List<Integer> appliedVersions = jdbcTemplate.queryForList(
                    "SELECT version::int FROM flyway_schema_history WHERE success = true ORDER BY version",
                    Integer.class);

            assertThat(appliedVersions)
                    .as("通常プロファイルでは db/migration (V1) のみが適用され、"
                            + "db/migration-demo (V2) は適用されないこと")
                    .containsExactly(1);

            assertThat(userRepository.count())
                    .as("通常プロファイルではデモユーザーが投入されないこと")
                    .isZero();
        }
    }

    @Nested
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
    @ActiveProfiles("dev")
    @Testcontainers
    class DevProfile {

        @Container
        @ServiceConnection
        static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(DockerImageName.parse("postgres:18"));

        @Autowired
        private JdbcTemplate jdbcTemplate;

        @Autowired
        private UserRepository userRepository;

        @Test
        void bothNormalAndDemoMigrationsAreApplied() {
            List<Integer> appliedVersions = jdbcTemplate.queryForList(
                    "SELECT version::int FROM flyway_schema_history WHERE success = true ORDER BY version",
                    Integer.class);

            assertThat(appliedVersions)
                    .as("dev プロファイルでは db/migration (V1) と db/migration-demo (V2) の"
                            + "両方が適用されること")
                    .containsExactly(1, 2);

            assertThat(userRepository.count())
                    .as("dev プロファイルでは6件のデモユーザーが投入されること")
                    .isEqualTo(6);
        }
    }
}

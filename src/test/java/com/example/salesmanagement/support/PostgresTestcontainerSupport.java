package com.example.salesmanagement.support;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * PostgreSQL を必要とする Repository Test / Integration Test の共通基底クラス（HD19）。
 * 実際のアプリケーション起動確認は、これとは別に docker-compose.yml の PostgreSQL を用いて行う。
 */
@Testcontainers
public abstract class PostgresTestcontainerSupport {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(DockerImageName.parse("postgres:18"));
}

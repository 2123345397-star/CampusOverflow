package com.campusoverflow.qa.infrastructure.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JPA 持久化层测试：验证 QuestionJpaEntity 的保存、查询，以及计数器字段的原子更新。
 * 使用 H2 内存库（MySQL 兼容模式），通过 Hibernate 根据实体自动建表（ddl-auto）。
 */
@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class QuestionJpaRepositoryTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestConfig {
    }

    @Autowired
    private QuestionJpaRepository repository;

    @Autowired
    private TestEntityManager em;

    @Test
    void savesAndFindsQuestion() {
        QuestionJpaEntity entity = new QuestionJpaEntity(1001L, 42L, Instant.now());
        entity.setTitle("如何配置 Flyway");
        entity.setBody("正文内容");
        entity.setUpdatedAt(Instant.now());
        entity.getTags().add("java");
        repository.save(entity);
        em.flush();
        em.clear();

        QuestionJpaEntity found = repository.findById(entity.getId()).orElseThrow();
        assertThat(found.getTitle()).isEqualTo("如何配置 Flyway");
        assertThat(found.getAuthorId()).isEqualTo(1001L);
        assertThat(found.getCourseId()).isEqualTo(42L);
        assertThat(found.getTags()).contains("java");
    }

    @Test
    void atomicallyAdjustsAnswerCountAndScore() {
        QuestionJpaEntity entity = new QuestionJpaEntity(1001L, 42L, Instant.now());
        entity.setTitle("标题");
        entity.setBody("正文");
        entity.setUpdatedAt(Instant.now());
        repository.save(entity);
        em.flush();
        em.clear();

        repository.adjustAnswerCount(entity.getId(), 1);
        repository.adjustScore(entity.getId(), 5);
        em.flush();
        em.clear();

        QuestionJpaEntity reloaded = repository.findById(entity.getId()).orElseThrow();
        assertThat(reloaded.getAnswerCount()).isEqualTo(1);
        assertThat(reloaded.getScore()).isEqualTo(5);
    }
}

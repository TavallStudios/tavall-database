package org.tavall.database.postgres.jpa.fixtures;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "test_jpa_entity")
public class TestJpaEntity {

    @Id
    private Long id;

    protected TestJpaEntity() {
    }

    public TestJpaEntity(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}

package org.tavall.database.postgres.fixture;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tavall_jpa_probe")
public class JpaProbeEntity {
    @Id
    @Column(name = "probe_id", nullable = false, length = 64)
    private String id;

    @Column(name = "probe_value", nullable = false, length = 128)
    private String value;

    protected JpaProbeEntity() {
    }

    public JpaProbeEntity(String id, String value) {
        this.id = id;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public String getValue() {
        return value;
    }
}

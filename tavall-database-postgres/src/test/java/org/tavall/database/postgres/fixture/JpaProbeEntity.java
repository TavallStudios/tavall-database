package org.tavall.database.postgres.fixture;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(name = "tavall_jpa_probe")
@NamedQueries({
        @NamedQuery(
                name = JpaProbeEntity.FIND_BY_VALUE,
                query = "SELECT probe FROM JpaProbeEntity probe "
                        + "WHERE probe.value = :value ORDER BY probe.id"
        ),
        @NamedQuery(
                name = JpaProbeEntity.UPDATE_VALUE_BY_ID,
                query = "UPDATE JpaProbeEntity probe SET probe.value = :value "
                        + "WHERE probe.id = :id"
        )
})
public class JpaProbeEntity {
    public static final String FIND_BY_VALUE = "JpaProbeEntity.findByValue";
    public static final String UPDATE_VALUE_BY_ID =
            "JpaProbeEntity.updateValueById";

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

    public JpaProbeEntity withValue(String replacement) {
        return new JpaProbeEntity(id, replacement);
    }
}

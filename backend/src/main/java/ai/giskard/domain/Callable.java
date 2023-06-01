package ai.giskard.domain;

import ai.giskard.domain.ml.SuiteTest;
import ai.giskard.utils.SimpleJSONStringAttributeConverter;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Getter
@MappedSuperclass
@Setter
public class Callable implements Serializable {
    @Id
    private UUID uuid;
    @Column(nullable = false)
    private String name;

    @Column
    private String displayName;
    @Column(nullable = false)
    private int version;
    private String module;

    @Column(columnDefinition = "VARCHAR")
    private String doc;
    @Column(columnDefinition = "VARCHAR")
    private String moduleDoc;
    @Column(nullable = false, columnDefinition = "VARCHAR")
    private String code;
    @Column(columnDefinition = "VARCHAR")
    @Convert(converter = SimpleJSONStringAttributeConverter.class)
    private List<String> tags;

    @OneToMany(mappedBy = "testFunction", cascade = CascadeType.ALL)
    private List<SuiteTest> suiteTests;

}

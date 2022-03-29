package net.catenax.irs.aspectmodels;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum AspectModelTypes {
    SERIAL_PART_TYPIZATION("serialPartTypization"),
    ASSEMBLY_PART_RELATIONSHIP("assemblyPartRelationship");

    private String value;

    AspectModelTypes(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
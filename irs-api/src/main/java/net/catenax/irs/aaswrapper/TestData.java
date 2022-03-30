package net.catenax.irs.aaswrapper;

import java.util.Arrays;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import net.catenax.irs.aspectmodels.assemblypartrelationship.AssemblyPartRelationship;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestData {
    @JsonProperty("catenaXId")
    private final String catenaXId;
    @JsonProperty("AssemblyPartRelationship")
    private final AssemblyPartRelationship assemblyPartRelationship;

    @JsonCreator
    public TestData(@JsonProperty(value = "catenaXId") final String catenaXId,
            @JsonProperty("AssemblyPartRelationship") final AssemblyPartRelationship[] assemblyPartRelationship) {
        this.catenaXId = catenaXId;
        AssemblyPartRelationship tmp = new AssemblyPartRelationship(catenaXId, Set.of());
        try {
            if (assemblyPartRelationship != null && Arrays.stream(assemblyPartRelationship).findAny().isPresent()) {
                tmp = assemblyPartRelationship[0];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.assemblyPartRelationship = tmp;
    }

    @Override
    public String toString() {
        return "TestData{" + "catenaXId='" + catenaXId + '\'' + ", assemblyPartRelationship=" + assemblyPartRelationship
                + '}';
    }
}
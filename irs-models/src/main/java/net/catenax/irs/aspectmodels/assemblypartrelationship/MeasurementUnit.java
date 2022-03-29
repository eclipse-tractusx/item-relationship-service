package net.catenax.irs.aspectmodels.assemblypartrelationship;

import java.util.Objects;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.openmanufacturing.sds.metamodel.datatypes.Curie;
import lombok.Getter;

/**
 * Created class for MeasurementUnit. Comprises the datatypeURI and the lexical value for the
 * respective quantity
 */
@Getter
public class MeasurementUnit {

    @NotNull
    private final String datatypeURI;

    @NotNull
    private final String lexicalValue;

    @JsonCreator
    public MeasurementUnit(@JsonProperty(value = "datatypeURI") String datatypeURI,
            @JsonProperty(value = "lexicalValue") String lexicalValue) {
        this.datatypeURI = datatypeURI;
        this.lexicalValue = lexicalValue;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final MeasurementUnit that = (MeasurementUnit) o;
        return Objects.equals(datatypeURI, that.datatypeURI) && Objects.equals(lexicalValue, that.lexicalValue);
    }
}

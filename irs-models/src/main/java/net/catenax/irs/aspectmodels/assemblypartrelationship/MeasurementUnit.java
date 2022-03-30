package net.catenax.irs.aspectmodels.assemblypartrelationship;

import java.io.Serializable;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * Created class for MeasurementUnit. Comprises the datatypeURI and the lexical value for the
 * respective quantity
 */
@Getter
public class MeasurementUnit implements Serializable {

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

    @Override
    public int hashCode() {
        return Objects.hash(datatypeURI, lexicalValue);
    }

    @Override
    public String toString() {
        return "MeasurementUnit{" + "datatypeURI='" + datatypeURI + '\'' + ", lexicalValue='" + lexicalValue + '\''
                + '}';
    }
}

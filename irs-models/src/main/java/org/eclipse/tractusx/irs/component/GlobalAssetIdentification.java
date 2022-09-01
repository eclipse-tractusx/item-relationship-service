//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package org.eclipse.tractusx.irs.component;

import java.io.IOException;

import javax.validation.Valid;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * Global unique identifier for asset
 */

@Schema(description = "CATENA-X global asset id in the format urn:uuid:uuid4.")
@Value
@AllArgsConstructor(staticName = "of")
@JsonSerialize(using = ToStringSerializer.class)
@JsonDeserialize(using = GlobalAssetIdentification.DefinitionDeserializer.class)
public class GlobalAssetIdentification {

    private static final int GLOBAL_ASSET_ID_LENGTH = 45;

    @Valid
    @Schema(description = "CATENA-X global asset id in the format urn:uuid:uuid4.", example = "urn:uuid:6c311d29-5753-46d4-b32c-19b918ea93b0",
            minLength = GLOBAL_ASSET_ID_LENGTH, maxLength = GLOBAL_ASSET_ID_LENGTH)
    private String globalAssetId;

    @Override
    public String toString() {
        return globalAssetId;
    }

    /**
     * Custom deserializer from string
     */
    /* package */  static class DefinitionDeserializer extends JsonDeserializer<GlobalAssetIdentification> {
        @Override
        public GlobalAssetIdentification deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext)
                throws IOException {
            return GlobalAssetIdentification.of(jsonParser.readValueAs(String.class));
        }
    }

}

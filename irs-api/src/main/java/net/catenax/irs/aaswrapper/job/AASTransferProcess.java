//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper.job;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.catenax.irs.connector.job.TransferProcess;

/**
 * Transfer Process for AAS Objects
 */
@Getter
@JsonDeserialize(using = AASTransferProcess.AASTransferProcessDeserializer.class)
@RequiredArgsConstructor
public class AASTransferProcess implements TransferProcess {

    @Getter(AccessLevel.NONE)
    private final String transferProcessId;

    private final List<String> idsToProcess = new ArrayList<>();

    private final Integer depth;

    public void addIdsToProcess(final List<String> childIds) {
        idsToProcess.addAll(childIds);
    }

    @Override
    public String getId() {
        return transferProcessId;
    }

    /**
     * Deserializer for AASTransferProcess
     */
    public static class AASTransferProcessDeserializer extends JsonDeserializer<AASTransferProcess> {

        @Override
        public AASTransferProcess deserialize(final JsonParser jsonParser,
                final DeserializationContext deserializationContext) throws IOException {
            final ObjectCodec codec = jsonParser.getCodec();
            final JsonNode treeNode = codec.readTree(jsonParser);
            final String idValue = treeNode.get("id").textValue();
            return new AASTransferProcess(idValue, null);
        }
    }
}

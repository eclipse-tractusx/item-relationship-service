package org.eclipse.tractusx.irs.edc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import org.eclipse.dataspaceconnector.spi.types.domain.edr.EndpointDataReference;

@Service
public class EndpointDataReferenceStorage {

    Map<String, EndpointDataReference> storageMap = null;

    public void put(String contractAgreementId, EndpointDataReference dataReference) {
        if (storageMap == null) {
            storageMap = new ConcurrentHashMap<>();
        }
        storageMap.put(contractAgreementId, dataReference);
    }

    public EndpointDataReference get(String contractAgreementId) {
        if (!storageMap.containsKey(contractAgreementId)) {
            throw new RuntimeException();
        }
        return storageMap.get(contractAgreementId);
    }

}

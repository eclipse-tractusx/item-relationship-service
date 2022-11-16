package org.eclipse.tractusx.irs.edc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.dataspaceconnector.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.IrsApplication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(IrsApplication.API_PREFIX)
@RequiredArgsConstructor
public class EdcCallbackController {

    private final EndpointDataReferenceStorage storage;

    @PostMapping("/endpoint-data-reference")
    public void receiveEdcCallback(EndpointDataReference dataReference) {
        var contractAgreementId = dataReference.getProperties().get("cid");
        storage.put(contractAgreementId, dataReference);
        log.info("Endpoint Data Reference received and cached for agreement: {}", contractAgreementId);
    }

}

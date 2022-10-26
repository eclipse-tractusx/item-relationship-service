package org.eclipse.tractusx.irs.edc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.edc.model.TransferProcessId;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("api/edc")
@RequiredArgsConstructor
public class EdcRequestTestApi {

    private final ContractNegotiationFacade contractNegotiationFacade;

    @GetMapping("/{assetId}")
    public TransferProcessId contractNegotiation(@PathVariable String assetId) {
        return contractNegotiationFacade.negotiate(assetId);
    }



}

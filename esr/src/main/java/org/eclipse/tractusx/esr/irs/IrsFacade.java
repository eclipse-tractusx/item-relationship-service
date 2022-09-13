package org.eclipse.tractusx.esr.irs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Public API Facade for IRS domain
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class IrsFacade {

    private IrsClient irsClient;

    public IrsResponse getIrsResponse(String globalAssetId, String bomLifecycle, String authorizationToken) {
        StartJobResponse response = irsClient.startJob(
                IrsRequest.builder().globalAssetId(globalAssetId).bomLifecycle(bomLifecycle).build(),
                authorizationToken);
        return irsClient.getJobDetails(response.getJobId(), authorizationToken);
    }

}

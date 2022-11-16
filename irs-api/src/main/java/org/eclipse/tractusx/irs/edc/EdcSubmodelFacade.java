package org.eclipse.tractusx.irs.edc;

import java.util.Collections;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.dataspaceconnector.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.aaswrapper.submodel.domain.RelationshipAspect;
import org.eclipse.tractusx.irs.aaswrapper.submodel.domain.RelationshipSubmodel;
import org.eclipse.tractusx.irs.component.Relationship;
import org.eclipse.tractusx.irs.edc.model.NegotiationResponse;
import org.eclipse.tractusx.irs.util.JsonUtil;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EdcSubmodelFacade {

    private final ContractNegotiationService contractNegotiationService;
    private final EdcDataPlaneClient edcDataPlaneClient;
    private final EndpointDataReferenceStorage endpointDataReferenceStorage;
    private final JsonUtil jsonUtil;

    public List<Relationship> getRelationships(final String submodelEndpointAddress, final RelationshipAspect traversalAspectType)
            throws InterruptedException {

        final String submodel = "/submodel";
        final int indexOfUrn = findIndexOf(submodelEndpointAddress, "/urn");
        final int indexOfSubModel = findIndexOf(submodelEndpointAddress, submodel);

        if (indexOfUrn == -1 || indexOfSubModel == -1) {
            throw new IllegalArgumentException("Cannot rewrite endpoint address, malformed format: " + submodelEndpointAddress);
        }

        final String providerConnectorUrl = submodelEndpointAddress.substring(0, indexOfUrn);
        final String target = submodelEndpointAddress.substring(indexOfUrn, indexOfSubModel);

        NegotiationResponse negotiationResponse = contractNegotiationService.negotiate(providerConnectorUrl, target);

        EndpointDataReference dataReference = null;
        // need to add timeout break
        while (dataReference == null) {
            Thread.sleep(1000);
            dataReference = endpointDataReferenceStorage.get(negotiationResponse.getContractAgreementId());
        }

        String data = edcDataPlaneClient.getData(dataReference, submodel);

        // need to extract List<Relationship> here
        final RelationshipSubmodel relationshipSubmodel = jsonUtil.fromString(data, traversalAspectType.getSubmodelClazz());

        return Collections.emptyList();

    }

    private int findIndexOf(final String endpointAddress, final String str) {
        return endpointAddress.indexOf(str);
    }

}

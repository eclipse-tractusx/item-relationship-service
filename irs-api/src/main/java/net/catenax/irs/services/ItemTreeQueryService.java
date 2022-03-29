package net.catenax.irs.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.aaswrapper.AASWrapperClient;
import net.catenax.irs.aaswrapper.AASWrapperClientLocalStub;
import net.catenax.irs.aaswrapper.registry.domain.AssetAdministrationShellDescriptor;
import net.catenax.irs.aspectmodels.AspectModel;
import net.catenax.irs.aspectmodels.AspectModelTypes;
import net.catenax.irs.aspectmodels.assemblypartrelationship.AssemblyPartRelationship;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemTreeQueryService {

    private final AASWrapperClient aasWrapperClient;

    @Getter
    private List<AspectModel> aspectModels = new ArrayList<>();

    public String getItemTree(final String assedId) {
        // createNewJob();
        // startJob();

        final AssetAdministrationShellDescriptor assetAdministrationShellDescriptor = aasWrapperClient.getAssetAdministrationShellDescriptor(
                assedId);

        getSubmodelForAspectModel(assetAdministrationShellDescriptor, AspectModelTypes.ASSEMBLY_PART_RELATIONSHIP);

        return "Job started.";
    }

    public void getSubmodelForAspectModel(final AssetAdministrationShellDescriptor input,
            final AspectModelTypes aspectModel) {
        input.getSubmodelDescriptors()
             .stream()
             .filter(submodelDescriptor -> submodelDescriptor.getIdShort().equalsIgnoreCase(aspectModel.getValue()))
             .forEach(submodelDescriptor -> submodelDescriptor.getEndpoints()
                                                              .forEach(endpoint -> getSubmodel(
                                                                      endpoint.getProtocolInformation()
                                                                              .getEndpointAddress(), aspectModel)));
    }

    public List<String> getEndpointsForAspectModel(
            final AssetAdministrationShellDescriptor assetAdministrationShellDescriptor,
            final AspectModelTypes aspectModel) {
        final List<String> endpoints = new ArrayList<>();
        assetAdministrationShellDescriptor.getSubmodelDescriptors()
                                          .stream()
                                          .filter(submodelDescriptor -> submodelDescriptor.getIdShort()
                                                                                          .equalsIgnoreCase(
                                                                                                  aspectModel.getValue()))
                                          .forEach(submodelDescriptor -> endpoints.addAll(
                                                  submodelDescriptor.getEndpoints()
                                                                    .stream()
                                                                    .map(endpoint -> endpoint.getProtocolInformation()
                                                                                             .getEndpointAddress())
                                                                    .collect(Collectors.toList())));
        return endpoints;
    }

    public AssemblyPartRelationship getSubmodel(final String endpointAddress, final AspectModelTypes aspectModel) {
        log.info(endpointAddress);

        final AssemblyPartRelationship submodel = (AssemblyPartRelationship) aasWrapperClient.getSubmodel(
                endpointAddress, aspectModel);

        log.info(submodel.toString());
        aspectModels.add(submodel);
        return submodel;
    }

}

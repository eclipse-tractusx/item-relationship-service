package net.catenax.irs.aaswrapper.registry.domain;

import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import net.catenax.irs.aaswrapper.dto.SubmodelEndpoint;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class DigitalTwinRegistryFacade {

   private final DigitalTwinRegistryClient digitalTwinRegistryClient;

   /**
    * Combines required data from Digital Twin Registry Service
    * @param aasIdentifier The Asset Administration Shell’s unique id
    * @return list of submodel addresses
    */
   public List<SubmodelEndpoint> getAASSubmodelEndpointAddresses(final String aasIdentifier) {
      final List<SubmodelDescriptor> submodelDescriptors = digitalTwinRegistryClient.getAssetAdministrationShellDescriptor(aasIdentifier).getSubmodelDescriptors();

      return submodelDescriptors.stream()
                                .map(submodelDescriptor -> new SubmodelEndpoint(submodelDescriptor.getEndpoints().get(0).getProtocolInformation().getEndpointAddress()))
                                .collect(Collectors.toList());
   }
}

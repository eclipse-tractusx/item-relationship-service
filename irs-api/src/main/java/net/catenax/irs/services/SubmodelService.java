package net.catenax.irs.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.catenax.irs.aaswrapper.submodel.domain.AssemblyPartRelationship;
import net.catenax.irs.aaswrapper.submodel.domain.ClientBuilder;
import net.catenax.irs.aaswrapper.submodel.domain.SubmodelClient;

import org.springframework.stereotype.Service;

/**
 * Service for retrieving the submodel json from a remote api
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubmodelService {

   public AssemblyPartRelationship retrieveSubmodel(final String uri) {
      AssemblyPartRelationship assemblyPartRelationship = null;

      try {
         final SubmodelClient client = ClientBuilder.createClient(SubmodelClient.class, uri);
         if (client != null) {
            assemblyPartRelationship = client.getSubmodel("", "", "");
            log.debug("assemblyPartRelationship {}", assemblyPartRelationship.toString());
         }
      } catch (Exception e) {
         log.error("Unable to retrieve AssemblyPartRelationship message {}", e.getMessage());
      }
      return assemblyPartRelationship;
   }
}

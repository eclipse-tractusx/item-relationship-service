package net.catenax.irs.aaswrapper.submodel.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class SubmodelFacade {

   private final SubmodelClient submodelClient;

   public Object getDataRequiredONLYToDoOurBusiness() {
      return "";
   }
}

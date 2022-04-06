//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper.submodel.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Public API Facade for submodel domain
 */
@RequiredArgsConstructor
@Service
public class SubmodelFacade {

    private final SubmodelClient submodelClient;

   /**
    *
    * @return xx
    */
    public Object getDataRequiredONLYToDoOurBusiness() {
        return "";
    }
}

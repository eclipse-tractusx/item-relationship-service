//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.component;

import java.util.List;


import io.swagger.v3.oas.annotations.media.Schema;


/**
 * SubmodelDescriptor
 */

public abstract class BaseDescriptor {

   @Schema(implementation = String.class)
   private String identification;

   @Schema(implementation = String.class)
   private String idShort;

   @Schema(implementation = Description.class)
   private List<Description> descriptions;



}

//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.requests.IrsPartsTreeRequest;
import net.catenax.irs.component.IrsPartRelationshipsWithInfos;
import org.springframework.stereotype.Service;

/**
 * Service for retrieving parts tree.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IrsPartsTreeQueryService {

   public IrsPartRelationshipsWithInfos registerItemJob(IrsPartsTreeRequest request) {
      return IrsPartRelationshipsWithInfos.builder().build();
   }

}

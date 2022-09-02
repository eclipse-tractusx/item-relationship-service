//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package org.eclipse.tractusx.irs.aaswrapper.job;

import java.util.List;
import java.util.Set;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.tractusx.irs.component.Bpn;
import org.eclipse.tractusx.irs.component.Relationship;
import org.eclipse.tractusx.irs.component.Submodel;
import org.eclipse.tractusx.irs.component.Tombstone;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;

/**
 * Container class to store item data
 */
@Getter
@Builder
@Jacksonized
public class ItemContainer {

    @Singular
    private List<Relationship> relationships;

    @Singular
    private List<Tombstone> tombstones;

    @Singular
    private List<AssetAdministrationShellDescriptor> shells;

    @Singular
    private List<Submodel> submodels;

    @Singular
    private Set<Bpn> bpns;
}

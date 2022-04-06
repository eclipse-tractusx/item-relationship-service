//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.jobs;

import net.catenax.irs.connector.job.TransferProcess;

public class AASTransferProcess implements TransferProcess {

    private final String id;

    public AASTransferProcess(final String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }
}

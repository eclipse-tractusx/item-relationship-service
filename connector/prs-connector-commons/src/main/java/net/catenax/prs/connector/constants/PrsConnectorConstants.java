//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.prs.connector.constants;

import net.catenax.prs.annotations.ExcludeFromCodeCoverageGeneratedReport;

/**
 * Constants shared across PRS Connector Consumer and Provider.
 */
@ExcludeFromCodeCoverageGeneratedReport
public class PrsConnectorConstants {
    /**
     * EDC Asset ID representing a request to the PRS API.
     */
    public static final String PRS_REQUEST_ASSET_ID = "prs-request";
    /**
     * EDC Policy ID used for PRS requests.
     */
    public static final String PRS_REQUEST_POLICY_ID = "use-eu";
    /**
     * EDC DataRequest property representing the PRS request payload.
     */
    public static final String DATA_REQUEST_PRS_REQUEST_PARAMETERS = "prs-request-parameters";
    /**
     * EDC DataRequest property representing the blob name to be created.
     */
    public static final String DATA_REQUEST_PRS_DESTINATION_PATH = "prs-destination-path";
}

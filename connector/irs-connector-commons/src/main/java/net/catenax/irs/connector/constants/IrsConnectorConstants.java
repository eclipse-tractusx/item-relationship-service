//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.connector.constants;

import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;

/**
 * Constants shared across IRS Connector Consumer and Provider.
 */
@ExcludeFromCodeCoverageGeneratedReport
public class IrsConnectorConstants {
    /**
     * EDC Asset ID representing a request to the IRS API.
     */
    public static final String IRS_REQUEST_ASSET_ID = "irs-request";
    /**
     * EDC Policy ID used for IRS requests.
     */
    public static final String IRS_REQUEST_POLICY_ID = "use-eu";
    /**
     * EDC DataRequest property representing the IRS request payload.
     */
    public static final String DATA_REQUEST_IRS_REQUEST_PARAMETERS = "irs-request-parameters";
    /**
     * EDC DataRequest property representing the blob name to be created.
     */
    public static final String DATA_REQUEST_IRS_DESTINATION_PATH = "irs-destination-path";
}

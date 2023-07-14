package org.eclipse.tractusx.irs.edc.client;

import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;

public class ItemNotFoundInCatalogException extends EdcClientException {
    public ItemNotFoundInCatalogException(final String endpoint, final String itemId) {
        super("Catalog for endpoint '" + endpoint + "' did not contain asset with id '" + itemId + "'");
    }
}

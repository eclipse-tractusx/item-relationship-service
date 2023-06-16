package org.eclipse.tractusx.irs.policystore.exceptions;

public class PolicyStoreException extends RuntimeException {

    public PolicyStoreException(final String msg) {
        super(msg);
    }

    public PolicyStoreException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}

package org.eclipse.tractusx.esr.irs;

import java.util.function.Predicate;

public class JobIsRunning implements Predicate<IrsResponse> {
    @Override
    public boolean test(IrsResponse irsResponse) {
        return irsResponse.isRunning();
    }
}
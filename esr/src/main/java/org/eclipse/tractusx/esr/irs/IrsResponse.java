package org.eclipse.tractusx.esr.irs;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
public class IrsResponse {

    JobStatus jobStatus;
    List<IrsRelationship> relationships;

    public boolean isRunning()  {
        return "RUNNING".equals(jobStatus.getJobState());
    }
}

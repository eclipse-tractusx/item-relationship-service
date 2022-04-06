package net.catenax.irs.jobs;

import lombok.Value;
import net.catenax.irs.connector.job.DataRequest;

@Value
public class ItemDataRequest implements DataRequest {

    final String itemId;

}

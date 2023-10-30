package org.eclipse.tractusx.irs.component;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class Notification {

    String notificationId;
    String bpn;
}

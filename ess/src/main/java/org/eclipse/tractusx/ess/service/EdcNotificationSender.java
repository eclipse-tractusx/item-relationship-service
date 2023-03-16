package org.eclipse.tractusx.ess.service;

import java.util.Map;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.edc.EdcSubmodelFacade;
import org.eclipse.tractusx.edc.exceptions.EdcClientException;
import org.eclipse.tractusx.edc.model.notification.EdcNotification;
import org.eclipse.tractusx.edc.model.notification.EdcNotificationHeader;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
public class EdcNotificationSender {

    private final EdcSubmodelFacade edcSubmodelFacade;

    public void sendEdcNotification(EdcNotification originalEdcNotification, SupplyChainImpacted supplyChainImpacted) {
        try {
            final String notificationId = UUID.randomUUID().toString();
            final String originalNotificationId = originalEdcNotification.getHeader().getNotificationId();
            final String senderEdc = ServletUriComponentsBuilder.fromCurrentRequest().build().toUriString();
            final String senderBpn = originalEdcNotification.getHeader().getRecipientBpn();
            final String recipientBpn = originalEdcNotification.getHeader().getSenderBpn();
            final String recipientUrl = originalEdcNotification.getHeader().getSenderEdc();
            final Map<String, Object> notificationContent = Map.of("result", supplyChainImpacted.getDescription());

            final EdcNotification edcRequest = edcRequest(notificationId, originalNotificationId, senderEdc, senderBpn,
                    recipientBpn, notificationContent);

            final var response = edcSubmodelFacade.sendNotification(recipientUrl, "ess-response-asset", edcRequest);
            if (!response.deliveredSuccessfully()) {
                throw new EdcClientException("EDC Provider did not accept message with notificationId " + notificationId);
            }
            log.info("Successfully sent response notification.");
        } catch (EdcClientException exception) {
            log.error("Cannot send edc notification", exception);
        }
    }

    private EdcNotification edcRequest(final String notificationId, final String originalId, final String senderEdc,
            final String senderBpn, final String recipientBpn, final Map<String, Object> content) {
        final EdcNotificationHeader header = EdcNotificationHeader.builder()
                                                                  .notificationId(notificationId)
                                                                  .senderEdc(senderEdc)
                                                                  .senderBpn(senderBpn)
                                                                  .recipientBpn(recipientBpn)
                                                                  .originalNotificationId(originalId)
                                                                  .replyAssetId("")
                                                                  .replyAssetSubPath("")
                                                                  .notificationType("ess-supplier-response")
                                                                  .build();

        return EdcNotification.builder().header(header).content(content).build();
    }

}

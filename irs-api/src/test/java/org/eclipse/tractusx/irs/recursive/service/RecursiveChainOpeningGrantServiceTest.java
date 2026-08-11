/********************************************************************************
 * Copyright (c) 2026 Volkswagen AG
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ********************************************************************************/
package org.eclipse.tractusx.irs.recursive.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.tractusx.irs.recursive.model.RecursiveChainOpeningGrant;
import org.eclipse.tractusx.irs.recursive.model.RecursiveChainOpeningGrantKey;
import org.eclipse.tractusx.irs.recursive.model.RecursiveUseCase;
import org.eclipse.tractusx.irs.recursive.store.InMemoryRecursiveChainOpeningGrantStore;
import org.eclipse.tractusx.irs.recursive.store.RecursiveChainOpeningGrantStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RecursiveChainOpeningGrantServiceTest {

    private static final String OPENING_ID = "opening-42";
    private static final RecursiveUseCase USE_CASE = RecursiveUseCase.PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE;
    private static final String REQUESTER_BPN = "BPNL0000ATLS0001";
    private static final String GLOBAL_ASSET_ID = "urn:uuid:68904173-ad59-4a77-8412-3e73fcafbd8b";
    private static final String PLAIN_GLOBAL_ASSET_ID = "68904173-ad59-4a77-8412-3e73fcafbd8b";

    private RecursiveChainOpeningGrantService service;
    private RecursiveChainOpeningGrantStore store;

    @BeforeEach
    void setUp() {
        store = new InMemoryRecursiveChainOpeningGrantStore();
        service = new RecursiveChainOpeningGrantService(store);
    }

    @Test
    void shouldValidateExistingGrant() {
        store.store(validGrant());

        final RecursiveChainOpeningGrant result = service.getActiveGrant(
                OPENING_ID, USE_CASE, REQUESTER_BPN, GLOBAL_ASSET_ID);

        assertThat(result.getOpeningId()).isEqualTo(OPENING_ID);
    }

    @Test
    void shouldAddAuditMetadataWhenRegisteringGrant() {
        final RecursiveChainOpeningGrant result = service.registerGrant(validGrant());

        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isAfterOrEqualTo(result.getCreatedAt());
        assertThat(store.find(RecursiveChainOpeningGrantKey.of(result)))
                .get()
                .extracting(RecursiveChainOpeningGrant::getUpdatedAt)
                .isEqualTo(result.getUpdatedAt());
    }

    @Test
    void shouldRejectDuplicateGrantRegistration() {
        service.registerGrant(validGrant());

        assertThatThrownBy(() -> service.registerGrant(validGrant()))
                .isInstanceOf(RecursiveChainOpeningGrantAlreadyExistsException.class);
    }

    @Test
    void shouldStoreGrantWithCanonicalAssetId() {
        final RecursiveChainOpeningGrant result = service.registerGrant(validGrant().toBuilder()
                .globalAssetId(PLAIN_GLOBAL_ASSET_ID)
                .build());

        assertThat(result.getGlobalAssetId()).isEqualTo(GLOBAL_ASSET_ID);
        assertThat(store.find(RecursiveChainOpeningGrantKey.of(validGrant()))).isPresent();
    }

    @Test
    void shouldMatchGrantWhenAssetIdUsesDifferentSupportedFormat() {
        service.registerGrant(validGrant().toBuilder()
                .globalAssetId(PLAIN_GLOBAL_ASSET_ID)
                .build());

        final RecursiveChainOpeningGrant result = service.getActiveGrant(
                OPENING_ID, USE_CASE, REQUESTER_BPN, GLOBAL_ASSET_ID);

        assertThat(result.getGlobalAssetId()).isEqualTo(GLOBAL_ASSET_ID);
    }

    @Test
    void shouldRejectMissingUseCaseAtGrantBoundaries() {
        final RecursiveChainOpeningGrant unknownUseCaseGrant = validGrant().toBuilder()
                .useCase(null)
                .build();

        assertThatThrownBy(() -> service.registerGrant(unknownUseCaseGrant))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("useCase must be provided");
        assertThatThrownBy(() -> service.replaceGrant(unknownUseCaseGrant))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("useCase must be provided");
        assertThatThrownBy(() -> service.getActiveGrant(
                OPENING_ID, null, REQUESTER_BPN, GLOBAL_ASSET_ID))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("useCase must be provided");
        assertThat(store.findAll()).isEmpty();
    }

    @Test
    void shouldAllowSeparateGrantsPerRequesterAndAssetForSameOpening() {
        service.registerGrant(validGrant());
        service.registerGrant(validGrant().toBuilder()
                .requesterBpn("BPNL0000BELF0001")
                .build());
        service.registerGrant(validGrant().toBuilder()
                .globalAssetId("urn:uuid:22222222-2222-2222-2222-222222222222")
                .build());

        assertThat(service.findGrants(OPENING_ID, null, null, null, true)).hasSize(3);
        assertThat(service.getActiveGrant(OPENING_ID, USE_CASE, "BPNL0000BELF0001", GLOBAL_ASSET_ID)
                .getRequesterBpn()).isEqualTo("BPNL0000BELF0001");
        assertThat(service.getActiveGrant(OPENING_ID, USE_CASE, REQUESTER_BPN,
                "urn:uuid:22222222-2222-2222-2222-222222222222").getRequesterBpn()).isEqualTo(REQUESTER_BPN);
    }

    @Test
    void shouldPreserveCreatedAtWhenReplacingGrant() {
        final RecursiveChainOpeningGrant existing = service.registerGrant(validGrant());

        final RecursiveChainOpeningGrant replacement = validGrant().toBuilder()
                .allowedBpnlSet(Set.of("BPNL0000BELF0001"))
                .build();
        final RecursiveChainOpeningGrant result = service.replaceGrant(replacement);

        assertThat(result.getCreatedAt()).isEqualTo(existing.getCreatedAt());
        assertThat(result.getUpdatedAt()).isAfterOrEqualTo(existing.getUpdatedAt());
        assertThat(result.getAllowedBpnlSet()).containsExactly("BPNL0000BELF0001");
    }

    @Test
    void shouldRejectWhenNoGrantExists() {
        assertThatThrownBy(() ->
                service.getActiveGrant("unknown", USE_CASE, REQUESTER_BPN, GLOBAL_ASSET_ID))
                .isInstanceOf(RecursiveChainOpeningGrantInactiveException.class)
                .hasMessageContaining("No grant found");
    }

    @Test
    void shouldRejectExpiredGrant() {
        store.store(validGrant().toBuilder()
                .validFrom(ZonedDateTime.now().minusHours(10))
                .validTo(ZonedDateTime.now().minusHours(1))
                .build());

        assertThatThrownBy(() ->
                service.getActiveGrant(OPENING_ID, USE_CASE, REQUESTER_BPN, GLOBAL_ASSET_ID))
                .isInstanceOf(RecursiveChainOpeningGrantInactiveException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void shouldListOnlyValidGrantsWhenRequested() {
        store.store(validGrant());
        store.store(validGrant().toBuilder()
                .useCase(USE_CASE)
                .requesterBpn("BPNL0000EXPR0001")
                .validFrom(ZonedDateTime.now().minusHours(10))
                .validTo(ZonedDateTime.now().minusHours(1))
                .build());

        final List<RecursiveChainOpeningGrant> result = service.findGrants(OPENING_ID, null, null, null, true);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUseCase()).isEqualTo(USE_CASE);
    }

    @Test
    void shouldListAllGrantsWhenValidOnlyIsFalse() {
        store.store(validGrant());
        store.store(validGrant().toBuilder()
                .useCase(USE_CASE)
                .requesterBpn("BPNL0000EXPR0001")
                .validFrom(ZonedDateTime.now().minusHours(10))
                .validTo(ZonedDateTime.now().minusHours(1))
                .build());

        final List<RecursiveChainOpeningGrant> result = service.findGrants(OPENING_ID, null, null, null, false);

        assertThat(result).hasSize(2);
    }

    @Test
    void shouldFilterGrantListByRequesterAndAsset() {
        store.store(validGrant());
        store.store(validGrant().toBuilder().requesterBpn("BPNL0000BELF0001").build());
        store.store(validGrant().toBuilder()
                .globalAssetId("urn:uuid:22222222-2222-2222-2222-222222222222")
                .build());

        assertThat(service.findGrants(OPENING_ID, null, REQUESTER_BPN, null, true)).hasSize(2);
        assertThat(service.findGrants(OPENING_ID, GLOBAL_ASSET_ID, null, null, true)).hasSize(2);
        assertThat(service.findGrants(OPENING_ID, PLAIN_GLOBAL_ASSET_ID, null, null, true)).hasSize(2);
        assertThat(service.findGrants(OPENING_ID, GLOBAL_ASSET_ID, "BPNL0000BELF0001", null, true)).hasSize(1);
    }

    @Test
    void shouldFindGrantByFullKeyWithoutScanningAllGrants() {
        final RecursiveChainOpeningGrantStore grantStore = mock(RecursiveChainOpeningGrantStore.class);
        final RecursiveChainOpeningGrantService grantService = new RecursiveChainOpeningGrantService(grantStore);
        final RecursiveChainOpeningGrant grant = validGrant();
        final RecursiveChainOpeningGrantKey key = RecursiveChainOpeningGrantKey.of(grant);
        when(grantStore.find(key)).thenReturn(Optional.of(grant));

        final List<RecursiveChainOpeningGrant> result = grantService.findGrants(
                OPENING_ID, GLOBAL_ASSET_ID, REQUESTER_BPN, USE_CASE, true);

        assertThat(result).containsExactly(grant);
        verify(grantStore).find(key);
        verify(grantStore, never()).findAll();
    }

    @Test
    void shouldRejectGrantNotYetValid() {
        store.store(validGrant().toBuilder()
                .validFrom(ZonedDateTime.now().plusHours(1))
                .validTo(ZonedDateTime.now().plusHours(10))
                .build());

        assertThatThrownBy(() ->
                service.getActiveGrant(OPENING_ID, USE_CASE, REQUESTER_BPN, GLOBAL_ASSET_ID))
                .isInstanceOf(RecursiveChainOpeningGrantInactiveException.class)
                .hasMessageContaining("not yet valid");
    }

    @Test
    void shouldRejectWhenRequesterDoesNotMatchAnyGrant() {
        store.store(validGrant());

        assertThatThrownBy(() ->
                service.getActiveGrant(OPENING_ID, USE_CASE, "BPNL0000OTHER0001", GLOBAL_ASSET_ID))
                .isInstanceOf(RecursiveChainOpeningGrantInactiveException.class)
                .hasMessageContaining("No grant found");
    }

    @Test
    void shouldRejectWhenAssetDoesNotMatchAnyGrant() {
        store.store(validGrant());

        assertThatThrownBy(() ->
                service.getActiveGrant(OPENING_ID, USE_CASE, REQUESTER_BPN,
                        "urn:uuid:22222222-2222-2222-2222-222222222222"))
                .isInstanceOf(RecursiveChainOpeningGrantInactiveException.class)
                .hasMessageContaining("No grant found");
    }

    @Test
    void shouldDeleteGrantByFullKey() {
        store.store(validGrant());

        assertThat(service.deleteGrant(OPENING_ID, PLAIN_GLOBAL_ASSET_ID, REQUESTER_BPN, USE_CASE)).isTrue();
        assertThat(service.deleteGrant(OPENING_ID, GLOBAL_ASSET_ID, REQUESTER_BPN, USE_CASE)).isFalse();
    }

    @Test
    void shouldNotDeleteMissingGrant() {
        final RecursiveChainOpeningGrantStore grantStore = mock(RecursiveChainOpeningGrantStore.class);
        final RecursiveChainOpeningGrantService grantService = new RecursiveChainOpeningGrantService(grantStore);
        final RecursiveChainOpeningGrantKey key = new RecursiveChainOpeningGrantKey(OPENING_ID, GLOBAL_ASSET_ID,
                REQUESTER_BPN, USE_CASE);
        when(grantStore.find(key)).thenReturn(java.util.Optional.empty());

        assertThat(grantService.deleteGrant(OPENING_ID, GLOBAL_ASSET_ID, REQUESTER_BPN, USE_CASE)).isFalse();
        verify(grantStore, never()).remove(key);
    }

    @Test
    void shouldFilterPartnersAgainstAllowList() {
        final RecursiveChainOpeningGrant grant = validGrant();

        final Set<String> bomCandidates = Set.of(
                "BPNL0000BELF0001", "BPNL0000CERS0001", "BPNL0000DLTA0001", "BPNL_NOT_ALLOWED");

        final Set<String> allowed = service.filterAllowedPartners(bomCandidates, grant);

        // Only Belfast, Ceres, Delta are in the grant's allow-list
        assertThat(allowed).containsExactlyInAnyOrder(
                "BPNL0000BELF0001", "BPNL0000CERS0001", "BPNL0000DLTA0001");
        assertThat(allowed).doesNotContain("BPNL_NOT_ALLOWED");
    }

    @Test
    void shouldReturnEmptyWhenNoIntersection() {
        final RecursiveChainOpeningGrant grant = validGrant();

        final Set<String> bomCandidates = Set.of("BPNL_UNKNOWN_1", "BPNL_UNKNOWN_2");

        final Set<String> allowed = service.filterAllowedPartners(bomCandidates, grant);

        assertThat(allowed).isEmpty();
    }

    private RecursiveChainOpeningGrant validGrant() {
        return RecursiveChainOpeningGrant.builder()
                .openingId(OPENING_ID)
                .useCase(USE_CASE)
                .requesterBpn(REQUESTER_BPN)
                .globalAssetId(GLOBAL_ASSET_ID)
                .allowedBpnlSet(Set.of("BPNL0000BELF0001", "BPNL0000CERS0001", "BPNL0000DLTA0001"))
                .validFrom(ZonedDateTime.now().minusHours(1))
                .validTo(ZonedDateTime.now().plusHours(12))
                .build();
    }
}

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
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.recursive.store;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.eclipse.tractusx.irs.common.persistence.BlobPersistence;
import org.eclipse.tractusx.irs.common.persistence.BlobPersistenceException;
import org.eclipse.tractusx.irs.data.JsonParseException;
import org.eclipse.tractusx.irs.recursive.model.RecursiveChainOpeningGrant;
import org.eclipse.tractusx.irs.recursive.model.RecursiveChainOpeningGrantKey;
import org.eclipse.tractusx.irs.recursive.util.RecursiveLogValue;
import org.eclipse.tractusx.irs.util.JsonUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * MinIO / S3-backed implementation of {@link RecursiveChainOpeningGrantStore}.
 *
 * <h3>Blob key scheme</h3>
 * <pre>
 *   chain-opening-grant:{openingId}:{globalAssetId}:{requesterBpn}:{useCase}
 *       ->  JSON of RecursiveChainOpeningGrant
 * </pre>
 * Each key component is URL-encoded: {@code globalAssetId} is a {@code urn:uuid:...} URN and
 * therefore contains the separator character itself - encoding keeps the four components
 * unambiguous.
 *
 * <p>Stored in the dedicated chain-opening-grants blob bucket
 * ({@code irs-chain-opening-grants} by default), separate from regular IRS jobs.</p>
 *
 * <h3>Crash recovery</h3>
 * <p>Grants registered via the REST API
 * ({@code POST /irs/recursive/chain-openings/grants})
 * are persisted here and survive pod restarts.</p>
 */
@Slf4j
@RequiredArgsConstructor
public class BlobRecursiveChainOpeningGrantStore implements RecursiveChainOpeningGrantStore {

    /** Prefix for all grant blobs. */
    /* package */ static final String GRANT_PREFIX = "chain-opening-grant:";

    private final BlobPersistence blobPersistence;
    private final JsonUtil jsonUtil;

    @Override
    public void store(final RecursiveChainOpeningGrant grant) {
        final RecursiveChainOpeningGrantKey key = RecursiveChainOpeningGrantKey.of(grant);
        try {
            final String json = jsonUtil.asString(grant);
            blobPersistence.putBlob(toBlobKey(key), json.getBytes(StandardCharsets.UTF_8));
            log.debug("Stored grant: {}", RecursiveLogValue.of(key.toString()));
        } catch (final BlobPersistenceException | JsonParseException e) {
            throw new RecursiveStoreException("Failed to store recursive chain opening grant", e);
        }
    }

    @Override
    public Optional<RecursiveChainOpeningGrant> find(final RecursiveChainOpeningGrantKey key) {
        try {
            return blobPersistence.getBlob(toBlobKey(key))
                    .map(bytes -> jsonUtil.fromString(new String(bytes, StandardCharsets.UTF_8),
                            RecursiveChainOpeningGrant.class));
        } catch (final BlobPersistenceException | JsonParseException e) {
            throw new RecursiveStoreException("Failed to read recursive chain opening grant", e);
        }
    }

    @Override
    public List<RecursiveChainOpeningGrant> findAll() {
        try {
            final Collection<byte[]> blobs = RecursiveBlobStoreAccess.findBlobByPrefix(blobPersistence, GRANT_PREFIX);
            return blobs.stream()
                    .map(bytes -> jsonUtil.fromString(new String(bytes, StandardCharsets.UTF_8),
                            RecursiveChainOpeningGrant.class))
                    .toList();
        } catch (final BlobPersistenceException | JsonParseException e) {
            throw new RecursiveStoreException("Failed to list recursive chain opening grants", e);
        }
    }

    @Override
    public boolean remove(final RecursiveChainOpeningGrantKey key) {
        try {
            return blobPersistence.delete(toBlobKey(key), List.of());
        } catch (final BlobPersistenceException e) {
            throw new RecursiveStoreException("Failed to delete recursive chain opening grant", e);
        }
    }

    private static String toBlobKey(final RecursiveChainOpeningGrantKey key) {
        return GRANT_PREFIX + String.join(":", encode(key.openingId()), encode(key.globalAssetId()),
                encode(key.requesterBpn()), encode(key.useCase() == null ? null : key.useCase().name()));
    }

    private static String encode(final String value) {
        return value == null ? "" : URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}

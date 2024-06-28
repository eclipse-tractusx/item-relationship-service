/********************************************************************************
 * Copyright (c) 2022,2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 * Copyright (c) 2021,2024 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.irs.edc.client.model.edr;

import java.util.List;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

/**
 * CallbackAddress represents the properties of a callback address.
 *
 * @param uri           The URI of the callback.
 * @param events        The list of events for the callback.
 * @param transactional Indicates if the callback is transactional.
 * @param authKey       The authentication key.
 * @param authCode      The authentication code.
 */
@Builder
@Jacksonized
public record CallbackAddress(String uri, List<String> events, boolean transactional, String authKey, String authCode) {
}

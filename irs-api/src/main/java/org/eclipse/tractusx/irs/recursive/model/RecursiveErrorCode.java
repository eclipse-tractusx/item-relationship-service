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
package org.eclipse.tractusx.irs.recursive.model;

/** Stable error codes returned by the recursive APIs. */
public enum RecursiveErrorCode {
    INVALID_REQUEST,
    MALFORMED_REQUEST,
    UNSUPPORTED_ASPECT,
    CHAIN_OPENING_GRANT_REJECTED,
    NOTIFICATION_AUTHENTICATION_FAILED,
    AUTHORIZATION_FAILED,
    INVALID_NOTIFICATION,
    RECURSIVE_JOB_NOT_FOUND,
    CHAIN_OPENING_GRANT_NOT_FOUND,
    CHAIN_OPENING_GRANT_ALREADY_EXISTS,
    PERSISTENCE_UNAVAILABLE,
    RECURSIVE_ENDPOINT_NOT_FOUND,
    METHOD_NOT_ALLOWED,
    NOT_ACCEPTABLE,
    UNSUPPORTED_MEDIA_TYPE,
    INTERNAL_ERROR
}

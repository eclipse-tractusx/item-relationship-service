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
package org.eclipse.tractusx.irs.recursive.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logger for the audit lines of state-changing recursive API calls. Its appender and the
 * default level {@code off} come from the parent logger {@code org.eclipse.tractusx.irs.audit} in
 * {@code log4j2.xml}; operators enable it via {@code logging.level.org.eclipse.tractusx.irs.audit=INFO}.
 */
public final class RecursiveAuditLogger {

    public static final Logger LOG = LoggerFactory.getLogger("org.eclipse.tractusx.irs.audit.recursive");

    private RecursiveAuditLogger() {
    }
}

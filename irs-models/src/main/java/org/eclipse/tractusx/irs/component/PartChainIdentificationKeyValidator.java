/********************************************************************************
 * Copyright (c) 2021,2025 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.irs.component;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for PartChainIdentificationKey
 */
public class PartChainIdentificationKeyValidator implements
        ConstraintValidator<SingleIdentifierOnly, PartChainIdentificationKey> {

    @Override
    public boolean isValid(final PartChainIdentificationKey dto, final ConstraintValidatorContext context) {
        final boolean isField1Present = dto.getGlobalAssetId() != null && !dto.getGlobalAssetId().isEmpty();
        final boolean isField2Present = dto.getIdentifier() != null && !dto.getIdentifier().isEmpty();

        return isField1Present ^ isField2Present;
    }
}

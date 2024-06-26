/********************************************************************************
 * Copyright (c) 2022,2024
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2024: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
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
package org.eclipse.tractusx.irs.component;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.support.PagedListHolder;

/**
 * Paginated results for {@link JobStatusResult} content
 */
@Schema(example = PageResult.EXAMPLE)
public record PageResult(

        List<JobStatusResult> content,
        Integer pageNumber,
        Integer pageCount,
        Integer pageSize,
        Integer totalElements) {

    public static final String EXAMPLE = "{\"content\"=[{\"completedOn\"=\"test\", \"id\"=\"046b6c7f-0b8a-43b9-b35d-6489e6daee91\", \"startedOn\"=\"2000-01-23T04:56:07.000+00:00\", \"state\"=\"UNSAVED\"}], \"pageCount\"=0, \"pageNumber\"=6, \"pageSize\"=1, \"totalElements\"=5}";

    public PageResult(final PagedListHolder<JobStatusResult> pagedListHolder) {
        this(pagedListHolder.getPageList(), pagedListHolder.getPage(), pagedListHolder.getPageCount(), pagedListHolder.getPageSize(), pagedListHolder.getNrOfElements());
    }

}


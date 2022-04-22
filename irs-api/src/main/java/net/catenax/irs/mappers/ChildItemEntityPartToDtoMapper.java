//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.mappers;

import lombok.RequiredArgsConstructor;
import net.catenax.irs.component.Job;
import net.catenax.irs.entities.JobEntityPart;
import org.springframework.stereotype.Component;

/**
 * Mapper from {@link JobEntityPart} entity to {@link Job} DTO.
 */
@Component
@RequiredArgsConstructor
public class ChildItemEntityPartToDtoMapper {
    /**
     * Map a {@link JobEntityPart} entity into a {@link Job} DTO.
     *
     * @param source entity to map. Must not be {@literal null}.
     * @return DTO containing data from the entity. Guaranteed to be not {@literal null}.
     */
    public Job toJob(final Job source) {
        return Job.builder()
                .jobId(source.getJobId())
                .globalAssetId(source.getGlobalAssetId())
                .jobState(source.getJobState())
                .exception(source.getException())
                .createdOn(source.getCreatedOn())
                .lastModifiedOn(source.getLastModifiedOn())
                .jobCompleted(source.getJobCompleted())
                .requestUrl(source.getRequestUrl())
                .action(source.getAction())
                .owner(source.getOwner())
                .build();
    }
}

//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.repositories;

import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;
import net.catenax.irs.entities.JobEntityPart;
import net.catenax.irs.entities.SummaryAttributeEntity;
import net.catenax.irs.entities.SummaryAttributeEntityKey;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * JPA Repository for managing {@link SummaryAttributeEntity} objects.
 */
public interface PartAttributeRepository extends Repository<SummaryAttributeEntity, SummaryAttributeEntityKey> {
    /**
     * Returns all instances of the type {@link SummaryAttributeEntity}
     * for the given Part IDs and attribute name.
     * <p>
     * If some or all Part Ids are not found, no entities are returned for these IDs.
     * <p>
     * Note that the order of elements in the result is not guaranteed.
     *
     * @param partIds Part IDs to retrieve attribute for.
     *                Must not be {@literal null} nor contain any {@literal null} values.
     * @param name    Attribute name to retrieve. Must not be {@literal null}.
     * @return guaranteed to be not {@literal null}.
     * The size can be equal or less than the number of given {@literal partIds}.
     * @see org.springframework.data.repository.CrudRepository#findAllById(Iterable)
     */
    @Query("SELECT a FROM SummaryAttributeEntity a WHERE a.key.asynchFetchedItems IN (:partIds) AND a.key.attribute = :name")
    List<SummaryAttributeEntity> findAllBy(
            @Param("partIds")
            Collection<JobEntityPart> partIds,
            @Param("name")
            String name);

    List<SummaryAttributeEntity> findAll(Example<SummaryAttributeEntity> searchFilter, Sort sortedByOneid);
}

/**
 * Substitute repository implementation without DB
 */
@Component
@ExcludeFromCodeCoverageGeneratedReport
class PartAttributeRepositoryStub implements PartAttributeRepository {
    @Override
    public List<SummaryAttributeEntity> findAllBy(final Collection<JobEntityPart> partIds, final String name) {
        return new ArrayList<>();
    }

    @Override
    public List<SummaryAttributeEntity> findAll(final Example<SummaryAttributeEntity> searchFilter,
                                                final Sort sortedByOneid) {
        return new ArrayList<>();
    }
}


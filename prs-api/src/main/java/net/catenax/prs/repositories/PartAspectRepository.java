//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.prs.repositories;

import net.catenax.prs.entities.PartAspectEntity;
import net.catenax.prs.entities.PartAspectEntityKey;
import net.catenax.prs.entities.PartIdEntityPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

/**
 * JPA Repository for managing {@link PartAspectEntity} objects.
 */
public interface PartAspectRepository extends JpaRepository<PartAspectEntity, PartAspectEntityKey> {
    /**
     * Returns all instances of the type {@link PartAspectEntity} for the given Part IDs and aspect name.
     * <p>
     * If some or all Part IDs are not found, no entities are returned for these IDs.
     * <p>
     * Note that the order of elements in the result is not guaranteed.
     *
     * @param partIds Part IDs to retrieve aspect for.
     *                Must not be {@literal null} nor contain any {@literal null} values.
     * @param name    Aspect name to retrieve. Must not be {@literal null}.
     * @return guaranteed to be not {@literal null}.
     * The size can be equal or less than the number of given {@literal partIds}.
     * @see org.springframework.data.repository.CrudRepository#findAllById(Iterable)
     */
    @Query("SELECT a FROM PartAspectEntity a WHERE a.key.partId IN (:partIds) AND a.key.name = :name")
    List<PartAspectEntity> findAllBy(
            @Param("partIds")
            Collection<PartIdEntityPart> partIds,
            @Param("name")
            String name);
}

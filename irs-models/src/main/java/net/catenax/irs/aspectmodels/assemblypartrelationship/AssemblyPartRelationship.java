/*
 * Copyright (c) 2022 Robert Bosch Manufacturing Solutions GmbH, Germany. All rights reserved.
 */
package net.catenax.irs.aspectmodels.assemblypartrelationship;

import java.util.Objects;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.openmanufacturing.sds.aspectmodel.java.CollectionAspect;

/**
 * Generated class for Assembly Part Relationship. The aspect provides the child parts (one
 * structural level down) which the given object assembles.
 */
public class AssemblyPartRelationship implements CollectionAspect<Set<ChildData>, ChildData> {

  @NotNull
  @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
  private final String catenaXId;

  @NotNull private final Set<ChildData> childParts;

  @JsonCreator
  public AssemblyPartRelationship(
      @JsonProperty(value = "catenaXId") String catenaXId,
      @JsonProperty(value = "childParts") Set<ChildData> childParts) {
    this.catenaXId = catenaXId;
    this.childParts = childParts;
  }

  /**
   * Returns Catena-X Identifier
   *
   * @return {@link #catenaXId}
   */
  public String getCatenaXId() {
    return this.catenaXId;
  }
  /**
   * Returns Child Parts
   *
   * @return {@link #childParts}
   */
  public Set<ChildData> getChildParts() {
    return this.childParts;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final AssemblyPartRelationship that = (AssemblyPartRelationship) o;
    return Objects.equals(catenaXId, that.catenaXId) && Objects.equals(childParts, that.childParts);
  }

  @Override
  public String toString() {
    return "AssemblyPartRelationship{" + "catenaXId='" + catenaXId + '\'' + ", childParts=" + childParts + '}';
  }
}

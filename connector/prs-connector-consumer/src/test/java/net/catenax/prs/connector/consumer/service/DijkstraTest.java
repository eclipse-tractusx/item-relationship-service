package net.catenax.prs.connector.consumer.service;

import net.catenax.prs.client.model.PartId;
import net.catenax.prs.client.model.PartRelationship;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class DijkstraTest {

    static Stream<Arguments> shortestPathLengthArgs() {

        RequestMother generate = new RequestMother();
        PartId source = generate.partId();
        PartId target = generate.partId();
        PartId other = generate.partId();
        PartId another = generate.partId();

        return Stream.of(
                Arguments.of(Set.of(generate.relationship(source, source)), source, source, Optional.of(0))
                , Arguments.of(Set.of(), source, target, Optional.empty())
                , Arguments.of(Set.of(generate.relationship(source, target)), source, target, Optional.of(1))
                , Arguments.of(Set.of(generate.relationship(source, other)), source, target, Optional.empty())
                , Arguments.of(Set.of(generate.relationship(target, source)), source, target, Optional.empty())
                , Arguments.of(Set.of(
                        generate.relationship(source, other)
                        , generate.relationship(other, target)
                ), source, target, Optional.of(2))
                , Arguments.of(Set.of(
                        generate.relationship(source, other)
                        , generate.relationship(other, another)
                        , generate.relationship(another, target)
                        , generate.relationship(other, target)
                ), source, target, Optional.of(2))
                , Arguments.of(Set.of(
                        generate.relationship(source, other)
                        , generate.relationship(other, source)
                        , generate.relationship(other, target)
                ), source, target, Optional.of(2))
        );
    }

    @ParameterizedTest
    @MethodSource("shortestPathLengthArgs")
    void shortestPathLength(Set<PartRelationship> edges, PartId source, PartId target, Optional<Integer> expected) {
        assertThat(Dijkstra.shortestPathLength(edges, source, target))
                .isEqualTo(expected);
    }
}
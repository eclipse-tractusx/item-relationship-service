//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.connector.consumer.service;

import java.util.HashSet;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Collection;
import java.util.List;
import java.util.HashMap;
import java.util.Comparator;
import java.util.stream.Collectors;

import net.catenax.irs.component.ChildItem;
import net.catenax.irs.component.Relationship;

/**
 * Implementation of the Dijkstra algorithm for shortest path search.
 * Adapted from <a href="https://commons.apache.org/sandbox/commons-graph/">Apache Commons Graph</a>.
 */
public final class Dijkstra {
    private Dijkstra() {
    }

    /**
     * Return the shortest path length between two parts in a relationship graph.
     * <p>
     * Returns the shortest distance between {@literal source} and {@literal target} in the graph
     * described by {@literal edges}.
     *
     * @param edges  collection of edges in the graph.
     * @param source source part identifier.
     * @param target target part identifier.
     * @return distance, or {@link Optional#empty()} if no path is found.
     */
    public static Optional<Integer> shortestPathLength(final Collection<Relationship> edges, final ChildItem source,
            final ChildItem target) {
        if (edges.isEmpty()) {
            return Optional.empty();
        }

        final ShortestDistances shortestDistances = new ShortestDistances();

        shortestDistances.put(source, 0);

        final PriorityQueue<ChildItem> unsettledNodes = new PriorityQueue<>(edges.size(), shortestDistances);
        unsettledNodes.add(source);

        final Set<ChildItem> settledNodes = new HashSet<>();

        // extract the node with the shortest distance
        while (!unsettledNodes.isEmpty()) {
            final ChildItem vertex = unsettledNodes.poll();

            // destination reached, stop and build the path
            if (target.equals(vertex)) {
                return Optional.of(shortestDistances.get(target));
            }

            settledNodes.add(vertex);

            for (final Relationship edge : getEdges(edges, vertex)) {
                final ChildItem child = edge.getChildItem();
                // skip node already settled
                if (!settledNodes.contains(child)) {
                    final int shortDist = shortestDistances.get(vertex) + 1;

                    if (shortDist < shortestDistances.get(child)) {
                        // assign new shortest distance and mark unsettled
                        shortestDistances.put(child, shortDist);
                        unsettledNodes.add(child);
                    }
                }
            }
        }

        return Optional.empty();
    }

    private static List<Relationship> getEdges(final Collection<Relationship> edges, final ChildItem vertex) {
        return edges.stream().filter(g -> g.getChildItem().equals(vertex)).collect(Collectors.toList());

    }

    /**
     * {@link Comparator} for storing distances and retrieving shortest distances.
     */
    private static final class ShortestDistances extends HashMap<ChildItem, Integer> implements Comparator<ChildItem> {

        /**
         * {@inheritDoc}
         */
        @Override
        public Integer get(final Object key) {
            final Integer distance = super.get(key);
            return (distance == null) ? Integer.valueOf(Integer.MAX_VALUE) : distance;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int compare(final ChildItem left, final ChildItem right) {
            return get(left).compareTo(get(right));
        }

    }
}

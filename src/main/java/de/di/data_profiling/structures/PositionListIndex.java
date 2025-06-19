package de.di.data_profiling.structures;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public class PositionListIndex {

    private final AttributeList attributes;
    private final List<IntArrayList> clusters;
    private final int[] invertedClusters;

    public PositionListIndex(final AttributeList attributes, final String[] values) {
        this.attributes = attributes;
        this.clusters = this.calculateClusters(values);
        this.invertedClusters = this.calculateInverted(this.clusters, values.length);
    }

    public PositionListIndex(final AttributeList attributes, final List<IntArrayList> clusters, int relationLength) {
        this.attributes = attributes;
        this.clusters = clusters;
        this.invertedClusters = this.calculateInverted(this.clusters, relationLength);
    }

    private List<IntArrayList> calculateClusters(final String[] values) {
        Map<String, IntArrayList> invertedIndex = new HashMap<>(values.length);
        for (int recordIndex = 0; recordIndex < values.length; recordIndex++) {
            invertedIndex.putIfAbsent(values[recordIndex], new IntArrayList());
            invertedIndex.get(values[recordIndex]).add(recordIndex);
        }
        return invertedIndex.values().stream().filter(cluster -> cluster.size() > 1).collect(Collectors.toList());
    }

    private int[] calculateInverted(List<IntArrayList> clusters, int relationLength) {
        int[] invertedClusters = new int[relationLength];
        Arrays.fill(invertedClusters, -1);
        for (int clusterIndex = 0; clusterIndex < clusters.size(); clusterIndex++)
            for (int recordIndex : clusters.get(clusterIndex))
                invertedClusters[recordIndex] = clusterIndex;
        return invertedClusters;
    }

    public boolean isUnique() {
        return this.clusters.isEmpty();
    }

    public int relationLength() {
        return this.invertedClusters.length;
    }

    public PositionListIndex intersect(PositionListIndex other) {
        List<IntArrayList> clustersIntersection = this.intersect(this.clusters, other.getInvertedClusters());
        AttributeList attributesUnion = this.attributes.union(other.getAttributes());
//ringewashere
        return new PositionListIndex(attributesUnion, clustersIntersection, this.relationLength());
    }

    private List<IntArrayList> intersect(List<IntArrayList> clusters, int[] invertedClusters) {
        List<IntArrayList> clustersIntersection = new ArrayList<>();

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //                                      DATA INTEGRATION ASSIGNMENT                                           //
        // Calculate the intersection of one PLI's clusters and another PLI's (conveniently already inverted)         //
        // invertedClusters. The clustersIntersection is a new list that stores the intersection result. Note that    //
        // the clusters are "Stripped Partitions", which means that only clusters of size >1 are part of the result.  //

        // Map to hold intersection clusters using a composite key based on both cluster indices
        Map<Integer, Int2ObjectMap<IntArrayList>> tempClusters = new HashMap<>();

        for (int i = 0; i < clusters.size(); i++) {
            IntArrayList cluster = clusters.get(i);
            for (int recordId : cluster) {
                int otherClusterId = invertedClusters[recordId];
                if (otherClusterId == -1) continue; // Skip records that are not part of any cluster in other PLI

                tempClusters.putIfAbsent(i, new Int2ObjectArrayMap<>());
                Int2ObjectMap<IntArrayList> subMap = tempClusters.get(i);
                subMap.putIfAbsent(otherClusterId, new IntArrayList());
                subMap.get(otherClusterId).add(recordId);
            }
        }

        for (Int2ObjectMap<IntArrayList> subMap : tempClusters.values()) {
            for (IntArrayList intersectionCluster : subMap.values()) {
                if (intersectionCluster.size() > 1) {
                    clustersIntersection.add(intersectionCluster);
                }
            }
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        return clustersIntersection;
    }
}
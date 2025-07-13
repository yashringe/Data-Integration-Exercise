package de.di.duplicate_detection;

import de.di.Relation;
import de.di.duplicate_detection.structures.AttrSimWeight;
import de.di.duplicate_detection.structures.Duplicate;
import de.di.similarity_measures.Jaccard;
import de.di.similarity_measures.Levenshtein;
import de.di.similarity_measures.SimilarityMeasure;
import de.di.similarity_measures.helper.Tokenizer;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

public class SortedNeighborhood {

    // A Record class that stores the values of a record with its original index. This class helps to remember the
    // original index of a record when this record is being sorted.
    @Data
    @AllArgsConstructor
    private static class Record {
        private int index;
        private String[] values;
    }

    /**
     * Discovers all duplicates in the relation by running the Sorted Neighborhood Method once with every sortingKey.
     * Each run uses one of the specified sortingKeys for the sorting, the windowsSize for the windowing, and
     * the recordComparator for the similarity calculations. A pair of records is classified as a duplicate and the
     * corresponding record indexes are returned as a Duplicate object, if the similarity of the two records w.r.t.
     * the provided recordComparator is equal to or greater than the similarityThreshold.
     * @param relation The relation, in which duplicates should be detected.
     * @param sortingKeys The sorting keys that should be used; a sorting key corresponds to an attribute index, whose
     *                    lexicographical order should determine a sortation; every specificed sorting key korresponds
     *                    to one Sorted Neighborhood run and the union of all duplicates of all runs is the result of
     *                    the call.
     * @param windowSize The window size each Sorted Neighborhood run should use.
     * @param recordComparator The record comparator each Sorted Neighborhood run should use when comparing records.
     * @return The list of discovered duplicate pairs of all Sorted Neighborhood runs.
     */
    public Set<Duplicate> detectDuplicates(Relation relation, int[] sortingKeys, int windowSize, RecordComparator recordComparator) {
        Set<Duplicate> duplicates = new HashSet<>();

        Record[] records = new Record[relation.getRecords().length];
        for (int i = 0; i < relation.getRecords().length; i++)
            records[i] = new Record(i, relation.getRecords()[i]);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //                                      DATA INTEGRATION ASSIGNMENT                                           //
        // Discover all duplicates in the provided relation. A duplicate stores the attribute indexes that refer to   //
        // matching records. Use the provided sortingKeys, windowSize, and recordComparator to implement the Sorted   //
        // Neighborhood Method correctly.                                                                             //
        RecordComparator rc2 = suggestRecordComparatorFor(relation);
        for (int key : sortingKeys) {
            Arrays.sort(records, Comparator.comparing(o -> o.getValues()[key]));

            // Apply sliding window
            for (int i = 0; i < records.length - windowSize + 1; i++) {
                for (int j = i + 1; j < i + windowSize && j < records.length; j++) {
                    Record record1 = records[i];
                    Record record2 = records[j];
                    double similarity = rc2.compare(record1.getValues(), record2.getValues());
                    if (recordComparator.isDuplicate(similarity)) {
                        duplicates.add(new Duplicate(record1.getIndex(), record2.getIndex(), similarity, relation));
                    }
                }
            }
        }
//      ringewashere
        //            for (int i = 0; i < records.length - windowSize - 1; i++) {
//
//                int windowStart = i;
//                int windowEnd = Math.min(i + windowSize, records.length);
//
//                for (int j=windowStart; j<windowEnd; j++) {
//                    for (int k=j+1; k<windowEnd; k++) {
//                        String[] tuple1 = records[j].getValues();
//                        String[] tuple2 = records[k].getValues();
//                        double similarity = recordComparator.compare(tuple1, tuple2);
//                        if (recordComparator.isDuplicate(similarity)) {
//                            duplicates.add(new Duplicate(records[j].getIndex(), records[k].getIndex(), similarity, relation));
//                        }
//                    }
//                }
//            }
//        }

        //                                                                                                            //
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        return duplicates;
    }

    /**
     * Suggests a RecordComparator instance based on the provided relation for duplicate detection purposes.
     * @param relation The relation a RecordComparator needs to be suggested for.
     * @return A RecordComparator instance for comparing records of the provided relation.
     */
    public static RecordComparator suggestRecordComparatorFor(Relation relation) {
        List<AttrSimWeight> attrSimWeights = new ArrayList<>(relation.getAttributes().length);
        double threshold = 0.0;

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //                                      DATA INTEGRATION ASSIGNMENT                                           //
        // Define the AttrSimWeight objects for a RecordComparator that matches the records of the provided relation  //
        // possibly well, i.e., duplicate should receive possibly high similarity scores and non-duplicates should    //
        // receive possibly low scores. In other words, put together a possibly effective ensemble of the already     //
        // implemented similarity functions for duplicate detections runs on the provided relation. Side note: This   //
        // is usually learned by machine learning algorithms, but a creative, heuristics-based solution is sufficient //
        // here.                                                                                                      //

        for (int i = 0; i < relation.getAttributes().length; i++) {
            String attributeName = relation.getAttributes()[i].toLowerCase();
            SimilarityMeasure similarityMeasure;
            double weight;

            if (attributeName.contains("artist")) {
                similarityMeasure = new Jaccard(new Tokenizer(3, true), false);
                weight = 0.2;
            } else if (attributeName.contains("title")) {
                similarityMeasure = new Levenshtein(true);
                weight = 0.1;
            } else if (attributeName.contains("id")) {
                similarityMeasure = new Levenshtein(true);
                weight = 0.1;
            } else if (attributeName.contains("pk")) {
                similarityMeasure = new Levenshtein(true);
                weight = 0.1;
            } else if (attributeName.contains("category")) {
                similarityMeasure = new Levenshtein(true);
                weight = 0.15;
            } else if (attributeName.contains("genre")) {
                similarityMeasure = new Levenshtein(true);
                weight = 0.15;
            } else if (attributeName.contains("year")) {
                similarityMeasure = new Jaccard(new Tokenizer(3, true), false);
                weight = 0.2;
            } else if (attributeName.contains("track0")) {
                similarityMeasure = new Levenshtein(true);
                weight = 0.04;
            } else {
                similarityMeasure = new Levenshtein(false);
                weight = 0.0;
            }

            attrSimWeights.add(new AttrSimWeight(i, similarityMeasure, weight));
        }

        // Normalize weights
        double totalWeight = attrSimWeights.stream().mapToDouble(AttrSimWeight::getWeight).sum();
        List<AttrSimWeight> normalizedAttrSimWeights = attrSimWeights.stream()
                .map(attrSimWeight -> new AttrSimWeight(
                        attrSimWeight.getAttribute(),
                        attrSimWeight.getSimilarityMeasure(),
                        attrSimWeight.getWeight() / totalWeight))
                .collect(Collectors.toList());
        threshold = 0.9;
        //                                                                                                            //
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        return new RecordComparator(normalizedAttrSimWeights, threshold);
    }
}

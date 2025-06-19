package de.di.similarity_measures;

import de.di.similarity_measures.helper.Tokenizer;
import lombok.AllArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the Jaccard similarity measure supporting both set and bag semantics.
 */
@AllArgsConstructor
public class Jaccard implements SimilarityMeasure {

    // The tokenizer that is used to transform string inputs into token lists.
    private final Tokenizer tokenizer;

    // A flag indicating whether the Jaccard algorithm should use set or bag semantics for the similarity calculation.
    private final boolean bagSemantics;

    /**
     * Calculates the Jaccard similarity of the two input strings. Note that the Jaccard similarity may use set or
     * multiset, i.e., bag semantics for the union and intersect operations. The maximum Jaccard similarity with
     * multiset semantics is 1/2 and the maximum Jaccard similarity with set semantics is 1.
     * @param string1 The first string argument for the similarity calculation.
     * @param string2 The second string argument for the similarity calculation.
     * @return The multiset Jaccard similarity of the two arguments.
     */
    @Override
    public double calculate(String string1, String string2) {
        string1 = (string1 == null) ? "" : string1;
        string2 = (string2 == null) ? "" : string2;

        String[] tokens1 = this.tokenizer.tokenize(string1);
        String[] tokens2 = this.tokenizer.tokenize(string2);
        return this.calculate(tokens1, tokens2);
    }

    /**
     * Calculates the Jaccard similarity of the two string lists. Note that the Jaccard similarity may use set or
     * multiset, i.e., bag semantics for the union and intersect operations. The maximum Jaccard similarity with
     * multiset semantics is 1/2 and the maximum Jaccard similarity with set semantics is 1.
     * @param strings1 The first string list argument for the similarity calculation.
     * @param strings2 The second string list argument for the similarity calculation.
     * @return The Jaccard similarity of the two arguments.
     */
    @Override
    public double calculate(String[] strings1, String[] strings2) {
        // SET SEMANTICS: duplicates are ignored
        if (!bagSemantics) {
            Set<String> set1 = Arrays.stream(strings1)
                    .collect(Collectors.toSet());
            Set<String> set2 = Arrays.stream(strings2)                                    .collect(Collectors.toSet());

            // Intersection and union for sets
            Set<String> intersection = new HashSet<>(set1);
            intersection.retainAll(set2);

            Set<String> union = new HashSet<>(set1);
            union.addAll(set2);

            // If both sets are empty, define similarity as 1
            if (union.isEmpty()) {
                return 1.0;
            }

            return (double) intersection.size() / union.size();
        }

        // BAG SEMANTICS: duplicates are counted
        Map<String, Integer> freq1 = new HashMap<>();
        for (String token : strings1) {
            freq1.put(token, freq1.getOrDefault(token, 0) + 1);
        }

        Map<String, Integer> freq2 = new HashMap<>();
        for (String token : strings2) {
            freq2.put(token, freq2.getOrDefault(token, 0) + 1);
        }

        // Compute intersection size: sum of minima of token counts
        int intersectionCount = 0;
        for (Map.Entry<String, Integer> entry : freq1.entrySet()) {
            String token = entry.getKey();
            if (freq2.containsKey(token)) {
                intersectionCount += Math.min(entry.getValue(), freq2.get(token));
            }
        }

        // Union size: total tokens in both arrays
        int unionCount = strings1.length + strings2.length;

        // If both lists are empty, define similarity as 1
        if (unionCount == 0) {
            return 1.0;
        }

        return (double) intersectionCount / unionCount;
    }
}
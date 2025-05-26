package de.di.similarity_measures;

import lombok.AllArgsConstructor;

import java.util.Arrays;

@AllArgsConstructor
public class Levenshtein implements SimilarityMeasure {

    public static int min(int... numbers) {
        return Arrays.stream(numbers).min().orElse(Integer.MAX_VALUE);
    }

    // The choice of whether Levenshtein or DamerauLevenshtein should be calculated.
    private final boolean withDamerau;

    /**
     * Calculates the Levenshtein similarity of the two input strings.
     * The Levenshtein similarity is defined as "1 - normalized Levenshtein distance".
     * @param string1 The first string argument for the similarity calculation.
     * @param string2 The second string argument for the similarity calculation.
     * @return The (Damerau) Levenshtein similarity of the two arguments.
     */
    @Override
    public double calculate(final String string1, final String string2) {
        double levenshteinSimilarity = 0;

        int[] upperupperLine = new int[string1.length() + 1];   // line for Demarau lookups
        int[] upperLine = new int[string1.length() + 1];        // line for regular Levenshtein lookups
        int[] lowerLine = new int[string1.length() + 1];        // line to be filled next by the algorithm

        // Fill the first line with the initial positions (= edits to generate string1 from nothing)
        for (int i = 0; i <= string1.length(); i++)
            upperLine[i] = i;

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //                                      DATA INTEGRATION ASSIGNMENT                                           //
        // Use the three provided lines to successively calculate the Levenshtein matrix with the dynamic programming //
        // algorithm. Depending on whether the inner flag withDamerau is set, the Damerau extension rule should be    //
        // used during calculation or not. Hint: Implement the Levenshtein algorithm here first, then copy the code   //
        // to the String tuple function and adjust it a bit to work on the arrays - the algorithm is the same.        //

        for (int j = 1; j <= string2.length(); j++) {
            lowerLine[0] = j;
            for (int i = 1; i <= string1.length(); i++) {
                int cost = (string1.charAt(i - 1) == string2.charAt(j - 1)) ? 0 : 1;

                lowerLine[i] = min(
                        lowerLine[i - 1] + 1,      // insertion
                        upperLine[i] + 1,          // deletion
                        upperLine[i - 1] + cost    // substitution
                );

                if (withDamerau && i > 1 && j > 1
                        && string1.charAt(i - 1) == string2.charAt(j - 2)
                        && string1.charAt(i - 2) == string2.charAt(j - 1)) {
                    lowerLine[i] = Math.min(lowerLine[i], upperupperLine[i - 2] + 1); // transposition
                }
            }

            // shift lines for next iteration
            System.arraycopy(upperLine, 0, upperupperLine, 0, upperLine.length);
            System.arraycopy(lowerLine, 0, upperLine, 0, lowerLine.length);
        }

        int distance = upperLine[string1.length()];
        int maxLength = Math.max(string1.length(), string2.length());
        levenshteinSimilarity = (maxLength == 0) ? 1.0 : 1.0 - ((double) distance / maxLength);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        return levenshteinSimilarity;
    }

    /**
     * Calculates the Levenshtein similarity of the two input string lists.
     * The Levenshtein similarity is defined as "1 - normalized Levenshtein distance".
     * For string lists, we consider each list as an ordered list of tokens and calculate the distance as the number of
     * token insertions, deletions, replacements (and swaps) that transform one list into the other.
     * @param strings1 The first string list argument for the similarity calculation.
     * @param strings2 The second string list argument for the similarity calculation.
     * @return The (multiset) Levenshtein similarity of the two arguments.
     */
    @Override
    public double calculate(final String[] strings1, final String[] strings2) {
        double levenshteinSimilarity = 0;

        int[] upperupperLine = new int[strings1.length + 1];   // line for Damerau lookups
        int[] upperLine = new int[strings1.length + 1];        // line for regular Levenshtein lookups
        int[] lowerLine = new int[strings1.length + 1];        // line to be filled next by the algorithm

        // Fill the first line with the initial positions (= edits to generate string1 from nothing)
        for (int i = 0; i <= strings1.length; i++)
            upperLine[i] = i;

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //                                      DATA INTEGRATION ASSIGNMENT                                           //
        // Use the three provided lines to successively calculate the Levenshtein matrix with the dynamic programming //
        // algorithm. Depending on whether the inner flag withDamerau is set, the Damerau extension rule should be    //
        // used during calculation or not. Hint: Implement the Levenshtein algorithm above first, then copy the code  //
        // to this function and adjust it a bit to work on the arrays - the algorithm is the same.                    //

        for (int j = 1; j <= strings2.length; j++) {
            lowerLine[0] = j;
            for (int i = 1; i <= strings1.length; i++) {
                int cost = strings1[i - 1].equals(strings2[j - 1]) ? 0 : 1;

                lowerLine[i] = min(
                        lowerLine[i - 1] + 1,      // insertion
                        upperLine[i] + 1,          // deletion
                        upperLine[i - 1] + cost    // substitution
                );

                if (withDamerau && i > 1 && j > 1
                        && strings1[i - 1].equals(strings2[j - 2])
                        && strings1[i - 2].equals(strings2[j - 1])) {
                    lowerLine[i] = Math.min(lowerLine[i], upperupperLine[i - 2] + 1); // transposition
                }
            }

            // shift lines
            System.arraycopy(upperLine, 0, upperupperLine, 0, upperLine.length);
            System.arraycopy(lowerLine, 0, upperLine, 0, lowerLine.length);
        }

        int distance = upperLine[strings1.length];
        int maxLength = Math.max(strings1.length, strings2.length);
        levenshteinSimilarity = (maxLength == 0) ? 1.0 : 1.0 - ((double) distance / maxLength);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        return levenshteinSimilarity;
    }
}
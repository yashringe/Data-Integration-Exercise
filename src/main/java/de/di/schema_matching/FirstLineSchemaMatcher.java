package de.di.schema_matching;

import de.di.Relation;
import de.di.schema_matching.structures.SimilarityMatrix;
import de.di.similarity_measures.Jaccard;
import de.di.similarity_measures.helper.Tokenizer;

/**
 * A trivial first-line schema matcher that uses only header string Jaccard similarity.
 */
public class FirstLineSchemaMatcher {

    /**
     * Matches attributes by computing Jaccard similarity over attribute names (headers).
     * @param sourceRelation The source relation.
     * @param targetRelation The target relation.
     * @return Similarity matrix of size (#source attrs) x (#target attrs).
     */
    public SimilarityMatrix match(Relation sourceRelation, Relation targetRelation) {
        String[][] sourceColumns = sourceRelation.getColumns();
        String[][] targetColumns = targetRelation.getColumns();
        String[] sourceHeaders = sourceRelation.getAttributes();
        String[] targetHeaders = targetRelation.getAttributes();

        double[][] matrix = new double[sourceColumns.length][targetColumns.length];

        Tokenizer tokenizer = new Tokenizer(3, true);
        Jaccard jaccard = new Jaccard(tokenizer, false); // false = set semantics

        for (int i = 0; i < sourceColumns.length; i++) {
            String sourceHeader = sourceHeaders[i].toLowerCase();
            String sourceData = String.join(" ", sourceColumns[i]).toLowerCase();

            for (int j = 0; j < targetColumns.length; j++) {
                String targetHeader = targetHeaders[j].toLowerCase();
                String targetData = String.join(" ", targetColumns[j]).toLowerCase();

                // Weighted similarity using header and column data
                double headerSim = jaccard.calculate(sourceHeader, targetHeader);
                double valueSim = jaccard.calculate(sourceData, targetData);

                matrix[i][j] = 0.6 * headerSim + 0.4 * valueSim;
            }
        }

        return new SimilarityMatrix(matrix, sourceRelation, targetRelation);
    }
}


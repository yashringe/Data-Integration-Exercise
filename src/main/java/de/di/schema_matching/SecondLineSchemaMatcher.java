package de.di.schema_matching;

import de.di.schema_matching.structures.CorrespondenceMatrix;
import de.di.schema_matching.structures.SimilarityMatrix;

import java.util.*;

public class SecondLineSchemaMatcher {

    /**
     * Translates the provided similarity matrix into a binary correspondence matrix by selecting possibly optimal
     * attribute correspondences from the similarities.
     * @param similarityMatrix A matrix of pair-wise attribute similarities.
     * @return A CorrespondenceMatrix of pair-wise attribute correspondences.
     */
    /**
     * Translate an array of source assignments into a correlation matrix. For example, [0,3,2] maps 0->1, 1->3, 2->2
     * and, therefore, translates into [[1,0,0,0][0,0,0,1][0,0,1,0]].
     * @param sourceAssignments The list of source assignments.
     * @param simMatrix The original similarity matrix; just used to determine the number of source and target attributes.
     * @return The correlation matrix extracted form the source assignments.
     */


    public CorrespondenceMatrix match(SimilarityMatrix similarityMatrix) {
        double[][] simMatrix = similarityMatrix.getMatrix();
        int rowCount = simMatrix.length;
        int colCount = simMatrix[0].length;

        // Track assignments
        int[] assignment = new int[rowCount];
        Arrays.fill(assignment, -1);
        Set<Integer> usedCols = new HashSet<>();

        // Flatten and sort similarity values
        List<Match> allMatches = new ArrayList<>();
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                allMatches.add(new Match(i, j, simMatrix[i][j]));
            }
        }
        allMatches.sort((a, b) -> Double.compare(b.similarity, a.similarity)); // Descending

        // Greedily assign highest available matches
        Set<Integer> assignedRows = new HashSet<>();
        Set<Integer> assignedCols = new HashSet<>();
        for (Match match : allMatches) {
            if (!assignedRows.contains(match.row) && !assignedCols.contains(match.col)) {
                assignment[match.row] = match.col;
                assignedRows.add(match.row);
                assignedCols.add(match.col);
            }
        }

        int[][] corrMatrix = assignmentArray2correlationMatrix(assignment, simMatrix);
        return new CorrespondenceMatrix(corrMatrix,
                similarityMatrix.getSourceRelation(),
                similarityMatrix.getTargetRelation());
    }

    private int[][] assignmentArray2correlationMatrix(int[] sourceAssignments, double[][] simMatrix) {
        int[][] corrMatrix = new int[simMatrix.length][simMatrix[0].length];
        for (int i = 0; i < sourceAssignments.length; i++) {
            if (sourceAssignments[i] >= 0) {
                corrMatrix[i][sourceAssignments[i]] = 1;
            }
        }
        return corrMatrix;
    }

    private static class Match {
        int row;
        int col;
        double similarity;

        Match(int row, int col, double similarity) {
            this.row = row;
            this.col = col;
            this.similarity = similarity;
        }
    }
}
package de.di.data_profiling;

import de.di.Relation;
import de.di.data_profiling.structures.IND;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Discovers unary inclusion dependencies (INDs) among a set of relations.
 */
public class INDProfiler {

    /**
     * Discovers all non-trivial unary inclusion dependencies in the provided relations.
     * @param relations The list of relations to profile for inclusion dependencies.
     * @param discoverNary Whether to discover n-ary INDs; currently only unary INDs are supported.
     * @return The list of all discovered non-trivial INDs.
     */
    public List<IND> profile(List<Relation> relations, boolean discoverNary) {
        List<IND> inclusionDependencies = new ArrayList<>();

        // Precompute sets of values per column for each relation (including nulls)
        Map<Relation, List<Set<String>>> relationValueSets = new HashMap<>();
        for (Relation rel : relations) {
            String[][] columns = rel.getColumns();
            relationValueSets.put(rel, toColumnSets(columns));
        }

        // Compare every ordered pair of relations (including same), but skip identical columns
        for (Relation r : relations) {
            List<Set<String>> setsR = relationValueSets.get(r);
            for (Relation s : relations) {
                List<Set<String>> setsS = relationValueSets.get(s);
                for (int i = 0; i < setsR.size(); i++) {
                    for (int j = 0; j < setsS.size(); j++) {
                        // skip trivial same-column reflexive
                        if (r.equals(s) && i == j) continue;
                        Set<String> setR = setsR.get(i);
                        Set<String> setS = setsS.get(j);
                        // Check inclusion (empty sets allowed)
                        if (setS.containsAll(setR)) {
                            inclusionDependencies.add(new IND(r, i, s, j));
                        }
                    }
                }
            }
        }

        if (discoverNary) {
            throw new UnsupportedOperationException("N-ary IND discovery is not supported.");
        }

        return inclusionDependencies;
    }

    /**
     * Converts a 2D array of column values into a list of Sets of Strings, one per column.
     * Includes nulls as values.
     */
    private List<Set<String>> toColumnSets(String[][] columns) {
        return Arrays.stream(columns)
                .map(col -> new HashSet<>(Arrays.asList(col)))
                .collect(Collectors.toList());
    }
}
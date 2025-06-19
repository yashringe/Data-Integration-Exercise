package de.di.data_profiling;

import de.di.Relation;
import de.di.data_profiling.structures.AttributeList;
import de.di.data_profiling.structures.PositionListIndex;
import de.di.data_profiling.structures.UCC;

import java.util.*;

/**
 * Discovers all minimal, non-trivial unique column combinations (UCCs) in a relation.
 * Uses a level-wise lattice search with PLI intersections to validate uniqueness.
 */
public class UCCProfiler {

    /**
     * Discovers all minimal, non-trivial unique column combinations in the provided relation.
     * @param relation The relation to profile for UCCs.
     * @return The list of all minimal, non-trivial UCCs.
     */
    public List<UCC> profile(Relation relation) {
        int numAttributes = relation.getAttributes().length;
        Set<UCC> uniqueSet = new LinkedHashSet<>();
        List<PositionListIndex> nonUniquePLIs = new ArrayList<>();

        // 1. Compute unary UCCs
        for (int i = 0; i < numAttributes; i++) {
            AttributeList al = new AttributeList(i);
            PositionListIndex pli = new PositionListIndex(al, relation.getColumns()[i]);
            if (pli.isUnique()) {
                uniqueSet.add(new UCC(relation, al));
            } else {
                nonUniquePLIs.add(pli);
            }
        }

        // Track discovered minimal uniques for pruning
        Set<AttributeList> minimalUniques = new HashSet<>();
        for (UCC ucc : uniqueSet) {
            minimalUniques.add(ucc.getAttributeList());
        }

        // 2. Level-wise lattice search
        while (!nonUniquePLIs.isEmpty()) {
            List<PositionListIndex> nextPLIs = new ArrayList<>();
            Set<AttributeList> seen = new HashSet<>();

            for (int x = 0; x < nonUniquePLIs.size(); x++) {
                for (int y = x + 1; y < nonUniquePLIs.size(); y++) {
                    PositionListIndex p1 = nonUniquePLIs.get(x);
                    PositionListIndex p2 = nonUniquePLIs.get(y);
                    AttributeList a1 = p1.getAttributes();
                    AttributeList a2 = p2.getAttributes();

                    // Combine only if n-1 attributes match (prefix join)
                    if (!a1.samePrefixAs(a2)) continue;
                    AttributeList combined = a1.union(a2);

                    // Avoid duplicate attribute combinations
                    if (!seen.add(combined)) continue;

                    // Prune if any known minimal unique is subset of combined (non-minimal)
                    boolean skip = false;
                    for (AttributeList mu : minimalUniques) {
                        if (combined.supersetOf(mu)) {
                            skip = true;
                            break;
                        }
                    }
                    if (skip) continue;

                    // Validate by intersecting PLIs
                    PositionListIndex mergedPLI = p1.intersect(p2);
                    if (mergedPLI.isUnique()) {
                        uniqueSet.add(new UCC(relation, combined));
                        minimalUniques.add(combined);
                    } else {
                        nextPLIs.add(mergedPLI);
                    }
                }
            }
            nonUniquePLIs = nextPLIs;
        }

        return new ArrayList<>(uniqueSet);
    }
}
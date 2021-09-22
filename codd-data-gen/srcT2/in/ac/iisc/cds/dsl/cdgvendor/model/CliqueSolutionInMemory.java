package in.ac.iisc.cds.dsl.cdgvendor.model;

import it.unimi.dsi.fastutil.ints.IntList;

/**
 * Different from ViewSolutionInMemory as its valueCombinations hold values only for the given colIndx
 * while ViewSolution is extended such that its valueCombinations hold values for all nonkeys in a view.
 * In memory.
 * @author rWX450917
 *
 */
public class CliqueSolutionInMemory extends AbstractValueCombinationListInMemory {

    protected final IntList colIndxs;

    public CliqueSolutionInMemory(IntList colIndxs) {
        super(1000);
        this.colIndxs = colIndxs;
    }

    public IntList getColIndexes() {
        return colIndxs;
    }

    @Override
    public String toString() {
        return "colIndxs=" + colIndxs + "\n" + super.toString();
    }
}

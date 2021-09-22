package in.ac.iisc.cds.dsl.cdgvendor.model;

import it.unimi.dsi.fastutil.ints.IntList;

public interface ViewSolution extends AbstractValueCombinationListInterface {

    public void prepareForSearch();

    public boolean contains(IntList valuesInCombination);

    public long getPK(IntList valuesInCombination);

    public ViewSolution clone();
}

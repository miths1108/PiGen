package in.ac.iisc.cds.dsl.cdgvendor.model;

import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;

/**
 * Different from ViewSolutionInMemory as its valueCombinations hold values only for the given colIndx
 * while ViewSolution is extended such that its valueCombinations hold values for all nonkeys in a view.
 * In memory.
 * @author rWX450917
 *
 */
public class ViewSolutionInMemory extends AbstractValueCombinationListInMemory implements ViewSolution {

    private Object2LongLinkedOpenHashMap<IntList> combinationValuesToPKStartMap; //another format to store List<ValueCombination> which is easy to search
    private long                                  lastSeenPK;

    public ViewSolutionInMemory(int expectedCapacity) {
        super(expectedCapacity);
        combinationValuesToPKStartMap = null;
        lastSeenPK = -1;
    }

    @Override
    public void prepareForSearch() {
        combinationValuesToPKStartMap = new Object2LongLinkedOpenHashMap<>();
        combinationValuesToPKStartMap.defaultReturnValue(-1);
        lastSeenPK = 0;
        //Overwrites if map entry already present
        for (ValueCombination valueCombination : valueCombinations) {
            combinationValuesToPKStartMap.put(valueCombination.getColValues(), lastSeenPK);
            lastSeenPK += valueCombination.getRowcount();
        }
    }

    /**
     * In memory ViewSolution gains by using this optimized Hash code based search
     * instead of using linear search through the list
     */
    @Override
    public boolean contains(IntList valuesInCombination) {
        return combinationValuesToPKStartMap.keySet().contains(valuesInCombination);
    }

    /**
     * In memory ViewSolution gains by using this optimized Hash code based search
     * instead of using linear search through the list
     */
    @Override
    public long getPK(IntList valuesInCombination) {
        long result = combinationValuesToPKStartMap.getLong(valuesInCombination);
        if (result == combinationValuesToPKStartMap.defaultReturnValue())
            throw new RuntimeException("Map doesn't contain key " + valuesInCombination);
        return result;
    }

    @Override
    public void addValueCombination(ValueCombination valueCombination) {
        valueCombinations.add(valueCombination);
        if (combinationValuesToPKStartMap != null) {
            //Overwrites if map entry already present
            combinationValuesToPKStartMap.put(valueCombination.getColValues(), lastSeenPK);
            lastSeenPK += valueCombination.getRowcount();
        }
    }

    @Override
    public ViewSolution clone() {
        ViewSolutionInMemory cloneViewSolutionInMemory = new ViewSolutionInMemory(valueCombinations.size());
        for (ValueCombination valueCombination : valueCombinations) {
            cloneViewSolutionInMemory.addValueCombination(valueCombination);
        }
        if (combinationValuesToPKStartMap != null) { //Already prepared for search
            cloneViewSolutionInMemory.prepareForSearch();
        }
        return cloneViewSolutionInMemory;
    }
}

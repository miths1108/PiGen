package in.ac.iisc.cds.dsl.dirty;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import in.ac.iisc.cds.dsl.cdgvendor.model.ValueCombination;
import in.ac.iisc.cds.dsl.cdgvendor.model.ViewSolution;
import in.ac.iisc.cds.dsl.cdgvendor.model.ViewSolutionInMemory;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;

@Deprecated
public class DirtyViewSolution implements Iterable<DirtyValueCombinationWithProjectionValues> {
	protected List<DirtyValueCombinationWithProjectionValues> valueCombinations;
//	private Object2LongLinkedOpenHashMap<IntList> combinationValuesToPKStartMap; //another format to store List<ValueCombination> which is easy to search
	private Object2IntLinkedOpenHashMap<IntList> combinationValuesToIndex; //another format to store List<ValueCombination> which is easy to search
	private LongList indexToPKStart;
    private long                                  lastSeenPK;

    public DirtyViewSolution(int expectedCapacity) {
    	valueCombinations = new ArrayList<>(expectedCapacity);
    	combinationValuesToIndex = null;
    	indexToPKStart = new LongArrayList();
        lastSeenPK = -1;
    }
    
    public DirtyViewSolution(List<DirtyValueCombinationWithProjectionValues> valueCombinations) {
    	this.valueCombinations = valueCombinations;
    	combinationValuesToIndex = null;
    	indexToPKStart = new LongArrayList();
        lastSeenPK = -1;
    }

    public void prepareForSearch() {
    	combinationValuesToIndex = new Object2IntLinkedOpenHashMap<>();
    	combinationValuesToIndex.defaultReturnValue(-1);
        lastSeenPK = 0;
        //Overwrites if map entry already present
        for (int i = 0; i < valueCombinations.size(); i++) {
        	DirtyValueCombinationWithProjectionValues valueCombination = valueCombinations.get(i);
        	combinationValuesToIndex.put(valueCombination.getColValues(), i);
        	indexToPKStart.add(lastSeenPK);
            lastSeenPK += valueCombination.getRowcount();
        }
    }

    /**
     * In memory ViewSolution gains by using this optimized Hash code based search
     * instead of using linear search through the list
     */
    public boolean contains(IntList valuesInCombination) {
        return combinationValuesToIndex.keySet().contains(valuesInCombination);
    }

    /**
     * In memory ViewSolution gains by using this optimized Hash code based search
     * instead of using linear search through the list
     */
    public long getPK(IntList valuesInCombination) {
    	int index = combinationValuesToIndex.getInt(valuesInCombination);
        if (index == combinationValuesToIndex.defaultReturnValue())
            throw new RuntimeException("Map doesn't contain key " + valuesInCombination);
    	long result = indexToPKStart.getLong(index);
        return result;
    }

    public void addValueCombination(DirtyValueCombinationWithProjectionValues valueCombination) {
        valueCombinations.add(valueCombination);
        if (combinationValuesToIndex != null) {
            //Overwrites if map entry already present
        	combinationValuesToIndex.put(valueCombination.getColValues(), indexToPKStart.size());
        	indexToPKStart.add(lastSeenPK);
            lastSeenPK += valueCombination.getRowcount();
        }
    }

	public DirtyValueCombinationWithProjectionValues getValueCombination(IntList valuesInCombination) {
		int index = combinationValuesToIndex.getInt(valuesInCombination);
		return valueCombinations.get(index);
	}

    @Override
    public ViewSolution clone() {
        ViewSolutionInMemory cloneViewSolutionInMemory = new ViewSolutionInMemory(valueCombinations.size());
        for (ValueCombination valueCombination : valueCombinations) {
            cloneViewSolutionInMemory.addValueCombination(valueCombination);
        }
        if (combinationValuesToIndex != null) { //Already prepared for search
            cloneViewSolutionInMemory.prepareForSearch();
        }
        return cloneViewSolutionInMemory;
    }
    
    public int getCountOfValueCombinations() {
        return valueCombinations.size();
    }
    
	@Override
    public Iterator<DirtyValueCombinationWithProjectionValues> iterator() {
        Iterator<DirtyValueCombinationWithProjectionValues> it = new Iterator<DirtyValueCombinationWithProjectionValues>() {

            private int currentIndex = 0;

            @Override
            public boolean hasNext() {
                return currentIndex < getCountOfValueCombinations();
            }

            @Override
            public DirtyValueCombinationWithProjectionValues next() {
                return valueCombinations.get(currentIndex++);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
        return it;
    }
}

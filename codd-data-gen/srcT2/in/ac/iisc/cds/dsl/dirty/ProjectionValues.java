package in.ac.iisc.cds.dsl.dirty;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.Set;

import in.ac.iisc.cds.dsl.cdgvendor.utils.Triplet;

public class ProjectionValues {
//	private static long DefaultValue = 0;
	HashMap<Integer, LinkedList<DirtyValueIntervalWithCount>> colToValues = new HashMap<>();

	public void addToList(int colIndx, long start, long end, long count) {
		List<DirtyValueIntervalWithCount> listOfIntervalsWithCounts = getList(colIndx);
		listOfIntervalsWithCounts.add(new DirtyValueIntervalWithCount(start, end, count));
	}
	
	public void addToList(int colIndx, DirtyValueIntervalWithCount intervalToAdd) {
		List<DirtyValueIntervalWithCount> listOfIntervalsWithCounts = getList(colIndx);
		listOfIntervalsWithCounts.add(intervalToAdd);
	}
	
	public Set<Entry<Integer, LinkedList<DirtyValueIntervalWithCount>>> entrySet() {
		return colToValues.entrySet();
	}
	
	public LinkedList<DirtyValueIntervalWithCount> getIntervals(int colIndx) {
		return colToValues.get(colIndx);
	}
	
	@Override
	public String toString() {
		return colToValues.toString();
	}

//	public void fillLeftoverSpaceWithDefault(int colIndx, long leftoverSize) {
//		List<DirtyValueIntervalWithCount> listOfIntervalsWithCounts = getList(colIndx);
//		listOfIntervalsWithCounts.add(new DirtyValueIntervalWithCount(DefaultValue, DefaultValue + 1, leftoverSize));
//	}
	
	private List<DirtyValueIntervalWithCount> getList(int colIndx) {
		LinkedList<DirtyValueIntervalWithCount> listOfIntervalsWithCounts = colToValues.get(colIndx);
		if (listOfIntervalsWithCounts == null) {
			listOfIntervalsWithCounts = new LinkedList<>();
			colToValues.put(colIndx, listOfIntervalsWithCounts);
		}
		return listOfIntervalsWithCounts;
	}

	public long getTotalCount(int relativeColIndx) {
		List<DirtyValueIntervalWithCount> listOfIntervalsWithCounts = colToValues.get(relativeColIndx);
		if (listOfIntervalsWithCounts == null)
			return -1;
		long result = 0;
		for (DirtyValueIntervalWithCount interval : listOfIntervalsWithCounts) {
			result += interval.getIntervalSizeWithoutProjection();
		}
		return result;
	}

	public long getTotalCount() {
		if (colToValues.size() == 0)
			return 0;
		Collection<Integer> keys = colToValues.keySet();
		Iterator<Integer> it = keys.iterator();
		return getTotalCount(it.next());
	}

	public long checkCountConssistency() {
		Collection<Integer> keys = colToValues.keySet();
		Iterator<Integer> it = keys.iterator();
		long result = getTotalCount(it.next());
		
		while (it.hasNext()) {
			if (getTotalCount(it.next()) != result)
				throw new RuntimeException("Count of total values not same across columns");
		}
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		throw new RuntimeException("Not implemented!");
	}
	
	@Override
	public int hashCode() {
		return colToValues.hashCode();
	}

	public Set<Integer> keySet() {
		return colToValues.keySet();
	}

	public void takeFrom(ProjectionValues donorProjectionValues, long minMergable, Set<Integer> colsToSkip) {
		if (donorProjectionValues == null)
			return;
		for (Entry<Integer, LinkedList<DirtyValueIntervalWithCount>> entry : donorProjectionValues.entrySet()) {
			Integer colIdx = entry.getKey();
			if (colsToSkip != null && colsToSkip.contains(colIdx))
				continue;
			List<DirtyValueIntervalWithCount> listOfIntervalsWithCounts = getList(colIdx);
			LinkedList<DirtyValueIntervalWithCount> donorListOfIntervalsWithCounts = entry.getValue();
			long toBeDonated = minMergable;
			ListIterator<DirtyValueIntervalWithCount> it = donorListOfIntervalsWithCounts.listIterator();
			while (toBeDonated != 0) {
				DirtyValueIntervalWithCount donorInterval = it.next();
				if (donorInterval.getIntervalSizeWithoutProjection() <= toBeDonated) {
					toBeDonated -= donorInterval.getIntervalSizeWithoutProjection();
					it.remove();
					listOfIntervalsWithCounts.add(donorInterval);
				} else {
					Triplet<DirtyValueIntervalWithCount, DirtyValueIntervalWithCount, DirtyValueIntervalWithCount> leftOvers = donorInterval.splitForDonating(toBeDonated);
					
					if (donorInterval.getIntervalSizeWithoutProjection() == 0)
						it.remove();
					
					DirtyValueIntervalWithCount singleIntervalToKeep = leftOvers.getFirst();
					if (singleIntervalToKeep != null)
						it.add(singleIntervalToKeep);
					
					DirtyValueIntervalWithCount donatedPartWithOriginalCount = leftOvers.getSecond();
					if (donatedPartWithOriginalCount != null)
						listOfIntervalsWithCounts.add(donatedPartWithOriginalCount);
					
					DirtyValueIntervalWithCount singleIntervalToDonate = leftOvers.getThird();
					if (singleIntervalToDonate != null)
						listOfIntervalsWithCounts.add(singleIntervalToDonate);
					toBeDonated = 0;
				}
			}
		}
	}

	public ProjectionValues getDeepCopy() {
		ProjectionValues copy = new ProjectionValues();
		for (Entry<Integer, LinkedList<DirtyValueIntervalWithCount>> entry : colToValues.entrySet()) {
			LinkedList<DirtyValueIntervalWithCount> listOfValues = entry.getValue();
			for (DirtyValueIntervalWithCount interval : listOfValues) {
				copy.addToList(entry.getKey(), interval.start, interval.end, interval.count);
			}
		}
		return copy;
	}
}

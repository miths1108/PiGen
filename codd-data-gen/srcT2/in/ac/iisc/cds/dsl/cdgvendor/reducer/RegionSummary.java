package in.ac.iisc.cds.dsl.cdgvendor.reducer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import in.ac.iisc.cds.dsl.cdgvendor.model.ValueCombinationF;
import in.ac.iisc.cds.dsl.cdgvendor.utils.Pair;
import it.unimi.dsi.fastutil.ints.IntList;

public class RegionSummary implements Serializable {
	private static final long serialVersionUID = 2L;
	public Region region;
	public Long rowCount;
	public Set<List<String>> groupkeys;
	public List<ValueCombinationF> valComb;
	public Map<List<Integer>, List<Pair<RegionF, Pair<Long, Long>>>> groupKeyToRegionF;// first is count second is
																						// cutoff
	public Long counter = 0L;
	public List<Double> tuple;
	private Long genCount = 0L;
	Long numTuples;
	List<List<Double>> bucketSplitPoints;
	IntList mergedColIndxs;
	Map<List<Integer>, List<RegionF>> groupKeysToRegionFList;
	Map<List<Integer>, ListIterator<RegionF>> groupKeysToIterator;
	Map<List<Integer>, RegionF> groupKeysToCurRegionF;
	int size;

	public RegionSummary() {
		this.groupkeys = new HashSet<>();
		this.groupKeyToRegionF = new HashMap<>();
		groupKeysToRegionFList = new HashMap<>();
		groupKeysToIterator = new HashMap<>();
		groupKeysToCurRegionF = new HashMap<>();

	}

	public RegionSummary divide(Long count) {
		// count <- no of tuples are kept in the cyurrent object
		float ratio = (float) count / (float) rowCount;
		RegionSummary result = new RegionSummary();
		result.rowCount = rowCount - count;// left over row count
		// Region reg=new Region();
		result.region = region.getDeepCopy();
		result.groupkeys.addAll(groupkeys);
		rowCount = count;
		for (List<Integer> groupIdxs : groupKeyToRegionF.keySet()) {
			// iterated over each group key and find s the total no of distict tuples for
			// that group key also initilizes the map for result.
			result.groupKeyToRegionF.put(groupIdxs, new ArrayList<>());
			Long total = (long) 0;
			for (Pair<RegionF, Pair<Long, Long>> currInterval : groupKeyToRegionF.get(groupIdxs)) {
				total += currInterval.getSecond().getFirst();

			}
			Long toKeep = (long) (ratio * total);// how many distinct tuples to keep in the current region
			if(toKeep==total)
				System.out.println("yessss");
			if (toKeep == 0) {
				toKeep = (long) 1;
			}
			if (total == 1) {
				result.groupKeyToRegionF.get(groupIdxs).addAll(groupKeyToRegionF.get(groupIdxs));
				continue;
				// throw new RuntimeException("DO a better split. Perhaps add the same tuple in
				// both. that is same cutOff");
			}
			Long currCount = (long) 0;
			ListIterator<Pair<RegionF, Pair<Long, Long>>> iter = groupKeyToRegionF.get(groupIdxs).listIterator();
			while (iter.hasNext()) {
				
				Pair<RegionF, Pair<Long, Long>> currInterval = iter.next();
				if (currCount + currInterval.getSecond().getFirst() > toKeep) {
					Long cardinality = currCount + currInterval.getSecond().getFirst() - toKeep;// to be transferred
					Long cutOff = (toKeep - currCount) + currInterval.getSecond().getSecond();// adding the number of
																								// tuples generated from
																								// tis interval to the
																								// original cut off for
																								// this interval
					Pair<Long, Long> pTemp = new Pair<Long, Long>(cardinality, cutOff);
					currInterval.getSecond().setFirst(toKeep - currCount);
					Pair<RegionF, Pair<Long, Long>> splitRegionF = new Pair<RegionF, Pair<Long, Long>>(
							currInterval.getFirst(), pTemp);

					result.groupKeyToRegionF.get(groupIdxs).add(splitRegionF);

					while (iter.hasNext()) {
						currInterval = iter.next();
						iter.remove();// removes from the current region
						result.groupKeyToRegionF.get(groupIdxs).add(currInterval);// adds to the new region
					}
				}
				currCount+=currInterval.getSecond().getFirst();
			}
			
		}
		for (List<Integer> groupIdxs : groupKeyToRegionF.keySet()) {
			List<Pair<RegionF, Pair<Long, Long>>> regionsFList = groupKeyToRegionF.get(groupIdxs);
			if(regionsFList.isEmpty())
				throw new RuntimeException("groupkey is missing which was earlier present");
		}
		for (List<Integer> groupIdxs : result.groupKeyToRegionF.keySet()) {
			List<Pair<RegionF, Pair<Long, Long>>> regionsFList = result.groupKeyToRegionF.get(groupIdxs);
			if(regionsFList.isEmpty()) {
				throw new RuntimeException("groupkey is missing which was earlier present");
			}
		}
		return result;
	}

	public void intialize(IntList mergedColIndxs, List<List<Double>> bucketSplitPoints,
			List<List<Long>> splitPointsCount, Region region2, int size) {
		this.size = size;
		this.mergedColIndxs = mergedColIndxs;
		tuple = new ArrayList<Double>();
		numTuples = 0L;
		for (List<Integer> groupkeyIdx : groupKeyToRegionF.keySet()) {

			List<Pair<RegionF, Pair<Long, Long>>> regionsFList = groupKeyToRegionF.get(groupkeyIdx);
			Long curNumTuples = 0L;
			for (Pair<RegionF, Pair<Long, Long>> regionFCur : regionsFList) {
				curNumTuples += regionFCur.getSecond().getFirst();
			}
			numTuples = Math.max(curNumTuples, numTuples);
		}
		
		if (groupkeys.isEmpty()) {
			// double [] tuple=new double[allTrueBS.size()];

			// First initializing with the tuple with the corner point.
			List<Double> tuple = new ArrayList<>(Collections.nCopies(size, 0.0));
			for (int j = 0; j < mergedColIndxs.size(); j++) {
//				tuple[mergedColIndxs.getInt(j)]=region.at(0).at(j).at(0);
				tuple.set(mergedColIndxs.getInt(j), (double) region.at(0).at(j).at(0));
			}

//			for (Long i = 0L; i < regSum.rowCount; i++) {
//				
//				System.out.print(i);// write to file;
//			}
			return;
		}

		this.bucketSplitPoints = bucketSplitPoints;

		// Map<List<Integer>, List<Pair<RegionF, Pair<Long, Long>>>> groupKeyToRegionF =
		// regSum.groupKeyToRegionF;

		for (List<Integer> groupkeyIdx : groupKeyToRegionF.keySet()) {
			List<Pair<RegionF, Pair<Long, Long>>> regionsFList = groupKeyToRegionF.get(groupkeyIdx);
//			List<ValueCombinationF> ret = new ArrayList<>();

			for (Pair<RegionF, Pair<Long, Long>> regionFCur : regionsFList) {
				RegionF regionF = regionFCur.getFirst();
				Long rowCount = regionFCur.getSecond().getFirst();
				Long cutOff = regionFCur.getSecond().getSecond();
				Double splitPoint = regionF.at(0).at(0).at(0);// first bs, first bucket, first interval
				int splitPointIdx = bucketSplitPoints.get(groupkeyIdx.get(0)).indexOf(splitPoint);
				Double nextSplitPoint;
				if (splitPointIdx + 1 < bucketSplitPoints.get(groupkeyIdx.get(0)).size())
					nextSplitPoint = bucketSplitPoints.get(groupkeyIdx.get(0)).get(splitPointIdx + 1);// what if no next
																										// split point
				else
					nextSplitPoint = (double) Math.floor(splitPoint + 1);// if the split point is the last split point
																			// then we will generate tuples between this
																			// point and the next integer
				Long splitCount = splitPointsCount.get(groupkeyIdx.get(0)).get(splitPointIdx);// the number of tuples
																								// that are to be
																								// generated from this
																								// interval(may not be
																								// only due to the
																								// current region)
				regionF.rowCount = rowCount;
				regionF.cutOff = cutOff;
				regionF.splitPoint = splitPoint;
				regionF.nextSplitPoint = nextSplitPoint;
				regionF.splitCount = splitCount;
				regionF.intervalSize = nextSplitPoint - splitPoint;
				regionF.counter = 0;
			}
		}

		for (List<Integer> groupkeyIdx : groupKeyToRegionF.keySet()) {
			List<Pair<RegionF, Pair<Long, Long>>> regionsFList = groupKeyToRegionF.get(groupkeyIdx);
			if(regionsFList.isEmpty())
				System.out.println("Wrong wrong wrong");
			for (Pair<RegionF, Pair<Long, Long>> regionFCur : regionsFList) {
				if (!groupKeysToRegionFList.containsKey(groupkeyIdx)) {
					groupKeysToRegionFList.put(groupkeyIdx, new ArrayList<>());

				}
				groupKeysToRegionFList.get(groupkeyIdx).add(regionFCur.getFirst());
			}
			groupKeysToCurRegionF.put(groupkeyIdx, regionsFList.get(0).getFirst());
			groupKeysToIterator.put(groupkeyIdx, groupKeysToRegionFList.get(groupkeyIdx).listIterator());
		}
	}

	public List<Double> getNextRec() {

		if (genCount >= rowCount)
			return null;
		if (groupkeys.isEmpty()) {
			genCount++;
			return tuple;
		}
		if (genCount >= numTuples) {
			genCount++;
			return tuple;
		}

		else {
			List<Double> tuple = new ArrayList<>(Collections.nCopies(size, 0.0));
			for (int j = 0; j < mergedColIndxs.size(); j++) {
				tuple.set(mergedColIndxs.get(j), (double) region.at(0).at(j).at(0));
			}
//			ValueCombinationF temp=new ValueCombinationF(tuple,0);
//			if(!temp.isPartOf(expandedRegion));
//				System.out.println("Check");

			for (List<Integer> groupkeyIdx : groupKeysToCurRegionF.keySet()) {
				RegionF curRegion = groupKeysToCurRegionF.get(groupkeyIdx);
//				double[] curPartialTuple=curRegion.generateNextPartialTuple();
				List<Double> curPartialTuple = curRegion.generateNextPartialTuple();
				if (curPartialTuple == null) {// change to next projection interval
					if (!groupKeysToIterator.get(groupkeyIdx).hasNext())
						groupKeysToIterator.put(groupkeyIdx, groupKeysToRegionFList.get(groupkeyIdx).listIterator());
					groupKeysToCurRegionF.put(groupkeyIdx, groupKeysToIterator.get(groupkeyIdx).next());
					curRegion = groupKeysToCurRegionF.get(groupkeyIdx);
					curPartialTuple = curRegion.generateNextPartialTuple();
				}

				// adding the partial tuple attribute to respective columns
				for (int j = 0; j < groupkeyIdx.size(); j++) {
					tuple.set(groupkeyIdx.get(j), curPartialTuple.get(j));
				}

//				System.out.println(tuple);
//
//				for(int j=0;j<allTrueBS.size();j++) {
//					valComb.add((int)Math.floor(tuple.get(j)));
//				}
			}
			genCount++;
			return tuple;
		}
	}

}

package in.ac.iisc.cds.dsl.dirty;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.Set;

import com.microsoft.z3.IntExpr;

import in.ac.iisc.cds.dsl.cdgvendor.model.ValueCombination;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalCondition;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionAggregate;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionAnd;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionOr;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionSimpleInt;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.Symbol;
import in.ac.iisc.cds.dsl.cdgvendor.reducer.BucketStructure;
import in.ac.iisc.cds.dsl.cdgvendor.reducer.Region;
import in.ac.iisc.cds.dsl.cdgvendor.utils.Pair;
import in.ac.iisc.cds.dsl.cdgvendor.utils.Triplet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;

public class DirtyCode {
	
	public static void throwError(String msg) {
		throw new RuntimeException(msg);
	}

	public static void postMakingSummary() {
		DirtyDatabaseSummary dirtyDBSummary = DirtyDatabaseSummary.getInstance();
		dirtyDBSummary.makeFKeyConsistency();
	}
	
	public static void debugSolvingErrorPerConditionForProjections(List<FormalCondition> allAggConditions, List<DirtyValueCombinationWithProjectionValues> dirtyMergedValueCombinations, List<String> sortedColumns) {
		System.out.println("Verifying projection counts...");
		
		if (dirtyMergedValueCombinations.size() != (new HashSet<>(dirtyMergedValueCombinations)).size())
			throwError("Not equal!");
		
		List<List<DirtyValueIntervalWithCountAndInitValue>> intervalsGot = getIntervals(allAggConditions, dirtyMergedValueCombinations, sortedColumns);

		// Intervals may overlap. Overlapping intervals belong to different regions which share same projVar. Overlapping part must contribute once to projection while multiple times to selection
		
		
		for (int i = 0; i < allAggConditions.size(); i++) {
			List<DirtyValueIntervalWithCountAndInitValue> curIntervals = intervalsGot.get(i);
//			List<DirtyValueIntervalWithCountAndInitValue> mergedIntervals = DirtyValueIntervalWithCountAndInitValue.mergeIntervals(curIntervals);
			long gotProjectionValue = getCountUniqueValsFromIntervals(curIntervals);
			long gotSelectionValue = getCountSelectionValsFromIntervals(curIntervals);
			
			FormalCondition formalCondition= allAggConditions.get(i);
			if (formalCondition.getOutputCardinality() != gotSelectionValue)
				System.err.println(gotSelectionValue+ ", Not met selection " + formalCondition);
//				DirtyCode.throwError(gotSelectionValue+ ", Not met selection " + formalCondition);
			
			FormalConditionAggregate formalConditionAggregate = ((FormalConditionAggregate)allAggConditions.get(i));
			if (formalConditionAggregate.isTop()) {
				if (formalConditionAggregate.getProjectionCardinality() != gotProjectionValue)
					System.err.println(gotProjectionValue + ", Not met projection " + formalConditionAggregate);
//					DirtyCode.throwError(gotProjectionValue + ", Not met projection " + formalConditionAggregate);
			} else {
				if (formalConditionAggregate.getProjectionCardinality() > gotProjectionValue)
					System.err.println(gotProjectionValue + ", Not met projection " + formalConditionAggregate);
//					DirtyCode.throwError(gotProjectionValue + ", Not met projection " + formalConditionAggregate);
			}
		}
	}

	private static long getCountSelectionValsFromIntervals(List<DirtyValueIntervalWithCountAndInitValue> list) {
		long count = 0;
		for (DirtyValueIntervalWithCountAndInitValue interval : list) {
			count += interval.getIntervalSizeWithoutProjection();
		}
		return count;
	}

	public static long getCountUniqueValsFromIntervals(List<DirtyValueIntervalWithCountAndInitValue> list) {
		Set<Pair<Long, Long>> uniqueVals = new HashSet<>();
		for (DirtyValueIntervalWithCountAndInitValue interval : list) {
			for(long i = interval.start; i < interval.end; ++i)
				uniqueVals.add(new Pair<Long, Long>(interval.initValue, i));
				
		}
		return uniqueVals.size();
//		long count = 0;
//		for (DirtyValueIntervalWithCountAndInitValue interval : list) {
//			count += interval.getIntervalSizeWithProjection();
//		}
//		return count;
	}

	private static ArrayList<List<DirtyValueIntervalWithCountAndInitValue>> getIntervals(List<FormalCondition> allAggConditions, List<DirtyValueCombinationWithProjectionValues> dirtyMergedValueCombinations, List<String> sortedColumns) {
		ArrayList<List<DirtyValueIntervalWithCountAndInitValue>> intervalsGot = new ArrayList<List<DirtyValueIntervalWithCountAndInitValue>>(allAggConditions.size());
		
		for (int i = 0; i < allAggConditions.size(); i++) {
			intervalsGot.add(new ArrayList<>());
		}
		
		for (DirtyValueCombinationWithProjectionValues valueCombination : dirtyMergedValueCombinations) {
			IntList colValues = valueCombination.getColValues();
            for (int conditionIndex = 0; conditionIndex < allAggConditions.size(); conditionIndex++) {
                FormalCondition aggCondition = allAggConditions.get(conditionIndex);
            	String groupKey = ((FormalConditionAggregate) aggCondition).getGroupKey().get(0);
            	int groupKeyIndex = sortedColumns.indexOf(groupKey);
                if (aggCondition instanceof FormalConditionSimpleInt && meetsSimple(valueCombination, (FormalConditionSimpleInt) aggCondition, sortedColumns)) {
                	List<DirtyValueIntervalWithCountAndInitValue> allIntervalsWithInitValue = getAllIntervalsWithInitValue(valueCombination, groupKeyIndex, colValues.get(groupKeyIndex));
                	intervalsGot.get(conditionIndex).addAll(allIntervalsWithInitValue);
                } else if (aggCondition instanceof FormalConditionAnd && meetsAnd(valueCombination, (FormalConditionAnd) aggCondition, sortedColumns)) {
                	List<DirtyValueIntervalWithCountAndInitValue> allIntervalsWithInitValue = getAllIntervalsWithInitValue(valueCombination, groupKeyIndex, colValues.get(groupKeyIndex));
                	intervalsGot.get(conditionIndex).addAll(allIntervalsWithInitValue);
                } else if (aggCondition instanceof FormalConditionOr && meetsOr(valueCombination, (FormalConditionOr) aggCondition, sortedColumns)) {
                	List<DirtyValueIntervalWithCountAndInitValue> allIntervalsWithInitValue = getAllIntervalsWithInitValue(valueCombination, groupKeyIndex, colValues.get(groupKeyIndex));
                	intervalsGot.get(conditionIndex).addAll(allIntervalsWithInitValue);
                } else if (!(aggCondition instanceof FormalConditionSimpleInt || aggCondition instanceof FormalConditionAnd || aggCondition instanceof FormalConditionOr))
                    throw new RuntimeException("Unrecognized condition " + aggCondition + " of type " + aggCondition.getClass());
            }
        }
		
		return intervalsGot;
	}
	
	private static List<DirtyValueIntervalWithCountAndInitValue> getAllIntervalsWithInitValue(DirtyValueCombinationWithProjectionValues valueCombination, int groupKeyIndex, Integer initValue) {
		LinkedList<DirtyValueIntervalWithCount> intervals = valueCombination.getProjectionValues().getIntervals(groupKeyIndex);
		List<DirtyValueIntervalWithCountAndInitValue> result = new ArrayList<>(intervals.size());
		for (DirtyValueIntervalWithCount interval : intervals) {
			result.add(new DirtyValueIntervalWithCountAndInitValue(interval.start, interval.end, interval.count, initValue));
		}
		return result;
	}

	private static boolean meetsSimple(ValueCombination valueCombination, FormalConditionSimpleInt simpleCondition, List<String> sortedColumns) {
        String colname = simpleCondition.getColumnname();
        int condValue = simpleCondition.getValue();
        Symbol symbol = simpleCondition.getSymbol();

        int colIndx = sortedColumns.indexOf(colname);
        int tupleValue = valueCombination.getColValues().getInt(colIndx);

        switch (symbol) {
            case EQ:
                return tupleValue == condValue;
            case LT:
                return tupleValue < condValue;
            case LE:
                return tupleValue <= condValue;
            case GT:
                return tupleValue > condValue;
            case GE:
                return tupleValue >= condValue;
        }
        throw new RuntimeException("Should not be reaching here");
    }

    private static boolean meetsAnd(ValueCombination valueCombination, FormalConditionAnd andCondition, List<String> sortedColumns) {
        for (FormalCondition condition : andCondition.getConditionList()) {
            if (condition instanceof FormalConditionSimpleInt && !meetsSimple(valueCombination, (FormalConditionSimpleInt) condition, sortedColumns))
                return false;
            else if (condition instanceof FormalConditionAnd && !meetsAnd(valueCombination, (FormalConditionAnd) condition, sortedColumns))
                return false;
            else if (condition instanceof FormalConditionOr && !meetsOr(valueCombination, (FormalConditionOr) condition, sortedColumns))
                return false;
            else if (!(condition instanceof FormalConditionSimpleInt || condition instanceof FormalConditionAnd || condition instanceof FormalConditionOr))
                throw new RuntimeException("Unrecognized condition " + condition + " of type " + condition.getClass());
        }
        return true;
    }

    private static boolean meetsOr(ValueCombination valueCombination, FormalConditionOr orCondition, List<String> sortedColumns) {
        for (FormalCondition condition : orCondition.getConditionList()) {
            if (condition instanceof FormalConditionSimpleInt && meetsSimple(valueCombination, (FormalConditionSimpleInt) condition, sortedColumns))
                return true;
            else if (condition instanceof FormalConditionAnd && meetsAnd(valueCombination, (FormalConditionAnd) condition, sortedColumns))
                return true;
            else if (condition instanceof FormalConditionOr && meetsOr(valueCombination, (FormalConditionOr) condition, sortedColumns))
                return true;
            else if (!(condition instanceof FormalConditionSimpleInt || condition instanceof FormalConditionAnd || condition instanceof FormalConditionOr))
                throw new RuntimeException("Unrecognized condition " + condition + " of type " + condition.getClass());
        }
        return false;
    }

	public static Pair<DirtyVariableValuePairWithProjectionValues, BucketStructure> findBestMatch(DirtyValueCombinationWithProjectionValues lhsValueCombination, LinkedList<DirtyVariableValuePairWithProjectionValues> rhsVarValuePairs, IntList posInLHS, IntList posInRHS) {
		DirtyVariableValuePairWithProjectionValues bestMatch = null;
		int bestMatchMismatchCount = Integer.MAX_VALUE;
		BucketStructure bestMatchVariableBS = null;
		Iterator<DirtyVariableValuePairWithProjectionValues> rhsIt = rhsVarValuePairs.iterator();
		while (rhsIt.hasNext()) {
			DirtyVariableValuePairWithProjectionValues rhsVarValuePair = rhsIt.next();
			if (rhsVarValuePair.rowcount != lhsValueCombination.getRowcount())
				continue;
			if (!isSameProjectionValuesInCommonCols(lhsValueCombination.getProjectionValues(), rhsVarValuePair.getProjectionValues()))
				continue;
			Pair<BucketStructure, Integer> pair = getBSWithMinMismatch(lhsValueCombination.getColValues(), rhsVarValuePair.variable, posInLHS, posInRHS);
			int thisMismatchCount = pair.getSecond();
			if (thisMismatchCount < bestMatchMismatchCount) {
				bestMatchMismatchCount = thisMismatchCount;
				bestMatch = rhsVarValuePair;
				bestMatchVariableBS = pair.getFirst();
			}
		}
		return new Pair<DirtyVariableValuePairWithProjectionValues, BucketStructure>(bestMatch, bestMatchVariableBS);
	}

	private static boolean isSameProjectionValuesInCommonCols(ProjectionValues lhsProjectionValues, ProjectionValues rhsProjectionValues) {
		ProjectionValues lhsCopy = lhsProjectionValues.getDeepCopy();
		ProjectionValues rhsCopy = rhsProjectionValues.getDeepCopy();
		IntList sharedColIndxs = new IntArrayList(lhsCopy.keySet());
		sharedColIndxs.retainAll(rhsCopy.keySet());
		getIntersectingProjectionValues(sharedColIndxs, lhsCopy, rhsCopy);	// Just removing common projection values
		for (Integer colIndx : sharedColIndxs) {
			if (lhsCopy.getIntervals(colIndx).size() != 0)
				return false;
			if (rhsCopy.getIntervals(colIndx).size() != 0)
				return false;
		}
		return true;
	}

	private static Pair<BucketStructure, Integer> getBSWithMinMismatch(IntList lhsColValues, Region var, IntList posInLHS, IntList posInRHS) {
		BucketStructure minMismatchBS = null;
		int minMismatchCount = Integer.MAX_VALUE;
		for (BucketStructure bs : var.getAll()) {
            int thisBSMismatches = getMismatchCount(lhsColValues, bs, posInLHS, posInRHS);
            if (thisBSMismatches < minMismatchCount) {
            	minMismatchCount = thisBSMismatches;
            	minMismatchBS = bs;
            }
        }
		return new Pair<BucketStructure, Integer>(minMismatchBS, minMismatchCount);
	}
	
    private static int getMismatchCount(IntList lhsColValues,BucketStructure bs, IntList posInLHS, IntList posInRHS) {
    	int mismatches = 0;
        for (IntIterator iterLHS = posInLHS.iterator(), iterRHS = posInRHS.iterator(); iterLHS.hasNext();) {
            int posL = iterLHS.nextInt();
            int posR = iterRHS.nextInt();

            if (!bs.at(posR).contains(lhsColValues.getInt(posL)))
            	mismatches++;
        }
        return mismatches;
    }
    
    public static ProjectionValues getIntersectingProjectionValues(IntList sharedColIndxs, ProjectionValues lhsProjectionValues, ProjectionValues rhsProjectionValues) {
		ProjectionValues intersectingValues = new ProjectionValues();
		for (int i = 0; i < sharedColIndxs.size(); ++i) {		// For each common column
			int sharedColIndx = sharedColIndxs.get(i);
			LinkedList<DirtyValueIntervalWithCount> lhsIntervals = lhsProjectionValues.getIntervals(sharedColIndx);
			LinkedList<DirtyValueIntervalWithCount> rhsIntervals = rhsProjectionValues.getIntervals(sharedColIndx);
			if (lhsIntervals == null && rhsIntervals == null)		// No projection values for this column
				continue;
			if (lhsIntervals == null || rhsIntervals == null)		// Projection value present only for one but not both of them
        		throw new RuntimeException("Should not happen!");
			
			for(ListIterator<DirtyValueIntervalWithCount> lhsItreator = lhsIntervals.listIterator(); lhsItreator.hasNext();) {
				DirtyValueIntervalWithCount lhsInterval = lhsItreator.next();
				for(ListIterator<DirtyValueIntervalWithCount> rhsItreator = rhsIntervals.listIterator(); rhsItreator.hasNext();) {
					DirtyValueIntervalWithCount rhsInterval = rhsItreator.next();

					DirtyValueIntervalWithCount common = lhsInterval.getIntersection(rhsInterval);
					if (common != null) {
						intersectingValues.addToList(sharedColIndx, common);
						lhsInterval = updateInterval(lhsItreator, lhsInterval.getMinus(common));
						updateInterval(rhsItreator, rhsInterval.getMinus(common));
						if (lhsInterval == null)
							break;
					}
				}
			}
    	}
		return intersectingValues;
	}
	
	public static DirtyValueIntervalWithCount updateInterval(ListIterator<DirtyValueIntervalWithCount> iterator, Triplet<DirtyValueIntervalWithCount, DirtyValueIntervalWithCount, DirtyValueIntervalWithCount> leftover) {
		DirtyValueIntervalWithCount updatedCurInterval;
		if (leftover.getSecond() != null) {		// Leftover from common part
			iterator.set(leftover.getSecond());
			updatedCurInterval = leftover.getSecond();
		} else {
			iterator.remove();
			updatedCurInterval = null;
		}
		if (leftover.getFirst() != null) {		// Leftover from left
			iterator.add(leftover.getFirst());
			iterator.previous();
		}
		if (leftover.getThird() != null) {		// Leftover from right
			iterator.add(leftover.getThird());
			iterator.previous();
		}
		return updatedCurInterval;
	}

	public static void mergeValueInterval(LinkedList<DirtyValueInterval> existingIntervals, DirtyValueInterval intervalToMerge) {
		if (existingIntervals.size() == 0) {
			existingIntervals.add(new DirtyValueInterval(intervalToMerge.start, intervalToMerge.end));
			return;
		}
		ListIterator<DirtyValueInterval> iterator = existingIntervals.listIterator();
		DirtyValueInterval curInterval = null;
		do {
			curInterval = iterator.next();
			if (curInterval.start <= intervalToMerge.start) {
				if (intervalToMerge.start < curInterval.end) {		// curInterval intersects intervalToMerge
					if (curInterval.end >= intervalToMerge.end) {	// curInterval covers intervalToMerge
						return;
					}
					long start = curInterval.start;
					iterator.remove();
					while (iterator.hasNext()) {
						curInterval = iterator.next();
						if (intervalToMerge.end <= curInterval.start) {
							iterator.previous();
							iterator.add(new DirtyValueInterval(start, intervalToMerge.end));
							return;
						} else if (intervalToMerge.end <= curInterval.end) {
							iterator.remove();
							iterator.add(new DirtyValueInterval(start, curInterval.end));
							return;
						} else {
							iterator.remove();
						}
					}
					iterator.add(new DirtyValueInterval(start, intervalToMerge.end));
					return;
				}
				else		// curInterval is before intervalToMerge
					continue;
			} else {
				if (intervalToMerge.end <= curInterval.start) {		// No intersection
					iterator.previous();
					iterator.add(new DirtyValueInterval(intervalToMerge.start, intervalToMerge.end));
					return;
				} else if (intervalToMerge.end <= curInterval.end) {
					iterator.remove();
					iterator.add(new DirtyValueInterval(intervalToMerge.start, curInterval.end));
					return;
				} else {
					iterator.remove();
					while (iterator.hasNext()) {
						curInterval = iterator.next();
						if (intervalToMerge.end <= curInterval.start) {
							iterator.previous();
							iterator.add(new DirtyValueInterval(intervalToMerge.start, intervalToMerge.end));
							return;
						} else {
							if (intervalToMerge.end <= curInterval.end)
								iterator.set(new DirtyValueInterval(intervalToMerge.start, curInterval.end));
							else
								iterator.set(new DirtyValueInterval(intervalToMerge.start, intervalToMerge.end));
							return;
						}
					}
					iterator.add(new DirtyValueInterval(intervalToMerge.start, intervalToMerge.end));
					return;
				}
			}
		} while (iterator.hasNext());
		iterator.add(new DirtyValueInterval(intervalToMerge.start, intervalToMerge.end));
	}

	public static DirtyValueInterval getSuitableIntervalForProjVar(long count, LinkedList<DirtyValueInterval> availableIntervals, LinkedList<DirtyValueInterval> existingIntervals) {
		DirtyValueInterval bestInterval = null;
		if (existingIntervals != null && existingIntervals.size() != 0) {
			bestInterval = getBestInterval(count, existingIntervals);
		}
		if (bestInterval == null) {
			bestInterval = getBestInterval(count, availableIntervals);
		}
		if (bestInterval == null)
			throw new RuntimeException("Can't find any interval!");
		if (bestInterval.getIntervalSizeWithProjection() < 1) {
			throw new RuntimeException("WTF");
		}
		return bestInterval;
	}

	private static DirtyValueInterval getBestInterval(long count, LinkedList<DirtyValueInterval> allIntervals) {
		for (DirtyValueInterval interval : allIntervals) {
			if (interval.getIntervalSizeWithProjection() >= count)
				return new DirtyValueInterval(interval.start, interval.start + count);
		}
		return null;
	}

	public static void removeFromIntervals(DirtyValueInterval intervalToRemove, LinkedList<DirtyValueInterval> allIntervals) {
		long intervalToRemoveStart = intervalToRemove.start;
		long intervalToRemoveEnd = intervalToRemove.end;
		ListIterator<DirtyValueInterval> iterator = allIntervals.listIterator();
		while (iterator.hasNext()) {
			DirtyValueInterval curInterval = iterator.next();
			if (curInterval.end <= intervalToRemoveStart)
				continue;
			if (intervalToRemoveEnd <= curInterval.start)
				return;
			if (curInterval.start <= intervalToRemoveStart) {
				if (curInterval.end <= intervalToRemoveEnd) {
					if (intervalToRemoveStart - curInterval.start > 0)
						iterator.set(new DirtyValueInterval(curInterval.start, intervalToRemoveStart));
					else
						iterator.remove();
					intervalToRemoveStart = curInterval.end;
				} else {
					iterator.remove();
					if (intervalToRemoveStart - curInterval.start > 0)
						iterator.add(new DirtyValueInterval(curInterval.start, intervalToRemoveStart));
					if (curInterval.end - intervalToRemoveEnd > 0)
						iterator.add(new DirtyValueInterval(intervalToRemoveEnd, curInterval.end));
				}
			} else {
				if (curInterval.end <= intervalToRemoveEnd) {
					iterator.remove();
					intervalToRemoveStart = curInterval.end;
				} else {
					iterator.set(new DirtyValueInterval(intervalToRemoveEnd, curInterval.end));
					return;
				}
			}
		}
	}
	
	/*
	 * Modifies first
	 */
	public static void intersectWithIntervals(LinkedList<DirtyValueInterval> first, LinkedList<DirtyValueInterval> second, long maxSize) {
		LinkedList<DirtyValueInterval> secondInverse = getInverseOfIntervals(second, maxSize);
		for (DirtyValueInterval interval : secondInverse) {
			removeFromIntervals(interval, first);
		}
	}

	private static LinkedList<DirtyValueInterval> getInverseOfIntervals(LinkedList<DirtyValueInterval> intervals, long maxSize) {
		LinkedList<DirtyValueInterval> result = new LinkedList<>();
		long start = 0, end;
		for (DirtyValueInterval interval : intervals) {
			end = interval.start;
			if (end - start > 0)
				result.add(new DirtyValueInterval(start, end));
			start = interval.end;
		}
		end = maxSize;
		if (end - start > 0)
			result.add(new DirtyValueInterval(start, end));
		return result;
	}

	public static LinkedList<DirtyValueInterval> createCopyIntervals(LinkedList<DirtyValueInterval> intervals) {
		LinkedList<DirtyValueInterval> result = new LinkedList<>();
		for (DirtyValueInterval interval : intervals) {
			result.add(new DirtyValueInterval(interval.start, interval.end));
		}
		return result;
	}
}
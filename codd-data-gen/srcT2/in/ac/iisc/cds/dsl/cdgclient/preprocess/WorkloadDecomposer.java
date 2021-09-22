package in.ac.iisc.cds.dsl.cdgclient.preprocess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
// import  org.jgrapht.alg.color;
import in.ac.iisc.cds.dsl.cdgvendor.model.ViewInfo;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalCondition;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionAggregate;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionAnd;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionBaseAggregate;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionComposite;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionSOP;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionSimpleInt;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.Symbol;
import in.ac.iisc.cds.dsl.cdgvendor.reducer.Bucket;
import in.ac.iisc.cds.dsl.cdgvendor.reducer.BucketStructure;
import in.ac.iisc.cds.dsl.cdgvendor.reducer.Region;
import in.ac.iisc.cds.dsl.cdgvendor.solver.CliqueFinder;
import in.ac.iisc.cds.dsl.cdgvendor.solver.DomainDecomposer;
//import in.ac.iisc.cds.dsl.cdgvendor.solver.For;
import in.ac.iisc.cds.dsl.cdgvendor.utils.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

public class WorkloadDecomposer {
	private final boolean regionWise = false;
	private final boolean maximizeWorkload0 = false;
	private List<List<String>> workloads;

	private UndirectedGraph<String, DefaultEdge> workloadIncompatibilityGraph;
	//private UndirectedGraph<String, DefaultEdge> workloadIncompatibilityGraphTemp;
	// private Map<String,Integer>vertexToWorkloadNo;
	private Map<Integer, List<String>> workloadNoToQueryTemp;

	public WorkloadDecomposer() {
		workloads = new ArrayList<>();
		workloadIncompatibilityGraph = new SimpleGraph<>(DefaultEdge.class);
//		workloadIncompatibilityGraphTemp = new SimpleGraph<>(DefaultEdge.class);
		// vertexToWorkloadNo=new HashMap<>();
		workloadNoToQueryTemp = new HashMap<>();
	}

	// public List<List<String>> decompose(conditions)
	public List<List<String>> decompose() {
		List<String> vList = new ArrayList<>();
		vList.addAll(workloadIncompatibilityGraph.vertexSet());
//		List<Integer>degrees=new ArrayList<>();
//		for(String v:vList) {
//			degrees.add(workloadIncompatibilityGraph.degreeOf(v));
//		}
//		for(int i=0;i<degrees.size();i++) {
//			int min=degrees.get(i);
//			int pos=i;
//			for(int j=i+1;j<degrees.size();j++)
//				if(degrees.get(j)<min) {
//					min =degrees.get(j);
//					pos=j;
//				}
//			degrees.set(pos,degrees.get(i));
//			degrees.set(i,min);
//			
//			String temp=vList.get(i);
//			vList.set(i, vList.get(pos));
//			vList.set(pos, temp);
//		}
		// TODO Auto-generated method stub
		int count = 0;
		while (count++< 500) {
			Map<Integer, List<String>> workloadNoToQuery = new HashMap<>();
			//List<String> vList1 = new ArrayList<>();
			//vList.addAll(workloadIncompatibilityGraph.vertexSet());
			List<Integer> colorsUsed = new ArrayList<>();
			colorsUsed.add(0);
			int totCol = 1;
//		for(int i=0;i<vList.size();i++) 
//			colors.add(i);
//		vertexToWorkloadNo.put(vList.get(0), 0);
//		for(int i=1;i<vList.size();i++) {
//			String v=vList.get(i);
//			List<String>neighbours=Graphs.neighborListOf(workloadIncompatibilityGraph, v);
//			boolean found;
//			for(int color=0;color<vList.size();color++) {
//				found=true;
//				for(String u:neighbours) {
//					if(vertexToWorkloadNo.containsKey(u)) {
//						if(vertexToWorkloadNo.get(u)==color)
//						{
//							found=false;
//						
//							break;
//						}
//						
//					}
//				}
//				if(found) {
//					vertexToWorkloadNo.put(v, color);
//					break;
//				}
//			}
//		}
//		workloadNoToQuery.put(0, new ArrayList<>());
//		workloadNoToQuery.get(0).add(vList.get(0));
			workloadNoToQuery.put(0, new ArrayList<>());
			workloadNoToQuery.get(0).add(vList.get(0));// adding frist vertex to the first color;
			
			for (int i = 1; i < vList.size(); i++) {
				if(!maximizeWorkload0)
					Collections.shuffle(colorsUsed);
				String v = vList.get(i);
				List<String> neighbours = Graphs.neighborListOf(workloadIncompatibilityGraph, v);
				boolean found = false;
				for (Integer color : colorsUsed) {
					// if(workloadNoToQuery.containsKey(color)) {
					List<String> queries = new ArrayList<>();
					queries.addAll(workloadNoToQuery.get(color));
					queries.retainAll(neighbours);
					if (queries.isEmpty()) {
						workloadNoToQuery.get(color).add(v);
						found = true;
						break;
					}
				}
				if (!found) {
					workloadNoToQuery.put(totCol, new ArrayList<>());
					workloadNoToQuery.get(totCol).add(v);
					colorsUsed.add(totCol++);

				}
//			for(int color=0;color<vList.size();color++) {
//				if(workloadNoToQuery.containsKey(color)) {
//					List<String>queries=new ArrayList<>();
//					queries.addAll(workloadNoToQuery.get(color));
//					queries.retainAll(neighbours);
//					if(queries.isEmpty()) {
//						workloadNoToQuery.get(color).add(v);
//					break;
//					}
//				}
//				else {
//					workloadNoToQuery.put(color, new ArrayList<>());
//					workloadNoToQuery.get(color).add(v);
//					break;
//				}
//					
//			}
			}
			if (count == 1)
				workloadNoToQueryTemp = workloadNoToQuery;

			else if (workloadNoToQueryTemp.size() > workloadNoToQuery.size())
				workloadNoToQueryTemp = workloadNoToQuery;
			 //System.out.println(workloadNoToQuery.size());
//			if(workloadNoToQueryTemp.size()<=6)
//				break;
		}
		System.out.println(workloadNoToQueryTemp.size());
		
		
//		 count = 0;
//		while (count++ < 100) {
//			Map<Integer, List<String>> workloadNoToQuery = new HashMap<>();
//			List<String> vList = new ArrayList<>();
//			vList.addAll(workloadIncompatibilityGraph.vertexSet());
//			List<Integer> colorsUsed = new ArrayList<>();
//			colorsUsed.add(0);
//			int totCol = 1;
//			
//			//workloadIncompatibilityGraph.degreeOf(arg0)
//			//inDegreeOf()
////		for(int i=0;i<vList.size();i++) 
////			colors.add(i);
////		vertexToWorkloadNo.put(vList.get(0), 0);
////		for(int i=1;i<vList.size();i++) {
////			String v=vList.get(i);
////			List<String>neighbours=Graphs.neighborListOf(workloadIncompatibilityGraph, v);
////			boolean found;
////			for(int color=0;color<vList.size();color++) {
////				found=true;
////				for(String u:neighbours) {
////					if(vertexToWorkloadNo.containsKey(u)) {
////						if(vertexToWorkloadNo.get(u)==color)
////						{
////							found=false;
////						
////							break;
////						}
////						
////					}
////				}
////				if(found) {
////					vertexToWorkloadNo.put(v, color);
////					break;
////				}
////			}
////		}
////		workloadNoToQuery.put(0, new ArrayList<>());
////		workloadNoToQuery.get(0).add(vList.get(0));
//			workloadNoToQuery.put(0, new ArrayList<>());
//			workloadNoToQuery.get(0).add(vList.get(0));// adding frist vertex to the first color;
//			
//			for (int i = 1; i < vList.size(); i++) {
//				Collections.shuffle(colorsUsed);
//				String v = vList.get(i);
//				List<String> neighbours = Graphs.neighborListOf(workloadIncompatibilityGraph, v);
//				boolean found = false;
//				for (Integer color : colorsUsed) {
//					// if(workloadNoToQuery.containsKey(color)) {
//					List<String> queries = new ArrayList<>();
//					queries.addAll(workloadNoToQuery.get(color));
//					queries.retainAll(neighbours);
//					if (queries.isEmpty()) {
//						workloadNoToQuery.get(color).add(v);
//						found = true;
//						break;
//					}
//				}
//				if (!found) {
//					workloadNoToQuery.put(totCol, new ArrayList<>());
//					workloadNoToQuery.get(totCol).add(v);
//					colorsUsed.add(totCol++);
//
//				}
////			for(int color=0;color<vList.size();color++) {
////				if(workloadNoToQuery.containsKey(color)) {
////					List<String>queries=new ArrayList<>();
////					queries.addAll(workloadNoToQuery.get(color));
////					queries.retainAll(neighbours);
////					if(queries.isEmpty()) {
////						workloadNoToQuery.get(color).add(v);
////					break;
////					}
////				}
////				else {
////					workloadNoToQuery.put(color, new ArrayList<>());
////					workloadNoToQuery.get(color).add(v);
////					break;
////				}
////					
////			}
//			}
//			if (count == 1)
//				workloadNoToQueryTemp = workloadNoToQuery;
//
//			else if (workloadNoToQueryTemp.size() > workloadNoToQuery.size())
//				workloadNoToQueryTemp = workloadNoToQuery;
//			 System.out.println(workloadNoToQuery.size());
//		}

		
		
		
		
//		count = 0;
//		while (count++ < 100) {
//			Map<Integer, List<String>> workloadNoToQuery = new HashMap<>();
//			//List<String> vList1 = new ArrayList<>();
//			//vList.addAll(workloadIncompatibilityGraph.vertexSet());
//			List<Integer> colorsUsed = new ArrayList<>();
//			colorsUsed.add(0);
//			int totCol = 1;
////		for(int i=0;i<vList.size();i++) 
////			colors.add(i);
////		vertexToWorkloadNo.put(vList.get(0), 0);
////		for(int i=1;i<vList.size();i++) {
////			String v=vList.get(i);
////			List<String>neighbours=Graphs.neighborListOf(workloadIncompatibilityGraph, v);
////			boolean found;
////			for(int color=0;color<vList.size();color++) {
////				found=true;
////				for(String u:neighbours) {
////					if(vertexToWorkloadNo.containsKey(u)) {
////						if(vertexToWorkloadNo.get(u)==color)
////						{
////							found=false;
////						
////							break;
////						}
////						
////					}
////				}
////				if(found) {
////					vertexToWorkloadNo.put(v, color);
////					break;
////				}
////			}
////		}
////		workloadNoToQuery.put(0, new ArrayList<>());
////		workloadNoToQuery.get(0).add(vList.get(0));
//			workloadNoToQuery.put(0, new ArrayList<>());
//			workloadNoToQuery.get(0).add(vList.get(0));// adding frist vertex to the first color;
//			
//			for (int i = 1; i < vList.size(); i++) {
//				Collections.shuffle(colorsUsed);
//				String v = vList.get(i);
//				List<String> neighbours = Graphs.neighborListOf(workloadIncompatibilityGraphTemp, v);
//				boolean found = false;
//				for (Integer color : colorsUsed) {
//					// if(workloadNoToQuery.containsKey(color)) {
//					List<String> queries = new ArrayList<>();
//					queries.addAll(workloadNoToQuery.get(color));
//					queries.retainAll(neighbours);
//					if (queries.isEmpty()) {
//						workloadNoToQuery.get(color).add(v);
//						found = true;
//						break;
//					}
//				}
//				if (!found) {
//					workloadNoToQuery.put(totCol, new ArrayList<>());
//					workloadNoToQuery.get(totCol).add(v);
//					colorsUsed.add(totCol++);
//
//				}
////			for(int color=0;color<vList.size();color++) {
////				if(workloadNoToQuery.containsKey(color)) {
////					List<String>queries=new ArrayList<>();
////					queries.addAll(workloadNoToQuery.get(color));
////					queries.retainAll(neighbours);
////					if(queries.isEmpty()) {
////						workloadNoToQuery.get(color).add(v);
////					break;
////					}
////				}
////				else {
////					workloadNoToQuery.put(color, new ArrayList<>());
////					workloadNoToQuery.get(color).add(v);
////					break;
////				}
////					
////			}
//			}
//			if (count == 1)
//				workloadNoToQueryTemp = workloadNoToQuery;
//
//			else if (workloadNoToQueryTemp.size() > workloadNoToQuery.size())
//				workloadNoToQueryTemp = workloadNoToQuery;
//			// System.out.println(workloadNoToQuery.size());
//		}
//		 System.out.println(workloadNoToQueryTemp.size());

		return new ArrayList<>(workloadNoToQueryTemp.values());
	}

	public void addConditions(List<FormalCondition> conditions, ViewInfo viewInfo) {
		// TODO Auto-generated method stub
		for (ListIterator<FormalCondition> iter = conditions.listIterator(); iter.hasNext();) {
			FormalCondition fc = iter.next();
			if (!(fc instanceof FormalConditionAggregate))
				iter.remove();
			else {
				if (((FormalConditionAggregate) fc).isTop() == false)

					iter.remove();

				// else if(
			}
		}

		for (ListIterator<FormalCondition> iter = conditions.listIterator(); iter.hasNext();) {
			FormalCondition fc = iter.next();
			workloadIncompatibilityGraph.addVertex(fc.getQueryName());
			//workloadIncompatibilityGraphTemp.addVertex(fc.getQueryName());//temp

		}
		if (conditions.size() <= 1)
			return;
		if (regionWise) {
			Map<String, IntList> bucketFloorsByColumns = DomainDecomposer.getBucketFloors(conditions, viewInfo);

			final List<String> sortedViewColumns = new ArrayList<>(bucketFloorsByColumns.keySet());
			Collections.sort(sortedViewColumns);

			// Marking buckets for each column as true
			double varcountUR = 1;
			final List<boolean[]> allTrueBS = new ArrayList<>();
			for (String column : sortedViewColumns) {
				int length = bucketFloorsByColumns.get(column).size();
				boolean[] arr = new boolean[length];
				varcountUR *= length;

				Arrays.fill(arr, true);
				allTrueBS.add(arr);
			}
			CliqueFinder cliqueReducer = new CliqueFinder(viewInfo, allTrueBS);
			List<Set<String>> arasuCliques = cliqueReducer.getOrderedNonTrivialCliques(conditions);
			List<Set<List<String>>> cliqueToGroupkeys = new ArrayList<>();
			for (int i = 0; i < arasuCliques.size(); i++) {
				Set<List<String>> groupkeys = new HashSet<>();
				Set<String> clique = arasuCliques.get(i); // set of columns

				for (FormalCondition condition : conditions) {

					Set<String> appearingCols = new HashSet<>();
					getAppearingCols(appearingCols, condition); // appearing columns will have columns appeared in
																// current condition
					if (clique.containsAll(appearingCols)) {
						List<String> groupkey = ((FormalConditionAggregate) (condition)).getGroupKey();
						groupkeys.add(groupkey);
					}
				}
				cliqueToGroupkeys.add(groupkeys);
			}

			// got a group key list for each clique
			boolean mergeHappened = true;
			while (mergeHappened) {
				mergeHappened = false;
				for (int i = 0; i < arasuCliques.size(); i++) {

					Set<String> clique1 = arasuCliques.get(i); // set of columns
					Set<List<String>> groupkeys1 = cliqueToGroupkeys.get(i);
					Set<String> mergedClique = new HashSet<>();
					mergedClique.addAll(clique1);
					Set<List<String>> mergedGroupkeys = new HashSet<>();
					mergedGroupkeys.addAll(groupkeys1);
					IntList toRemove = new IntArrayList();
					for (int j = i + 1; j < arasuCliques.size(); j++) {
						Set<String> clique2 = arasuCliques.get(j); // set of columns
						Set<List<String>> groupkeys2 = cliqueToGroupkeys.get(j);
						// check intersection more thoroughly

						if (haveConflict(mergedGroupkeys, groupkeys2)) {

							// mergedClique.addAll(clique1);
							mergedClique.addAll(clique2);
							mergedGroupkeys.addAll(groupkeys2);
							mergeHappened = true;

							toRemove.add(j);

						}
					}
					Collections.sort(toRemove);
					arasuCliques.set(i, mergedClique);
					cliqueToGroupkeys.set(i, mergedGroupkeys);
					if (toRemove.size() != 0) {
						for (int idx = toRemove.size() - 1; idx >= 0; idx--) {
							arasuCliques.remove((int) toRemove.get(idx));
							cliqueToGroupkeys.remove((int) toRemove.get(idx));
						}
					}

				}
			}

			// form regions and check conflicting queries
			//
			List<List<Pair<FormalCondition, Region>>> cliqueToConditionAndRegion = new ArrayList<>();
			for (int i = 0; i < arasuCliques.size(); i++) {
				List<Pair<FormalCondition, Region>> conditionsAndRegions = new ArrayList<>();
				Set<String> clique = arasuCliques.get(i); // set of columns

				// codeS

				for (int j = 0; j < conditions.size(); j++) {
					FormalCondition condition = conditions.get(j);
					Set<String> appearingCols = new HashSet<>();
					getAppearingCols(appearingCols, condition); // appearing columns will have columns appeared in
																// current condition

					if (clique.containsAll(appearingCols)) {
						Region cRegion = getConditionRegion(condition, allTrueBS, sortedViewColumns,
								bucketFloorsByColumns);
						Pair<FormalCondition, Region> p = new Pair<FormalCondition, Region>(condition, cRegion);
						conditionsAndRegions.add(p);
					}
				}
				cliqueToConditionAndRegion.add(conditionsAndRegions);

			}
			// takes a lot of time for t17
			for (int cliqueIdx = 0; cliqueIdx < arasuCliques.size(); cliqueIdx++) {
				List<Pair<FormalCondition, Region>> conditionsAndRegions = cliqueToConditionAndRegion.get(cliqueIdx);
				for (int i = 0; i < conditionsAndRegions.size(); i++) {
					Pair<FormalCondition, Region> conditionAndRegion1 = conditionsAndRegions.get(i);
					FormalCondition condition1 = conditionAndRegion1.getFirst();
					for (int j = i + 1; j < conditionsAndRegions.size(); j++) {
						Pair<FormalCondition, Region> conditionAndRegion2 = conditionsAndRegions.get(j);
						FormalCondition condition2 = conditionAndRegion2.getFirst();
						if (overlappingGrouppkeys(((FormalConditionAggregate) condition1).getGroupKey(),
								((FormalConditionAggregate) condition2).getGroupKey())) {
							if(!(conditionAndRegion1.getSecond().intersection(conditionAndRegion2.getSecond())).isEmpty())
								workloadIncompatibilityGraph.addEdge(condition1.getQueryName(), condition2.getQueryName());
						}

					}
				}
			}
		}
		else {
			for(int i=0;i<conditions.size();i++) {
				List<String>groupkeys1=((FormalConditionAggregate)conditions.get(i)).getGroupKey();
				for(int j=i+1;j<conditions.size();j++) {
					List<String>groupkeys2=((FormalConditionAggregate)conditions.get(j)).getGroupKey();
//					List<String> common = new ArrayList<>();
////					if (groupkeys1.equals(groupkeys2))
////						continue;
//					common.addAll(groupkeys1);
//					common.retainAll(groupkeys2);
//					if (!common.isEmpty() && !groupkeys1.equals(groupkeys2))
					if(overlappingGrouppkeys(groupkeys1, groupkeys2))
						workloadIncompatibilityGraph.addEdge(conditions.get(i).getQueryName(), conditions.get(j).getQueryName());
				}
			}
		}
		// DepthFirstIterator depthFirstIterator
		// = new DepthFirstIterator<>(directedGraph);
		// workloadIncompatibilityGraph.colo

	}

	private boolean overlappingGrouppkeys(List<String> groupKey, List<String> groupKey2) {
		// TODO Auto-generated method stub
		List<String> intersection = new ArrayList<>();
		intersection.addAll(groupKey);
		intersection.retainAll(groupKey2);
		if (intersection.isEmpty())
			return false;
		else if (groupKey.equals(groupKey2))
			return false;
		return true;
	}

	public static void getAppearingCols(Set<String> attributesFound, FormalCondition condition) {

		if (condition instanceof FormalConditionComposite) {
			FormalConditionComposite composite = (FormalConditionComposite) condition;
			for (FormalCondition innerCondition : composite.getConditionList()) {
				getAppearingCols(attributesFound, innerCondition);
			}

		} else if (condition instanceof FormalConditionSimpleInt) {
			FormalConditionSimpleInt simple = (FormalConditionSimpleInt) condition;
			attributesFound.add(simple.getColumnname());

		} else if (condition instanceof FormalConditionBaseAggregate) {
		} else
			throw new RuntimeException("Unrecognized type of FormalCondition " + condition.getClass());

		if (condition instanceof FormalConditionAggregate) { // Adding those attributes which are part of group key
			attributesFound.addAll(((FormalConditionAggregate) condition).getGroupKey());
		}
	}

	private boolean haveConflict(Set<List<String>> groupkeys1, Set<List<String>> groupkeys2) {
		// checks whether groupkeys from differenct cliques have conflict. a condlict
		// will arise under the assumptions of non overlapping groupkeys when two
		// groupkeys of diff cliques have asubset superset relation.
		// Hence, its enough that the groupkeys have a common attribute.
		for (List<String> groupkey1 : groupkeys1) {
			for (List<String> groupkey2 : groupkeys2) {
				List<String> common = new ArrayList<>();
				common.addAll(groupkey1);
				common.retainAll(groupkey2);
				if (common.size() != 0)
					return true;
			}
		}
		return false;
	}

	// remove and use z3 to call later

	private Region getConditionRegion(FormalCondition condition, List<boolean[]> allTrueBS, List<String> sortedColumns,
			Map<String, IntList> bucketFloorsByColumns) {

		List<FormalCondition> subconditions = getSOPSubconditions(condition);
		Region conditionRegion = new Region();

		for (FormalCondition subcondition : subconditions) { // which buckets follow this particular subcondition
			List<boolean[]> bsCopy = deepCopyBS(allTrueBS);

			// Unmarking false buckets
			setFalseAppropriateBuckets(subcondition, sortedColumns, bsCopy, bucketFloorsByColumns);// assert: every
																									// element of
																									// bucketStructures
																									// has at least one
																									// true entry

			BucketStructure subConditionBS = new BucketStructure();
			for (int j = 0; j < bsCopy.size(); j++) {
				Bucket bucket = new Bucket();
				for (int k = 0; k < bsCopy.get(j).length; k++) {
					if (bsCopy.get(j)[k]) {
						bucket.add(k); // What split points of this dimension follow this sub constraint (Important
										// Note: index of split points in bucketFloorsByColumns is being added in bucket
										// instead of split point value)
					}
				}
				subConditionBS.add(bucket); // For particular dimension
			}
			conditionRegion.add(subConditionBS); // For every subcondition in condition
		}
		return conditionRegion;
	}

	private List<FormalCondition> getSOPSubconditions(FormalCondition condition) {
		FormalConditionSOP sopCondition = new FormalConditionSOP(condition);
		return sopCondition.getConditionList();
	}

	private static void setFalseAppropriateBuckets(FormalCondition condition, List<String> sortedColumns,
			List<boolean[]> bucketStructures, Map<String, IntList> bucketFloorsByColumns) {

		if (condition instanceof FormalConditionAnd) {
			setFalseAppropriateBuckets((FormalConditionAnd) condition, sortedColumns, bucketStructures,
					bucketFloorsByColumns);
			return;
		}
		if (condition instanceof FormalConditionSimpleInt) {
			setFalseAppropriateBuckets((FormalConditionSimpleInt) condition, sortedColumns, bucketStructures,
					bucketFloorsByColumns);
			return;
		}
		throw new RuntimeException("Unsupported FormalCondition of type" + condition.getClass() + " " + condition);
	}

	private static void setFalseAppropriateBuckets(FormalConditionAnd andCondition, List<String> sortedColumns,
			List<boolean[]> bucketStructures, Map<String, IntList> bucketFloorsByColumns) {
		for (FormalCondition condition : andCondition.getConditionList()) {
			setFalseAppropriateBuckets(condition, sortedColumns, bucketStructures, bucketFloorsByColumns);
		}
	}

	private static void setFalseAppropriateBuckets(FormalConditionSimpleInt simpleCondition, List<String> sortedColumns,
			List<boolean[]> bucketStructures, Map<String, IntList> bucketFloorsByColumns) {

		String columnname = simpleCondition.getColumnname();
		int columnIndex = sortedColumns.indexOf(columnname);
		boolean[] bucketStructure = bucketStructures.get(columnIndex);
		IntList bucketFloors = bucketFloorsByColumns.get(columnname);

		Symbol symbol = simpleCondition.getSymbol();
		long val = simpleCondition.getValue();

		switch (symbol) {
		case LT:
			for (int i = bucketFloors.size() - 1; i >= 0 && bucketFloors.getInt(i) >= val; i--) {
				bucketStructure[i] = false;
			}
			break;
		case LE:
			for (int i = bucketFloors.size() - 1; i >= 0 && bucketFloors.getInt(i) > val; i--) {
				bucketStructure[i] = false;
			}
			break;
		case GT:
			for (int i = 0; i < bucketFloors.size() && bucketFloors.getInt(i) <= val; i++) {
				bucketStructure[i] = false;
			}
			break;
		case GE:
			for (int i = 0; i < bucketFloors.size() && bucketFloors.getInt(i) < val; i++) {
				bucketStructure[i] = false;
			}
			break;
		case EQ:
			for (int i = 0; i < bucketFloors.size(); i++) {
				if (bucketFloors.getInt(i) != val) {
					bucketStructure[i] = false;
				}
			}
			break;
		default:
			throw new RuntimeException("Unrecognized Symbol " + symbol);
		}
	}

	private static List<boolean[]> deepCopyBS(List<boolean[]> bucketStructures) {
		List<boolean[]> bucketStructuresCopy = new ArrayList<>();
		for (boolean[] bucketStructure : bucketStructures) {
			boolean[] bucketStructureCopy = Arrays.copyOf(bucketStructure, bucketStructure.length);
			bucketStructuresCopy.add(bucketStructureCopy);
		}
		return bucketStructuresCopy;
	}

}

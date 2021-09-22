package in.ac.iisc.cds.dsl.cdgvendor.solver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import in.ac.iisc.cds.dsl.cdgvendor.model.ProjectionVariable;
import in.ac.iisc.cds.dsl.cdgvendor.model.ProjectionVariableOptimized;
import in.ac.iisc.cds.dsl.cdgvendor.model.SolverViewStats;
import in.ac.iisc.cds.dsl.cdgvendor.model.VariableValuePair;
import in.ac.iisc.cds.dsl.cdgvendor.model.ViewInfo;
import in.ac.iisc.cds.dsl.cdgvendor.model.ViewSolutionWithSolverStats;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalCondition;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionAggregate;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionAnd;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionSOP;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionSimpleInt;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.Symbol;
import in.ac.iisc.cds.dsl.cdgvendor.reducer.Bucket;
import in.ac.iisc.cds.dsl.cdgvendor.reducer.BucketF;
import in.ac.iisc.cds.dsl.cdgvendor.reducer.BucketStructure;
import in.ac.iisc.cds.dsl.cdgvendor.reducer.BucketStructureF;
import in.ac.iisc.cds.dsl.cdgvendor.reducer.Partition;
import in.ac.iisc.cds.dsl.cdgvendor.reducer.Reducer;
import in.ac.iisc.cds.dsl.cdgvendor.reducer.Region;
import in.ac.iisc.cds.dsl.cdgvendor.reducer.RegionF;
import in.ac.iisc.cds.dsl.cdgvendor.reducer.RegionSummary;
import in.ac.iisc.cds.dsl.cdgvendor.solver.Z3Solver.ConsistencyMakerType;
import in.ac.iisc.cds.dsl.cdgvendor.utils.DebugHelper;
import in.ac.iisc.cds.dsl.cdgvendor.utils.Pair;
import in.ac.iisc.cds.dsl.cdgvendor.utils.StopWatch;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

public class DoubleReductionBasedSolverNew extends AbstractCliqueFinder {

	private final SolverViewStats solverStats;
	private final boolean intervalization = false;
	private final boolean projectionOptimized = true;
	private final boolean ignoreProjection = false;

	private final boolean wantPowerset = false;
	private final boolean wantPoset = true;
	private final boolean wantSymmetric = false;
	private final boolean tupleGen = false;
	private final String slackVarNamePrefix = "slack_";
	private int countVarOptimized = 0;
	private int countVarPowerSet = 0;
	private int workloadNo = 17;
	private int numOfSymmetricMore = 0;
	boolean wantAccuracy = false;
	ListIterator<RegionSummary> summIter;
	RegionSummary regSum;
	List<List<Double>> bucketSplitPoints;
	List<List<Long>> splitPointsCount;
	// added by tarun
	List<LongList> cliqueIdxtoConditionBValuesList;
	List<Partition> cliqueIdxtoPList;
	List<List<IntList>> cliqueIdxtoPSimplifiedList;

	List<HashMap<Integer, Integer>> mappedIndexOfConsistencyConstraint; // Required by CONSISTENCYFILTERS type of
																		// consistency maker
	List<Integer> cliqueWiseTotalSingleSplitPointRegions; // per clique
	List<CliqueIntersectionInfo> cliqueIntersectionInfos; // Required by BRUTEFORCE type of consistency make
	List<List<RegionSummary>> cliqueToRegionsSummary;
	List<RegionSummary> finalRegionSummaryGlobal;

	public DoubleReductionBasedSolverNew(String viewname, ViewInfo viewInfo, List<boolean[]> allTrueBS,
			List<Set<String>> arasuCliques, Map<String, IntList> bucketFloorsByColumns) {
		super(viewname, viewInfo, allTrueBS, arasuCliques, bucketFloorsByColumns);
		solverStats = new SolverViewStats();
		solverStats.relRowCount = viewInfo.getRowcount();

		// variables added here by tarun -- starts
		cliqueIdxtoConditionBValuesList = new ArrayList<>(cliqueCount);
		cliqueIdxtoPList = new ArrayList<>(cliqueCount);
		cliqueIdxtoPSimplifiedList = new ArrayList<>(cliqueCount);

		mappedIndexOfConsistencyConstraint = new ArrayList<>(); // Required by CONSISTENCYFILTERS type of consistency
																// maker
		cliqueWiseTotalSingleSplitPointRegions = new ArrayList<>(); // per clique
		cliqueIntersectionInfos = new ArrayList<>(); // Required by BRUTEFORCE type of consistency make

	}

	@Override
	public ViewSolutionWithSolverStats solveView(List<FormalCondition> conditions, List<Region> conditionRegions,
			FormalCondition consistencyConstraints[], ConsistencyMakerType consistencyMakerType) {

		StopWatch formulationPlusSolvingSW = new StopWatch("LP-SolvingPlusPostSolving-" + viewname);
		beginLPFormulation();

		List<LinkedList<VariableValuePair>> cliqueIdxToVarValuesList = formulateAndSolve(conditions, conditionRegions,
				consistencyConstraints, consistencyMakerType);
		finishSolving();

		if (ignoreProjection)
			mergeCliqueSolutionsRegionwiseWithoutProjection(conditions, conditionRegions, cliqueIdxToVarValuesList); // to
																														// be
																														// used
		else
			mergeCliqueSolutionsRegionwiseWithProjection(conditions, conditionRegions); // to be

		finishPostSolving();
		solverStats.millisToSolve = formulationPlusSolvingSW.getTimeAndDispose();
		return null;
	}

	private List<LinkedList<VariableValuePair>> formulateAndSolve(List<FormalCondition> conditions,
			List<Region> conditionRegions, FormalCondition consistencyConstraints[],
			ConsistencyMakerType consistencyMakerType) {

		StopWatch preprocessingSW = new StopWatch("Preprocessing" + viewname);
		bucketSplitPoints = new ArrayList<>();
		splitPointsCount = new ArrayList<>();
		for (boolean[] allTrueB : allTrueBS) {

			List<Double> splits = new ArrayList<>();
			for (int i = 0; i < allTrueB.length; i++) {
				splits.add((double) (i));
			}
			bucketSplitPoints.add(splits);
		}

		List<List<FormalCondition>> cliqueToAggregateConditions = new ArrayList<>();

		// STEP 1: For each clique find set of applicable conditions and call variable
		// reduction
		List<List<Region>> cliqueToCRegions = new ArrayList<>();
		for (int i = 0; i < cliqueCount; i++) {

			LongList bvalues = new LongArrayList();
			Set<String> clique = arasuCliques.get(i); // set of columns
			System.out.println(i + "  " + clique);
			List<Region> cRegions = new ArrayList<>();
			List<Pair<Integer, Boolean>> applicableConditions = new ArrayList<>();

			List<FormalCondition> aggregateConditions = new ArrayList<>();

			for (int j = 0; j < conditions.size(); j++) {
				FormalCondition curCondition = conditions.get(j);
				Set<String> appearingCols = new HashSet<>();
				getAppearingCols(appearingCols, curCondition); // appearing columns will have columns appeared in
																// current condition

				if (clique.containsAll(appearingCols)) {

					cRegions.add(conditionRegions.get(j));
					bvalues.add(curCondition.getOutputCardinality());
					if (curCondition instanceof FormalConditionAggregate) {
//						System.out.println(curCondition.getQueryName());	
						aggregateConditions.add(curCondition);// codeS
					}
				}

			}
			cliqueToAggregateConditions.add(aggregateConditions);// codeS

			// Adding extra cRegion for all 1's condition
			Region allOnesCRegion = new Region();
			BucketStructure subConditionBS = new BucketStructure();
			for (int j = 0; j < allTrueBS.size(); j++) {
				Bucket bucket = new Bucket();
				for (int k = 0; k < allTrueBS.get(j).length; k++) {
					if (allTrueBS.get(j)[k]) { // Is this check needed?
						bucket.add(k);
					}
				}
				subConditionBS.add(bucket);
			}
			allOnesCRegion.add(subConditionBS);
			cRegions.add(allOnesCRegion);
			bvalues.add(relationCardinality);
			cliqueIdxtoConditionBValuesList.add(bvalues);

///////////////// Start dk

			if (consistencyMakerType == ConsistencyMakerType.CONSISTENCYFILTERS) {
				HashMap<Integer, Integer> indexKeeperForConsistency = new HashMap<>();
				int tempIndexForConsistency = 0;
				for (int j = 0; j < consistencyConstraints.length; j++) {
					FormalCondition condition = consistencyConstraints[j];
					Set<String> appearingCols = new HashSet<>();
					// changed

					getAppearingColsTemp(appearingCols, condition);
					if (clique.containsAll(appearingCols)) {
						indexKeeperForConsistency.put(j, tempIndexForConsistency++);
						cRegions.add(conditionRegions.get(conditions.size() + j));
					}
				}

				mappedIndexOfConsistencyConstraint.add(indexKeeperForConsistency);
			}

			cliqueToCRegions.add(cRegions);
			Reducer reducer = new Reducer(allTrueBS, cRegions);

			// Using varIds instead of old variable regions
			List<Region> oldVarList = new ArrayList<>(); // List of regions corresponding to below labels
			List<IntList> conditionIdxsList = new ArrayList<>(); // List of labels
			reducer.getVarsAndConditionsSimplified(oldVarList, conditionIdxsList);

			Partition cliqueP = new Partition(new ArrayList<>(oldVarList));
			cliqueIdxtoPList.add(cliqueP);

			cliqueIdxtoPSimplifiedList.add(conditionIdxsList);
			System.out.println("clique " + i);
		}

		long varcountDR = 0;
		for (int i = 0; i < cliqueCount; i++) {
			varcountDR += cliqueIdxtoPList.get(i).getAll().size();
		}
		DebugHelper.printInfo("Number of variables after double reduction before making symmetric " + varcountDR);

		if (!ignoreProjection) {

			// ************shadab projection region preprocessing
			// starts*********************//

			// generating a list of map from groupkey to aggregate conditions.
			List<Map<List<String>, List<FormalConditionAggregate>>> cliqueGroupkeyToConditions = new ArrayList<>();
			List<Map<List<String>, List<Region>>> cliqueGroupkeyToConditionRegions = new ArrayList<>();
			for (int i = 0; i < cliqueCount; i++) {
				List<FormalCondition> aggregateConditions = cliqueToAggregateConditions.get(i);// gets
																								// aggregatecondition
																								// for the current
																								// clique

				Map<List<String>, List<FormalConditionAggregate>> groupkeyToConditions = new HashMap<>();
				Map<List<String>, List<Region>> groupkeyToConditionRegion = new HashMap<>();

				for (FormalCondition condition : aggregateConditions) {

					Region cRegion = getConditionRegion(condition, allTrueBS, sortedViewColumns, bucketFloorsByColumns);
					List<String> groupKey = ((FormalConditionAggregate) condition).getGroupKey();
					if (!groupkeyToConditions.containsKey(groupKey)) {
						groupkeyToConditions.put(groupKey, new ArrayList<>());
						groupkeyToConditionRegion.put(groupKey, new ArrayList<>());
					}
					groupkeyToConditions.get(groupKey).add(((FormalConditionAggregate) condition));
					groupkeyToConditionRegion.get(groupKey).add((cRegion));

				}
				cliqueGroupkeyToConditions.add(groupkeyToConditions);
				cliqueGroupkeyToConditionRegions.add(groupkeyToConditionRegion);
			}

			// -----------------symmetry check--------------------

			for (int i = 0; i < cliqueCount; i++) {
				System.out.println("Clique=" + i);
				List<Region> newRegions = new ArrayList<>();
				List<Region> regionList = cliqueIdxtoPList.get(i).getAll();
				for (List<String> groupKey : cliqueGroupkeyToConditions.get(i).keySet()) {

					for (int k = 0; k < cliqueGroupkeyToConditions.get(i).get(groupKey).size(); k++) {
						// Iterates over the conditions for this groupkeys set.

						Region cRegion = cliqueGroupkeyToConditionRegions.get(i).get(groupKey).get(k);

						for (int j = 0; j < regionList.size(); j++) {
							Region region = regionList.get(j);

							if (cRegion.contains(region)) {
								// if region is a part of the condtion then it is being projected in the PAS
								// subspace.
								List<Region> symmRegions = makeSymmetric(region, groupKey, i); // make the region
																								// symmetric wrt the
																								// groupkey.
								if (symmRegions.size() > 1) {
									throw new RuntimeException("Unsymmetric regions"); // temporary check.

								}
							}
						}
					}
				}
			}
			// check ends

			List<Map<List<String>, Map<FormalConditionAggregate, List<Integer>>>> cliqueToGroupkeyConditionToRegionIdx = new ArrayList<>();
			List<Map<List<String>, List<Integer>>> cliqueToGroupkeyToRegionIdx = new ArrayList<>();

			for (int i = 0; i < cliqueCount; i++) {
				Map<List<String>, Map<FormalConditionAggregate, List<Integer>>> groupkeyConditionToRegionIdx = new HashMap<>();
				Map<List<String>, List<Integer>> groupkeyToregionIdx = new HashMap<>();
				List<Region> regionList = cliqueIdxtoPList.get(i).getAll();
				for (List<String> groupKey : cliqueGroupkeyToConditions.get(i).keySet()) {
					// if(!groupkeyConditionToRegionIdx.containsKey(groupKey))
					groupkeyConditionToRegionIdx.put(groupKey, new HashMap<>());
					groupkeyToregionIdx.put(groupKey, new ArrayList<>());
					for (int k = 0; k < cliqueGroupkeyToConditions.get(i).get(groupKey).size(); k++) {
						FormalConditionAggregate condition = cliqueGroupkeyToConditions.get(i).get(groupKey).get(k);
						Region cRegion = cliqueGroupkeyToConditionRegions.get(i).get(groupKey).get(k);
						List<Integer> toAdd = new ArrayList<>();
						groupkeyConditionToRegionIdx.get(groupKey).put(condition, toAdd);
						for (int j = 0; j < regionList.size(); j++) {
							Region region = regionList.get(j);
							if (cRegion.contains(region)) {
								toAdd.add(j);
								if (!groupkeyToregionIdx.get(groupKey).contains(j))
									groupkeyToregionIdx.get(groupKey).add(j);
							}
						}
					}
				}

				cliqueToGroupkeyConditionToRegionIdx.add(groupkeyConditionToRegionIdx);
				cliqueToGroupkeyToRegionIdx.add(groupkeyToregionIdx);

			}

			// -------------------------preprocessimg
//			add symmetry code here

			// finished----------------------------------------

			// -----------------------projection variable formution----------------------
			// starts-------------------------------

			StopWatch variableFormulationSW = new StopWatch("Variable formulation" + viewname);

			List<Map<List<String>, List<ProjectionVariable>>> cliqueToGroupkeyToProjectionVariables = new ArrayList<>();
			List<Map<List<String>, Map<Integer, List<Integer>>>> cliqueToGroupkeyToRegionToProjectionVariables = new ArrayList<>();// same

			List<Map<List<String>, List<ProjectionVariableOptimized>>> cliqueToGroupkeyToProjectionVariablesOptimized = new ArrayList<>();
			List<Map<List<String>, Map<Integer, List<Integer>>>> cliqueToGroupkeyToRegionToProjectionVariablesOptimzed = new ArrayList<>();// same//
																																			// as
			if (wantPowerset) {
				for (int i = 0; i < cliqueCount; i++) {
					Map<List<String>, List<ProjectionVariable>> groupkeyToProjectionVariables = new HashMap<>();
					Map<List<String>, Map<Integer, List<Integer>>> groupkeyToRegionToProjectionVariables = new HashMap<>();
					Map<List<String>, List<Integer>> groupkeyToregionIdx = cliqueToGroupkeyToRegionIdx.get(i);
					for (List<String> groupkey : groupkeyToregionIdx.keySet()) {
						Map<Integer, List<Integer>> regionToProjectionVariables = new HashMap<>();

						List<Integer> regionsIdx = groupkeyToregionIdx.get(groupkey);
						List<ProjectionVariable> currProjVariables = getProjectionRegions(regionsIdx, groupkey, i);
						System.out.println("For groupkeys " + groupkey + " count=" + currProjVariables.size());
						groupkeyToProjectionVariables.put(groupkey, currProjVariables);

						for (int j = 0; j < currProjVariables.size(); j++) {
							ProjectionVariable var = currProjVariables.get(j);
							for (Integer regionIdx : var.regionList) {
								if (!regionToProjectionVariables.containsKey(regionIdx))
									regionToProjectionVariables.put(regionIdx, new ArrayList<>());
								regionToProjectionVariables.get(regionIdx).add(j);
							}
						}
						groupkeyToRegionToProjectionVariables.put(groupkey, regionToProjectionVariables);
					}
					cliqueToGroupkeyToProjectionVariables.add(groupkeyToProjectionVariables);
					cliqueToGroupkeyToRegionToProjectionVariables.add(groupkeyToRegionToProjectionVariables);

				}
			}

			if (wantPoset) {
				System.out.println("Optimized variables count");
				for (int cliqueIndex = 0; cliqueIndex < cliqueCount; cliqueIndex++) {
					Map<List<String>, List<ProjectionVariableOptimized>> groupkeyToProjectionVariablesOptimized = new HashMap<>();
					Map<List<String>, Map<Integer, List<Integer>>> groupkeyToRegionToProjectionVariablesOptimzed = new HashMap<>();
					Map<List<String>, Map<FormalConditionAggregate, List<Integer>>> groupkeyConditionToRegionIdx = cliqueToGroupkeyConditionToRegionIdx
							.get(cliqueIndex);

					for (List<String> groupkey : groupkeyConditionToRegionIdx.keySet()) {
						Map<Integer, List<Integer>> regionToProjectionVariablesOptimzed = new HashMap<>();

						Map<FormalConditionAggregate, List<Integer>> conditionToRegionIdx = groupkeyConditionToRegionIdx
								.get(groupkey);
						UndirectedGraph<String, DefaultEdge> projectionConditionGraph = new SimpleGraph<>(
								DefaultEdge.class);
						for (FormalConditionAggregate condition : conditionToRegionIdx.keySet()) {

							List<Integer> regionsIdx = conditionToRegionIdx.get(condition);
							for (Integer regionIdx : regionsIdx) {

								projectionConditionGraph.addVertex(regionIdx.toString());
							}
							for (int j = 0; j < regionsIdx.size(); j++) {
								Region regU = cliqueIdxtoPList.get(cliqueIndex).at(regionsIdx.get(j));
								String u = regionsIdx.get(j).toString();
								for (int k = j + 1; k < regionsIdx.size(); k++) {
									Region regV = cliqueIdxtoPList.get(cliqueIndex).at(regionsIdx.get(k));
									String v = regionsIdx.get(k).toString();
									if (!(projectRegion(regU, groupkey).intersection(projectRegion(regV, groupkey)))
											.isEmpty())
										projectionConditionGraph.addEdge(u, v);
								}
							}

						}

						List<ProjectionVariableOptimized> varList = getProjectionVariablesOptimized(cliqueIndex,
								projectionConditionGraph, groupkey);
//				List<ProjectionVariableOptimized> varList2 = getProjectionVariablesOptimized(projectionConditionGraph);
						System.out.println("For groupKey " + groupkey + " count=" + varList.size());
//				System.out.println("For groupKey "+ groupkey+" count="+ varList2.size());
						if (varList.size() > 500)
							System.out.println("big");
						// throw new
						// RuntimeException("big+"+varList.size());//System.out.println("big");
						for (int j = 0; j < varList.size(); j++) {
							ProjectionVariableOptimized var = varList.get(j);
							for (Integer pos : var.positive) {
								if (!regionToProjectionVariablesOptimzed.containsKey(pos)) {
									regionToProjectionVariablesOptimzed.put(pos, new ArrayList<>());
								}
								regionToProjectionVariablesOptimzed.get(pos).add(j);
							}
						}
						groupkeyToRegionToProjectionVariablesOptimzed.put(groupkey,
								regionToProjectionVariablesOptimzed);
						groupkeyToProjectionVariablesOptimized.put(groupkey, varList);
					}
					cliqueToGroupkeyToRegionToProjectionVariablesOptimzed
							.add(groupkeyToRegionToProjectionVariablesOptimzed);
					cliqueToGroupkeyToProjectionVariablesOptimized.add(groupkeyToProjectionVariablesOptimized);

				}

				variableFormulationSW.displayTimeAndDispose();

				// --------------------------------variable formulation
				// finished-------------------------------

				for (int i = 0; i < cliqueCount; i++) {

					Map<Integer, List<Integer>> columnTofirstIntervalToProjVar = new HashMap<>();
					Map<Integer, List<Integer>> columnTofirstIntervalToProjVar1 = new HashMap<>();

					Map<List<String>, List<ProjectionVariableOptimized>> groupkeyToProjectionVariablesOptimized = cliqueToGroupkeyToProjectionVariablesOptimized
							.get(i);
					for (List<String> groupkey : groupkeyToProjectionVariablesOptimized.keySet()) {
						int colIdx = getColumnsIdx(groupkey).get(0);

						if (!columnTofirstIntervalToProjVar.containsKey(colIdx)) {
							columnTofirstIntervalToProjVar.put(colIdx,
									new ArrayList<>(Collections.nCopies(allTrueBS.get(colIdx).length, 0)));
							columnTofirstIntervalToProjVar1.put(colIdx,
									new ArrayList<>(Collections.nCopies(allTrueBS.get(colIdx).length, 0)));
						}
						List<ProjectionVariableOptimized> projectionVariablesOptimized = groupkeyToProjectionVariablesOptimized
								.get(groupkey);


						for (int j = 0; j < projectionVariablesOptimized.size(); j++) {
							ProjectionVariableOptimized var = projectionVariablesOptimized.get(j);
							Region reg = var.intersection;
							int firstInterval = reg.at(0).at(0).at(0);

							columnTofirstIntervalToProjVar.get(colIdx).set(firstInterval,
									columnTofirstIntervalToProjVar.get(colIdx).get(firstInterval) + 1);

					}
					for (List<String> groupkey : groupkeyToProjectionVariablesOptimized.keySet()) {
						int colIdx = getColumnsIdx(groupkey).get(0);
						List<ProjectionVariableOptimized> projectionVariablesOptimized = groupkeyToProjectionVariablesOptimized
								.get(groupkey);

						for (int j = 0; j < projectionVariablesOptimized.size(); j++) {
							ProjectionVariableOptimized var = projectionVariablesOptimized.get(j);
							Region reg = var.intersection;
							int firstInterval = reg.at(0).at(0).at(0);
							int k = columnTofirstIntervalToProjVar1.get(colIdx).get(firstInterval);
							columnTofirstIntervalToProjVar1.get(colIdx).set(firstInterval, k + 1);
							int n = columnTofirstIntervalToProjVar.get(colIdx).get(firstInterval);

							if (k != 0)
								bucketSplitPoints.get(colIdx).add(firstInterval + (double) k / n);

							RegionF interval = new RegionF();
							BucketStructureF bsF = new BucketStructureF();
							BucketF b = new BucketF();

							b.add(firstInterval + (double) k / n);
							bsF.add(b);
							BucketStructure bs = var.intersection.at(0);

							for (int l = 1; l < bs.size(); l++) {
								BucketF bTemp = new BucketF();
								bTemp.add((double) (bs.at(l).at(0)));
								bsF.add(bTemp);
							}
							interval.add(bsF);
							var.interval = interval;
						}
					}
				}

				for (List<Double> splitPoints : bucketSplitPoints)
					Collections.sort(splitPoints);
				for (List<Double> splitPoints : bucketSplitPoints) {

					List<Long> tempList = new ArrayList<>();
					for (Double temp : splitPoints)
						tempList.add(0L);
					splitPointsCount.add(tempList);
				}

			}

			// postVariableFormulationSW.displayTimeAndDispose();

			preprocessingSW.displayTimeAndDispose();
			return formAndSolveLP(consistencyMakerType, consistencyConstraints, conditions, cliqueToGroupkeyToRegionIdx,
					cliqueToGroupkeyConditionToRegionIdx, cliqueToGroupkeyToProjectionVariables,
					cliqueToGroupkeyToRegionToProjectionVariables,
					cliqueToGroupkeyToRegionToProjectionVariablesOptimzed,
					cliqueToGroupkeyToProjectionVariablesOptimized);
		}
		return formAndSolveLP(consistencyMakerType, consistencyConstraints, conditions, null, null, null, null, null,
				null);

	}
	
	
	private List<ProjectionVariable> getProjectionRegions(List<Integer> aggregateRegionIdx,
			List<String> projectionColumns, int cliqueIdx) {
//		Powerset enumeration. Uses the apriori algorithm.
		List<ProjectionVariable> projectionVariableList = new ArrayList<>();// k-length projection regions
		List<ProjectionVariable> prevProjectionVariableList = new ArrayList<>();//k-1 -length projection regions.
		List<Integer> projectionColumnsIdx = getColumnsIdx(projectionColumns);//new ArrayList<>();
		List<Region> regionList = cliqueIdxtoPList.get(cliqueIdx).getAll();

		for (Integer regionId : aggregateRegionIdx) {
			ProjectionVariable currVar = new ProjectionVariable(regionId);//start with first element
			currVar.groupkey = projectionColumns;
			currVar.intersectionRegion = projectRegion(regionList.get(regionId), projectionColumnsIdx);
			projectionVariableList.add(currVar);
			prevProjectionVariableList.add(currVar);

		}
		int maxLength = aggregateRegionIdx.size();
		for (int k = 1; k <= maxLength - 1; k++) {
			List<ProjectionVariable> currProjectionVariableList = new ArrayList<>();

			for (int i = 0; i < prevProjectionVariableList.size(); i++) {
				for (int j = i + 1; j < prevProjectionVariableList.size(); j++) {
					ProjectionVariable newVariable = areCompatible(prevProjectionVariableList.get(i),
							prevProjectionVariableList.get(j), projectionColumnsIdx, k, cliqueIdx);
					if (newVariable != null) {
						newVariable.groupkey = projectionColumns;
						currProjectionVariableList.add(newVariable);
					}

				}

			}
			prevProjectionVariableList.clear();
			prevProjectionVariableList.addAll(currProjectionVariableList);
			projectionVariableList.addAll(currProjectionVariableList);
			System.out.println(projectionVariableList.size());
			if (currProjectionVariableList.size() == 0)
				break;
		}

		return projectionVariableList;

	}

	/*
	 * Helper functions
	 * 
	 */

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

	private List<FormalCondition> getSOPSubconditions(FormalCondition condition) {
		FormalConditionSOP sopCondition = new FormalConditionSOP(condition);
		return sopCondition.getConditionList();
	}

	
	private ProjectionVariable areCompatible(ProjectionVariable projectionVariable1,
			ProjectionVariable projectionVariable2, List<Integer> projectionColumnsIdx, int len, int cliqueIdx) {
		// TODO Auto-generated method stub
		List<Integer> regionList1 = projectionVariable1.regionList;
		List<Integer> regionList2 = projectionVariable2.regionList;

		for (int i = 0; i < len - 1; i++) {
			if (!(regionList1.get(i) == regionList2.get(i))) {
				return null;
			}
		}

		Region intersection = projectionVariable1.intersectionRegion
				.intersection(projectionVariable2.intersectionRegion);
		if (intersection.isEmpty())
			return null;
		ProjectionVariable newProjectionVariable = new ProjectionVariable();
		newProjectionVariable.regionList.addAll(regionList1);
		newProjectionVariable.regionList.remove(len - 1);
		// System.out.println(regionList1.get(len-1)+regionList2.get(len-1) );
		newProjectionVariable.regionList.add(Math.min(regionList1.get(len - 1), regionList2.get(len - 1)));
		newProjectionVariable.regionList.add(Math.max(regionList1.get(len - 1), regionList2.get(len - 1)));
		newProjectionVariable.intersectionRegion = intersection;
		return newProjectionVariable;

	}


	private Region projectRegion(Region region1, List<Integer> projectionColumnsIdx) {
		
		Region projectedRegion = new Region();
		for (BucketStructure bs : region1.getAll()) {
			BucketStructure bsNew = new BucketStructure();
			for (int i = 0; i < bs.size(); i++) {
				if (projectionColumnsIdx.contains(i))
					bsNew.add(bs.at(i));
			}
			projectedRegion.add(bsNew);

		}
		compressRegions(projectedRegion);
		if (projectedRegion.size() == 1)
			return projectedRegion;

		if (projectedRegion.size() > 2)
			throw new RuntimeException("Handle the projectRegion Mehod better");

		BucketStructure bs1 = projectedRegion.getAll().get(0);
		BucketStructure bs2 = projectedRegion.getAll().get(1);
		projectedRegion = new Region();
		projectedRegion.add(bs1);
		projectedRegion.addAll(bs2.minus(bs1));

		return projectedRegion;
	}

	public void compressRegions(Region region) {
		// Code by Subhodeep
		// Eg :Two BS having all buckets exactly same except one will be merged into 1.

		Boolean change = true;

		while (change) {
			change = false;
			for (int i = 0; i < region.size(); i++) {
				for (int j = i + 1; j < region.size(); j++) {
					if ((region.at(i).minus(region.at(j))).isEmpty() && (region.at(j).minus(region.at(i))).isEmpty()) {
						region.remove(j);
						j--;
						continue;
					}
					int index = CheckBSmergeCompatibility(region.at(i), region.at(j));
					if (index != -1) {
						mergeBS(region.at(i), region.at(j), index);
						// region.set(i,mergeBS(region.at(i), region.at(j), index));
						region.remove(j);
						j--;
						change = true;
					}
				}
			}
		}
		// if(region.size()>1)
		// System.out.print(region);
	}

	static void mergeBS(BucketStructure BS1, BucketStructure BS2, int index) {
		// 0Bucket mergedB = new Bucket(BS1.at(index));
		BS1.at(index).addAll(BS2.at(index));

	}

	static int CheckBSmergeCompatibility(BucketStructure BS1, BucketStructure BS2) {

		int ans_index = -1;
		int diff_count = 0;
		for (int i = 0; i < BS1.size(); i++) {
			if (!BS1.at(i).isEqual(BS2.at(i))) {
				diff_count++;
				ans_index = i;
			}
			if (diff_count > 1) {
//				More than one dimension unaligned
				return -1;
			}
		}
		if (diff_count == 1)
			return ans_index;
		return -1;
	}
	
	private IntList getColumnsIdx(List<String> groupkey) {
//		List<Integer> projectionColumnsIdx = new ArrayList<>();
		IntList projectionColumnsIdx = new IntArrayList();

		// converting col names to index
		for (String projectionColumn : groupkey) {
			projectionColumnsIdx.add(sortedViewColumns.indexOf(projectionColumn));
		}
		return projectionColumnsIdx;
	}
}

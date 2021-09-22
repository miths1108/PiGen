package in.ac.iisc.cds.dsl.cdgvendor.solver;

/*
 * How to read code by dk:
 * Before every chunk of code a variable is defined. The value of that variable is found out in the corresponding code. The name of variable tells what the code is doing
 */
//TODO 
//getconditioinsop in case of single attribute predicate and diffeternt attribute projection-done
//correct the algo optmized
//remove only filter conditions-done
//check whether split count should have bucketSplitPonits.size(0+1 size

//check subho merge code-done
//groupKey sort order and its effect
//t07 throws exception in dividing
//merge order 
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.management.RuntimeErrorException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Writer;

import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.IntNum;
import com.microsoft.z3.Model;
import com.microsoft.z3.Optimize;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;

import in.ac.iisc.cds.dsl.cdgvendor.model.ProjectionStuffInColumn;
import in.ac.iisc.cds.dsl.cdgvendor.model.ProjectionStuffInSSPRegion;
import in.ac.iisc.cds.dsl.cdgvendor.model.ProjectionVariable;
import in.ac.iisc.cds.dsl.cdgvendor.model.ProjectionVariableOptimized;
import in.ac.iisc.cds.dsl.cdgvendor.model.SolverViewStats;
import in.ac.iisc.cds.dsl.cdgvendor.model.ValueCombination;
import in.ac.iisc.cds.dsl.cdgvendor.model.ValueCombinationF;
import in.ac.iisc.cds.dsl.cdgvendor.model.VariableValuePair;
import in.ac.iisc.cds.dsl.cdgvendor.model.ViewInfo;
import in.ac.iisc.cds.dsl.cdgvendor.model.ViewSolution;
import in.ac.iisc.cds.dsl.cdgvendor.model.ViewSolutionInMemory;
import in.ac.iisc.cds.dsl.cdgvendor.model.ViewSolutionRegion;
import in.ac.iisc.cds.dsl.cdgvendor.model.ViewSolutionWithSolverStats;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalCondition;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionAggregate;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionAnd;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionAndAggregate;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionOr;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionSOP;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionSimpleInt;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.Symbol;
import in.ac.iisc.cds.dsl.cdgvendor.reducer.Bucket;
import in.ac.iisc.cds.dsl.cdgvendor.reducer.BucketF;
import in.ac.iisc.cds.dsl.cdgvendor.reducer.BucketStructure;
import in.ac.iisc.cds.dsl.cdgvendor.reducer.BucketStructureF;
import in.ac.iisc.cds.dsl.cdgvendor.reducer.DatabaseSummaryProjection;
//import in.ac.iisc.cds.dsl.cdgvendor.reducer.DatabaseSummaryProjection;
import in.ac.iisc.cds.dsl.cdgvendor.reducer.Partition;
import in.ac.iisc.cds.dsl.cdgvendor.reducer.Reducer;
import in.ac.iisc.cds.dsl.cdgvendor.reducer.Region;
import in.ac.iisc.cds.dsl.cdgvendor.reducer.RegionF;
import in.ac.iisc.cds.dsl.cdgvendor.reducer.RegionSummary;
import in.ac.iisc.cds.dsl.cdgvendor.solver.Z3Solver.ConsistencyMakerType;
import in.ac.iisc.cds.dsl.cdgvendor.utils.DebugHelper;
import in.ac.iisc.cds.dsl.cdgvendor.utils.MutablePair;
import in.ac.iisc.cds.dsl.cdgvendor.utils.Pair;
import in.ac.iisc.cds.dsl.cdgvendor.utils.StopWatch;
import in.ac.iisc.cds.dsl.cdgvendor.utils.Triplet;
import in.ac.iisc.cds.dsl.dirty.DirtyCode;
import in.ac.iisc.cds.dsl.dirty.DirtyDatabaseSummary;
import in.ac.iisc.cds.dsl.dirty.DirtyValueCombinationWithProjectionValues;
import in.ac.iisc.cds.dsl.dirty.DirtyValueInterval;
import in.ac.iisc.cds.dsl.dirty.DirtyValueIntervalWithCount;
import in.ac.iisc.cds.dsl.dirty.DirtyVariableValuePairWithProjectionValues;
import in.ac.iisc.cds.dsl.dirty.DirtyViewSolution;
import in.ac.iisc.cds.dsl.dirty.ProjectionValues;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

import org.antlr.v4.runtime.misc.IntegerList;
import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.BronKerboschCliqueFinder;
import org.jgrapht.alg.CliqueMinimalSeparatorDecomposition;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.NeighborIndex;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import java.util.Scanner;

public class DoubleReductionTemp extends AbstractCliqueFinder {

	private final SolverViewStats solverStats;
	private final boolean intervalization = false;
	private final boolean projectionOptimized = true;
	private final boolean ignoreProjection = false;

	private final boolean wantPowerset = false;
	private final boolean wantPoset = true;
	private final boolean wantSymmetric = true;
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
	List<List<Pair<Integer, Boolean>>> applicableConditionsOnClique;
	List<CliqueIntersectionInfo> cliqueIntersectionInfos; // Required by BRUTEFORCE type of consistency make
	List<List<RegionSummary>> cliqueToRegionsSummary;
	List<RegionSummary> finalRegionSummaryGlobal;

	public DoubleReductionTemp(String viewname, ViewInfo viewInfo, List<boolean[]> allTrueBS,
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
		applicableConditionsOnClique = new ArrayList<>();
		cliqueIntersectionInfos = new ArrayList<>(); // Required by BRUTEFORCE type of consistency make
		// variables added here by tarun ends --

	}

	@Override
	public ViewSolutionWithSolverStats solveView(List<FormalCondition> conditions, List<Region> conditionRegions,
			FormalCondition consistencyConstraints[], ConsistencyMakerType consistencyMakerType) {

		StopWatch formulationPlusSolvingSW = new StopWatch("LP-SolvingPlusPostSolving-" + viewname);
		// System.out.println(relationCardinality);
		// return null;
		beginLPFormulation();
		/*
		 * Map<String, String> contextmaptest = new HashMap<>();
		 * contextmaptest.put("model", "true"); contextmaptest.put("unsat_core",
		 * "true"); Context ctxtest = new Context(contextmaptest);
		 * 
		 * Optimize osolver = ctxtest.mkOptimize(); IntExpr x1 =
		 * ctxtest.mkIntConst("x1"); IntExpr x2 = ctxtest.mkIntConst("x2"); IntExpr x3 =
		 * ctxtest.mkIntConst("x3"); IntExpr y11 = ctxtest.mkIntConst("y11"); IntExpr
		 * y12 = ctxtest.mkIntConst("y12"); IntExpr y13 = ctxtest.mkIntConst("y13");
		 * IntExpr y21 = ctxtest.mkIntConst("y21"); IntExpr y22 =
		 * ctxtest.mkIntConst("y22"); IntExpr y23 = ctxtest.mkIntConst("y23"); ArithExpr
		 * expr1 = ctxtest.mkAdd(y11, y12,y13); ArithExpr expr2 =
		 * ctxtest.mkAdd(y11,y13); ArithExpr expr3 = ctxtest.mkAdd(y12,y13);
		 * 
		 * ArithExpr expr5 =
		 * ctxtest.mkMul(ctxtest.mkInt(100000),ctxtest.mkAdd(y11,y13)); ArithExpr expr6
		 * = ctxtest.mkMul(ctxtest.mkInt(100000),ctxtest.mkAdd(y12,y13));
		 * 
		 * ArithExpr expr7 = ctxtest.mkAdd(y21, y22,y23); ArithExpr expr8 =
		 * ctxtest.mkAdd(y21,y23); ArithExpr expr9 = ctxtest.mkAdd(y22,y23);
		 * 
		 * ArithExpr expr11 =
		 * ctxtest.mkMul(ctxtest.mkInt(100000),ctxtest.mkAdd(y21,y23)); ArithExpr
		 * expr12= ctxtest.mkMul(ctxtest.mkInt(100000),ctxtest.mkAdd(y22,y23));
		 * 
		 * osolver.Add(ctxtest.mkGe(x1, ctxtest.mkInt(0))); osolver.Add(ctxtest.mkGe(x2,
		 * ctxtest.mkInt(0))); osolver.Add(ctxtest.mkGe(x3, ctxtest.mkInt(0)));
		 * osolver.Add(ctxtest.mkGe(y11, ctxtest.mkInt(0)));
		 * osolver.Add(ctxtest.mkGe(y12, ctxtest.mkInt(0)));
		 * osolver.Add(ctxtest.mkGe(y13, ctxtest.mkInt(0)));
		 * osolver.Add(ctxtest.mkGe(y21, ctxtest.mkInt(0)));
		 * osolver.Add(ctxtest.mkGe(y22, ctxtest.mkInt(0)));
		 * osolver.Add(ctxtest.mkGe(y23, ctxtest.mkInt(0)));
		 * 
		 * osolver.Add(ctxtest.mkEq(expr1,ctxtest.mkInt(3)));
		 * osolver.Add(ctxtest.mkEq(expr7,ctxtest.mkInt(4)));
		 * osolver.Add(ctxtest.mkLe(expr2,x1)); osolver.Add(ctxtest.mkLe(expr3,x2));
		 * osolver.Add(ctxtest.mkGe(expr5,x1)); osolver.Add(ctxtest.mkGe(expr6,x2));
		 * osolver.Add(ctxtest.mkLe(expr8,x2)); osolver.Add(ctxtest.mkLe(expr9,x3));
		 * osolver.Add(ctxtest.mkGe(expr11,x2)); osolver.Add(ctxtest.mkGe(expr11,x3));
		 * 
		 * osolver.Check(); // Model modeltest = osolver.getModel();
		 * System.out.println(x1);
		 */
		/*
		 * List<IntList> A = new ArrayList<>(); IntList a1 = new IntArrayList();
		 * a1.add(0); a1.add(1); IntList a2 = new IntArrayList(); a2.add(3); a2.add(4);
		 * IntList a3 = new IntArrayList(); a3.add(5); a3.add(6); BucketStructure bs1 =
		 * new BucketStructure(); BucketStructure bs2 = new BucketStructure();
		 * BucketStructure bs3 = new BucketStructure(); Bucket b1 = new Bucket(); Bucket
		 * b2 = new Bucket(); Bucket b3 = new Bucket(); Bucket b4 = new Bucket(); Bucket
		 * b5 = new Bucket(); Bucket b6 = new Bucket(); b1.add(0); b1.add(1); b1.add(2);
		 * b2.add(0); bs1.add(b1); bs1.add(b2);
		 * 
		 * b3.add(0); b3.add(2); b4.add(1); b4.add(2); bs2.add(b3); bs2.add(b4);
		 * 
		 * b5.add(1); b6.add(2); bs3.add(b5); bs3.add(b6); // b3.add(5);b3.add(6);
		 * 
		 * Region tempRegion = new Region(); tempRegion.add(bs1); tempRegion.add(bs2);
		 * tempRegion.add(bs3); List<Integer> tempProj = new ArrayList<>();
		 * tempProj.add(0);
		 */
//		A.add(a1);
//		A.add(a2);
//		A.add(a3);
//		IntList positions=new IntArrayList();
//		positions.add(0);
//		positions.add(0);
//		positions.add(0);
//		Region reg1=new Region();
//		reg1.add(bs1);reg1.add(bs2);reg1.add(bs3);
//		List<Integer> projectionColumns=new ArrayList<>();
//		List<BucketStructure>ans1=getProjectionIntervals(bs, positions);
//		List<IntList>ans=getProjectionIntervalsTemp(A,positions);
//		List<Region> regions = makeSymmetricNew(tempRegion, tempProj);
		List<LinkedList<VariableValuePair>> cliqueIdxToVarValuesList = formulateAndSolve(conditions, conditionRegions,
				consistencyConstraints, consistencyMakerType);
		finishSolving();

		if (ignoreProjection)
			mergeCliqueSolutionsRegionwiseTemp(conditions, conditionRegions, cliqueIdxToVarValuesList); // to be used
		else
			mergeCliqueSolutionsRegionwise(conditions, conditionRegions); // to be

		finishPostSolving();
		solverStats.millisToSolve = formulationPlusSolvingSW.getTimeAndDispose();
		return null;
	}

	private boolean symmetryCheck(Region reg, List<Integer> projectionColumns) {
		List<Integer> nonProjectionColumns = new ArrayList<>();
		int numCol = reg.at(0).size();
		for (int i = 0; i < numCol; i++) {
			if (!projectionColumns.contains(i))
				nonProjectionColumns.add(i);
		}
		Region projectionRegion = projectRegionTemp(reg, projectionColumns);
		Region nonProjectionRegion = projectRegionTemp(reg, nonProjectionColumns);
		Region crossProduct = new Region();

		// Do cross product between the two regions.
		for (BucketStructure bsProj : projectionRegion.getAll()) {
			for (BucketStructure bsNonProj : nonProjectionRegion.getAll()) {
				BucketStructure newBS = new BucketStructure();
				for (int i = 0; i < numCol; i++)
					newBS.add(new Bucket());
				for (int i = 0; i < projectionColumns.size(); i++) {
					int col = projectionColumns.get(i);
					newBS.set(bsProj.at(i), col);
				}
				for (int i = 0; i < nonProjectionColumns.size(); i++) {
					int col = nonProjectionColumns.get(i);
					newBS.set(bsNonProj.at(i), col);
				}
				crossProduct.add(newBS);
			}
		}

		if (crossProduct.areEqual(reg))
			return true;
		return false;

	}

	private List<Region> makeSymmetricNew(Region reg, List<Integer> projectionColumns) {
		List<Region> symmetricRegions = new ArrayList<>();
		int numCol = reg.at(0).size();
		Map<BucketStructure, Region> intervalToCompatibleRegion = new HashMap<BucketStructure, Region>();
		Map<Region, Region> symmetricMap = new HashMap<>();
		List<Integer> nonProjectionColumns = new ArrayList<>();
		for (int i = 0; i < numCol; i++) {
			if (!projectionColumns.contains(i))
				nonProjectionColumns.add(i);
		}
//		Region projectionRegion=projectRegionTemp(reg, projectionColumns);
//		Region nonProjectionRegion=projectRegionTemp(reg, nonProjectionColumns);
//		Region crossProduct=new Region();
//		
//		//Do cross product between the two regions.
//		for(BucketStructure bsProj:projectionRegion.getAll()) {
//			for(BucketStructure bsNonProj:nonProjectionRegion.getAll()) {
//				BucketStructure newBS=new BucketStructure();
//				for (int i = 0; i < numCol; i++) 
//					newBS.add(new Bucket());
//				for (int i = 0; i < projectionColumns.size(); i++) {
//					int col=projectionColumns.get(i);
//					newBS.set(bsProj.at(i),col);
//				}
//				for (int i = 0; i < nonProjectionColumns.size(); i++) {
//					int col=nonProjectionColumns.get(i);
//					newBS.set(bsNonProj.at(i),col);
//				}
//				crossProduct.add(newBS);
//			}
//		}
//		
//		if(crossProduct.areEqual(reg))
//			System.out.println("Both are equal. Already symmetric");

//		if(symmetryCheck(reg, projectionColumns))
//			System.out.println("Both are equal. Already symmetric");

		for (BucketStructure bs : reg.getAll()) {
			BucketStructure bsProj = bs.projectBS(projectionColumns);
			List<Integer> positions = new ArrayList<>();
			for (int i = 0; i < projectionColumns.size(); i++) {
				positions.add(0);
			}
			List<BucketStructure> intervals = getProjectionIntervals(bsProj, positions);
			for (BucketStructure interval : intervals) {
				boolean foundBS = false;
				for (Entry<BucketStructure, Region> entry : intervalToCompatibleRegion.entrySet()) {

					BucketStructure currBS = entry.getKey();
					if (currBS.areEqual(interval)) {
						foundBS = true;
						intervalToCompatibleRegion.get(interval).add(bs.projectBS(nonProjectionColumns).getDeepCopy());
						break;
					}

				}
				if (!foundBS) {
					intervalToCompatibleRegion.put(interval, new Region());
					intervalToCompatibleRegion.get(interval).add(bs.projectBS(nonProjectionColumns).getDeepCopy());
				}
//				if (!intervalToCompatibleRegion.containsKey(interval)) {
//					intervalToCompatibleRegion.put(interval, new Region());
//					intervalToCompatibleRegion.get(interval).add(bs.projectBS(nonProjectionColumns).getDeepCopy());
//				}
//				else
//					intervalToCompatibleRegion.get(interval).add(bs.projectBS(nonProjectionColumns).getDeepCopy());
			}

		}
		for (Entry<BucketStructure, Region> entry : intervalToCompatibleRegion.entrySet()) {
			compressRegions(entry.getValue());
		}
		for (Entry<BucketStructure, Region> entry : intervalToCompatibleRegion.entrySet()) {

			Region regCurr = entry.getValue();
			boolean foundReg = false;
			for (Region regTemp : symmetricMap.keySet()) {
				if (regTemp.areEqual(regCurr)) {
					foundReg = true;
					symmetricMap.get(regTemp).add(entry.getKey());
					break;
				}
			}
			if (!foundReg) {
				symmetricMap.put(regCurr, new Region());
				symmetricMap.get(regCurr).add(entry.getKey());
			}
		}
//		symmetricMap maps a non-projection subspace region to a list of BS intervals stored as regions 
//		Need to add buckets cross-product
		for (Entry<Region, Region> entry3 : symmetricMap.entrySet()) {
			compressRegions(entry3.getValue());
			Region newRegion = new Region();
			Region projRegion = entry3.getValue();
			Region nonProjRegion = entry3.getKey();
			// Do cross product between the two regions.
			for (BucketStructure bsProj : projRegion.getAll()) {
				for (BucketStructure bsNonProj : nonProjRegion.getAll()) {
					BucketStructure newBS = new BucketStructure();
					for (int i = 0; i < numCol; i++)
						newBS.add(new Bucket());
					for (int i = 0; i < projectionColumns.size(); i++) {
						int col = projectionColumns.get(i);
						newBS.set(bsProj.at(i), col);
					}
					for (int i = 0; i < nonProjectionColumns.size(); i++) {
						int col = nonProjectionColumns.get(i);
						newBS.set(bsNonProj.at(i), col);
					}
					newRegion.add(newBS);
				}
			}
			symmetricRegions.add(newRegion);

		}
		if (symmetricRegions.size() == 1) {
			if (!symmetricRegions.get(0).areEqual(reg))
				throw new RuntimeException("Wrong symmetry!");
		} else {
			Region tempRegion = reg.getDeepCopy();
			for (Region region : symmetricRegions) {
				if (!symmetryCheck(region, projectionColumns))
					throw new RuntimeException("Not symmetric after refinement!");
				if (!tempRegion.contains(region))
					throw new RuntimeException("Not a part of the old region");
				tempRegion = tempRegion.minus(region);
			}
			if (!tempRegion.isEmpty())
				throw new RuntimeException("Wrong implementation");

		}
		return symmetricRegions;
	}

	private List<BucketStructure> getProjectionIntervals(BucketStructure bs, List<Integer> positions) {
		List<BucketStructure> result = new ArrayList<>();
		BucketStructure combination = getCombination(bs, positions);
		if (!incrementPos(bs, positions, positions.size() - 1)) {
			result.add(combination);
			return result;
		}
		result = getProjectionIntervals(bs, positions);
		result.add(combination);
		return result;
	}

	private BucketStructure getCombination(BucketStructure bs, List<Integer> positions) {
		BucketStructure result = new BucketStructure();

		for (int i = 0; i < positions.size(); i++) {
			Bucket b = new Bucket();
			Integer pos = positions.get(i);
			b.add(bs.at(i).at(pos));
			result.add(b);
		}
		return result;
	}

	private boolean incrementPos(BucketStructure bs, List<Integer> positions, int i) {
		if (positions.get(i) >= bs.at(i).size() - 1) {
			if (i == 0)
				return false;
			positions.set(i, 0);
			return incrementPos(bs, positions, i - 1);
		}
//		Integer inc=positions.get(i)+1;
		positions.set(i, positions.get(i) + 1);
		return true;
	}

	private List<IntList> getProjectionIntervalsTemp(List<IntList> A, IntList positions) {
		List<IntList> result = new ArrayList<>();
		IntList combination = getCombination(A, positions);
		if (!incrementPos(A, positions, positions.size() - 1)) {
			result.add(combination);
			return result;
		}
		result = getProjectionIntervalsTemp(A, positions);
		result.add(combination);
		return result;
	}

	private boolean incrementPos(List<IntList> A, IntList positions, int i) {
		if (positions.get(i) >= A.get(i).size() - 1) {
			if (i == 0)
				return false;
			positions.set(i, 0);
			return incrementPos(A, positions, i - 1);
		}
		positions.set(i, positions.get(i) + 1);
		return true;
	}

	private IntList getCombination(List<IntList> A, IntList positions) {
		IntList result = new IntArrayList();

		for (int i = 0; i < positions.size(); i++) {
			Integer pos = positions.get(i);
			result.add(A.get(i).get(pos));
		}
		return result;
	}

	public void solveView1(List<FormalCondition> conditions, List<Region> conditionRegions,
			FormalCondition consistencyConstraints[], ConsistencyMakerType consistencyMakerType,
			Map<String, List<Region>> aggregateColumnsToSingleSplitPointRegions) {

		StopWatch formulationPlusSolvingSW = new StopWatch("LP-Solving (meging cliques not included)" + viewname);
		beginLPFormulation();

		finishSolving();
		solverStats.millisToSolve = formulationPlusSolvingSW.getTimeAndDispose();
		return;
	}

//	public ViewSolutionWithSolverStats mergeCliques(List<LinkedList<VariableValuePair>> cliqueIdxToVarValuesList) {
//
//		ViewSolution viewSolution = mergeCliqueSolutions(cliqueIdxToVarValuesList);
//		finishPostSolving();
//		return new ViewSolutionWithSolverStats(viewSolution, solverStats);
//	}

	private ViewSolutionRegion mergeCliqueSolutionsRegionwiseTemp(List<FormalCondition> conditions,
			List<Region> conditionRegions, List<LinkedList<VariableValuePair>> cliqueIdxToVarValuesList) {

		IntList mergedColIndxs = new IntArrayList();
		List<ValueCombination> mergedValueCombinations = new ArrayList<>();
		// S
		List<VariableValuePair> mergedVarValuePairs = new ArrayList();
		// arasuCliquesAsColIndxs --list of list of columns index (only those present in
		// some constraint) in a subview.
		mergedColIndxs.addAll(arasuCliquesAsColIndxs.get(0));
		// Instantiating variables of first clique
		for (VariableValuePair varValuePair : cliqueIdxToVarValuesList.get(0)) {
			mergedVarValuePairs.add(varValuePair);
		}
		// Shadab, mergeWithAlignmentRegionwise deletes the regions from
		// arasuCliquesAsColIndxs. So if regions are needed post merging too, then make
		// a deep copy in the function.

		for (int i = 1; i < cliqueCount; i++) {
			mergeWithAlignmentRegionwiseTemp(mergedColIndxs, mergedVarValuePairs, arasuCliquesAsColIndxs.get(i),
					cliqueIdxToVarValuesList.get(i));
		}
		for (int j = 0; j < conditions.size(); j++) {

//		FormalCondition curCondition = conditions.get(j);
//		Set<String> clique = arasuCliques.get;(0);
//		Set<String> appearingCols = new HashSet<>();
//		getAppearingCols(appearingCols, curCondition); // appearing columns will have columns appeared in
//														// current condition
//
//		if (!clique.containsAll(appearingCols)) 
//			continue;
			Region currRegion = conditionRegions.get(j);

			FormalCondition condition = conditions.get(j);
			checkAccuracyHydra(condition, currRegion, mergedVarValuePairs, mergedColIndxs);
		}

//		for (VariableValuePair var : mergedVarValuePairs) {
//			IntList columnValues = getFloorInstantiation(var.variable);
//
//			ValueCombination valueCombination = new ValueCombination(columnValues, var.rowcount);
//			mergedValueCombinations.add(valueCombination);
//			var.variable = getExpandedRegion(var.variable);
//
//		}
//		for (ListIterator<ValueCombination> iter = mergedValueCombinations.listIterator(); iter.hasNext();) {
//			ValueCombination valueCombination = iter.next();
//			valueCombination = getReverseMappedValueCombination(valueCombination);
//			valueCombination = getExpandedValueCombination(valueCombination);
//			iter.set(valueCombination);
//		}
//
//		ViewSolutionInMemory viewSolutionInMemory = new ViewSolutionInMemory(mergedValueCombinations.size());
//		for (ValueCombination mergedValueCombination : mergedValueCombinations) {
//			viewSolutionInMemory.addValueCombination(mergedValueCombination);
//		}
//		ViewSolutionRegion viewSolution = new ViewSolutionRegion(viewSolutionInMemory, mergedVarValuePairs);

		return null;
		// commented by shadab return viewSolutionInMemory;
	}

	private void checkAccuracyHydra(FormalCondition condition, Region currRegion,
			List<VariableValuePair> mergedVarValuePairs, IntList mergedColIndxs) {
		// TODO Auto-generated method stub
		Long outputCard = 0L;
		Long projCard = 0L;
		List<String> groupkey = ((FormalConditionAggregate) condition).getGroupKey();
		List<Integer> groupkeyIdx = getColumnsIdx(groupkey);
		List<Integer> groupkeyIdxInValComb = new ArrayList<>();
		Long totTuples = 0L;
		for (Integer idx : groupkeyIdx) {
			groupkeyIdxInValComb.add(mergedColIndxs.indexOf(idx));
		}
		Region compressedCRegion = new Region();
		for (BucketStructure bs : currRegion.getAll()) {
			BucketStructure bsNew = new BucketStructure();
			for (int i = 0; i < bs.size(); i++) {
				if (mergedColIndxs.contains(i))
					bsNew.add(bs.at(i));
			}
			compressedCRegion.add(bsNew);
		}
		for (VariableValuePair regVar : mergedVarValuePairs) {
			if (compressedCRegion.contains(regVar.variable)) {
				outputCard += regVar.rowcount;
				projCard++;
			}
		}
		Long accuracyDistinct = ((FormalConditionAggregate) condition).getProjectionCardinality() - projCard;
		Long accuracyTot = condition.getOutputCardinality() - outputCard;

		System.out.println("Query " + condition.getQueryName());
		System.out.println("Output cardinality error = " + accuracyTot);
		System.out.println("Projection cardinality = " + accuracyDistinct);

	}

	private ViewSolutionRegion mergeCliqueSolutionsRegionwise(List<FormalCondition> conditions,
			List<Region> conditionRegions) {
		Set<String> columns = new HashSet<>();
		for (Set<String> clique : arasuCliques) {
			columns.addAll(clique);
		}

		StopWatch postLPsolutionSW = new StopWatch("merging " + viewname);
		IntList mergedColIndxs = new IntArrayList();
		List<RegionSummary> finalRegionSummary = new ArrayList<>();
		mergedColIndxs.addAll(arasuCliquesAsColIndxs.get(0));

		// Instantiating variables of first clique

		for (RegionSummary regSum : cliqueToRegionsSummary.get(0)) {
			finalRegionSummary.add(regSum);
		}

		// Shadab, mergeWithAlignmentRegionwise deletes the regions from
		// arasuCliquesAsColIndxs. So if regions are needed post merging too, then make
		// a deep copy in the function.
		for (int i = 1; i < cliqueCount; i++) {
//			System.out.println(i);
			mergeWithAlignmentRegionwise(mergedColIndxs, finalRegionSummary, arasuCliquesAsColIndxs.get(i),
					cliqueToRegionsSummary.get(i));
		}

		DatabaseSummaryProjection summ = new DatabaseSummaryProjection(finalRegionSummary);
		try {
			FileOutputStream f = new FileOutputStream("summary" + workloadNo + ".txt", true);
			ObjectOutputStream o = new ObjectOutputStream(f);

			// Write objects to file
			o.writeObject(summ);
			// o.writeObject(p2);
			System.out.println("Summary written for view" + viewname);
			o.close();
			f.close();

		} catch (FileNotFoundException e) {
			throw new RuntimeException("file not found");
		} catch (IOException e) {
			throw new RuntimeException("Error initializing stream");
		}
		finalRegionSummaryGlobal = finalRegionSummary;
//		// List<ValueCombinationF> finalValueCombinations=new ArrayList<>();
//
////		postLPsolutionSW.displayTimeAndDispose();
////		int count=0;

////		
////	
//		valueGenerationinitialization(finalRegionSummary.get(1), mergedColIndxs);
//		if (tupleGen) {
//			StopWatch postLPsolutionSW = new StopWatch("generation time " + viewname);
//			for (RegionSummary regSum : finalRegionSummary) {
//
//				System.out.println("Generating " + regSum.rowCount + "tuples");
//				StopWatch regionLPsolutionSW = new StopWatch("region generation " + viewname);
//				valueGenerationinitialization(regSum, mergedColIndxs);
//				System.out.println("Generated " + regSum.rowCount + "tuples");
//				regionLPsolutionSW.displayTimeAndDispose();
//			}
//			postLPsolutionSW.displayTimeAndDispose();
//		}

		if (wantAccuracy) {
			for (RegionSummary regSum : finalRegionSummary) {
				getRegionValueCombination(regSum, mergedColIndxs);
				// finalValueCombinations.addAll(getRegionValueCombination(regSum,mergedColIndxs));
			}
			for (int j = 0; j < conditions.size(); j++) {

				Region currRegion = conditionRegions.get(j);

				FormalCondition condition = conditions.get(j);
				// for (RegionSummary regSum : finalRegionSummary)
//				valueGenerationinitialization(regSum, mergedColIndxs);
				checkAccuracy(condition, currRegion, finalRegionSummary, mergedColIndxs);
			}
		}
		postLPsolutionSW.displayTimeAndDispose();
		// On the fly tuple generation begins.

		StopWatch onTheFlySW = new StopWatch("On-the fly time " + viewname);
		// initializing each region summary with a corner tuple
		for (RegionSummary regSum : finalRegionSummaryGlobal) {
			regSum.intialize(mergedColIndxs, bucketSplitPoints, splitPointsCount, getExpandedRegion(regSum.region),
					allTrueBS.size());
		}

		
		summIter = finalRegionSummaryGlobal.listIterator();
		regSum = summIter.next();

		if (tupleGen) {
			for (long i = 0; i < relationCardinality; i++) {
				getNextRec();
			}
		}

		onTheFlySW.displayTimeAndDispose();
//		for(int i =0; i<relationCardinality;i++) {
//			
//			if( regSum.getNextRec()==null) {
//				regSum=summIter.next();
//			}
//			if(i%1000000==0)
//				System.out.println(i);
//		}
		return null;
	}

	private void getNextRec() {
		if (regSum.getNextRec() == null) {
			regSum = summIter.next();// move on the next region.
		}

	}

////
////		for (VariableValuePair var : mergedVarValuePairs) {
////			IntList columnValues = getFloorInstantiation(var.variable);
////
////			ValueCombination valueCombination = new ValueCombination(columnValues, var.rowcount);
////			mergedValueCombinations.add(valueCombination);
////			var.variable = getExpandedRegion(var.variable);
////
////		}
////		for (ListIterator<ValueCombination> iter = mergedValueCombinations.listIterator(); iter.hasNext();) {
////			ValueCombination valueCombination = iter.next();
////			valueCombination = getReverseMappedValueCombination(valueCombination);
////			valueCombination = getExpandedValueCombination(valueCombination);
////			iter.set(valueCombination);
////		}
////
////		ViewSolutionInMemory viewSolutionInMemory = new ViewSolutionInMemory(mergedValueCombinations.size());
////		for (ValueCombination mergedValueCombination : mergedValueCombinations) {
////			viewSolutionInMemory.addValueCombination(mergedValueCombination);
////		}
////		ViewSolutionRegion viewSolution = new ViewSolutionRegion(viewSolutionInMemory, mergedVarValuePairs);
////
////		return viewSolution;
//		// commented by shadab return viewSolutionInMemory;
//	}

	private void checkAccuracy(FormalCondition condition, Region currRegion, List<RegionSummary> finalRegionSummary,
			IntList mergedColIndxs) {
		// TODO Auto-generated method stub
		List<String> groupkey = ((FormalConditionAggregate) condition).getGroupKey();
		List<Integer> groupkeyIdx = getColumnsIdx(groupkey);
		List<Integer> groupkeyIdxInValComb = new ArrayList<>();
		Long totTuples = 0L;
		for (Integer idx : groupkeyIdx) {
			groupkeyIdxInValComb.add(mergedColIndxs.indexOf(idx));
		}
		Region compressedCRegion = new Region();
		for (BucketStructure bs : currRegion.getAll()) {
			BucketStructure bsNew = new BucketStructure();
			for (int i = 0; i < bs.size(); i++) {
				if (mergedColIndxs.contains(i))
					bsNew.add(bs.at(i));
			}
			compressedCRegion.add(bsNew);
		}
		Set<List<Double>> extractedColumns = new HashSet<>();
		for (RegionSummary regSum : finalRegionSummary) {
			if (compressedCRegion.contains(regSum.region)) {
				// region satisfies curr condition
				for (ValueCombinationF valComb : regSum.valComb) {
					List<Double> projectionColumnValues = extractColumnValues(valComb.colValues, groupkeyIdxInValComb);// gets
																														// the
																														// columns
																														// for
																														// which
																														// disticnt
																														// has
																														// to
																														// be
																														// checked.
					totTuples += valComb.rowcount;
					extractedColumns.add(projectionColumnValues);// check if duplicates removed
				}
			}
		}
		Long accuracyDistinct = ((FormalConditionAggregate) condition).getProjectionCardinality()
				- extractedColumns.size();

		Long accuracyTot = condition.getOutputCardinality() - totTuples;

		System.out.println("Query " + condition.getQueryName());
		System.out.println("Output cardinality error = " + accuracyTot);
		System.out.println("Projection cardinality error = " + accuracyDistinct);
		if (!accuracyDistinct.equals(0L) || !accuracyTot.equals(0L))
			throw new RuntimeException("Accuracy error");
	}

	private List<Double> extractColumnValues(List<Double> colValues, List<Integer> groupkeyIdxInValComb) {
		// TODO Auto-generated method stub
		List<Double> ret = new ArrayList<>();
		for (Integer idx : groupkeyIdxInValComb) {
			ret.add(colValues.get(idx));
		}
		return ret;
	}

	private void valueGenerationinitialization(RegionSummary regSum, IntList mergedColIndxs) {

		Long numTuples = getNumTuples(regSum);
		Region region = regSum.region;
		Region expandedRegion = getExpandedRegion(region);
		if (regSum.groupkeys.isEmpty()) {
			// double [] tuple=new double[allTrueBS.size()];

			// First initializing with the tuple with the corner point.
			List<Double> tuple = new ArrayList<>(Collections.nCopies(allTrueBS.size(), 0.0));
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

		Map<List<Integer>, List<Pair<RegionF, Pair<Long, Long>>>> groupKeyToRegionF = regSum.groupKeyToRegionF;

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

		Map<List<Integer>, List<RegionF>> groupKeysToRegionFList = new HashMap<>();
		Map<List<Integer>, ListIterator<RegionF>> groupKeysToIterator = new HashMap<>();
		Map<List<Integer>, RegionF> groupKeysToCurRegionF = new HashMap<>();
		for (List<Integer> groupkeyIdx : groupKeyToRegionF.keySet()) {
			List<Pair<RegionF, Pair<Long, Long>>> regionsFList = groupKeyToRegionF.get(groupkeyIdx);
			for (Pair<RegionF, Pair<Long, Long>> regionFCur : regionsFList) {
				if (!groupKeysToRegionFList.containsKey(groupkeyIdx)) {
					groupKeysToRegionFList.put(groupkeyIdx, new ArrayList<>());

				}
				groupKeysToRegionFList.get(groupkeyIdx).add(regionFCur.getFirst());
			}
			groupKeysToCurRegionF.put(groupkeyIdx, regionsFList.get(0).getFirst());
			groupKeysToIterator.put(groupkeyIdx, groupKeysToRegionFList.get(groupkeyIdx).listIterator());
		}
		File file = null;
		try {
			file = File.createTempFile("shadab", ".txt");
			System.out.println("Created file" + file.getAbsolutePath());
		} catch (Exception e) {
			System.out.println();
		}
		for (Long i = 0L; i < numTuples; i++) {

			List<Double> tuple = new ArrayList<>(Collections.nCopies(allTrueBS.size(), 0.0));
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
//			ValueCombinationF temp = new ValueCombinationF(tuple, 0);
//			if (!temp.isPartOf(expandedRegion))
//				System.out.println("Check");
			try {
				writeRaw(tuple.toString(), file);
			} catch (Exception e) {
				System.out.println(e);
			}
		}

	}

	private static void writeRaw(String records, File file) throws IOException {

		try {
			FileWriter writer = new FileWriter(file);
			System.out.print("Writing raw... ");
			write(records, writer);
		} finally {
			// comment this out if you want to inspect the files afterward
//	        file.delete();
		}
	}

	private static void write(String records, Writer writer) throws IOException {
		long start = System.currentTimeMillis();

		writer.write(records);

		// writer.flush(); // close() should take care of this
		writer.close();
		long end = System.currentTimeMillis();
		System.out.println((end - start) / 1000f + " seconds");
	}

	private String tupleToString(double[] tuple) {
		// TODO Auto-generated method stub
		String ret = "";
		for (int i = 0; i < tuple.length; i++)
			ret += tuple[i] + "\t";
		return ret;
	}

	private void getRegionValueCombination(RegionSummary regSum, IntList mergedColIndxs) {
		// TODO Auto-generated method stub
		List<ValueCombinationF> ret = new ArrayList<>();
		Long numTuples = getNumTuples(regSum);// finds the maximum no. of distict tuples to be generated. Except this
												// all must be roound0-robin-ed to produce numtuples
		List<List<Double>> columnList = new ArrayList<>();// a;; columns stiched together
		// for every column in the mergedidx, we create a list
		for (int i = 0; i < mergedColIndxs.size(); i++) {
			columnList.add(new ArrayList<>());
		}

		Map<List<Integer>, List<Pair<RegionF, Pair<Long, Long>>>> groupKeyToRegionF = regSum.groupKeyToRegionF;
		// Long numTuples=0L;
		for (List<Integer> groupkeyIdx : groupKeyToRegionF.keySet()) {
			List<Pair<RegionF, Pair<Long, Long>>> regionsFList = groupKeyToRegionF.get(groupkeyIdx);

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
				List<Double> colValues = generateColumnValues(splitPoint, nextSplitPoint, splitCount, rowCount, cutOff);
				// wrongly done
				// List<Float>
				// colValues=generateValuesRounRobin(numTuples,splitPoint,nextSplitPoint,splitCount,rowCount,cutOff);
				// //gets enumerated column values
				columnList.get(mergedColIndxs.indexOf(groupkeyIdx.get(0))).addAll(colValues);
				// generate all other col in th egroup keys as they are coupled together( have
				// to be fromm the same regionF)
				for (int i = 1; i < groupkeyIdx.size(); i++) {
					int idx = groupkeyIdx.get(i);
					List<Double> list = new ArrayList<Double>();
					Double point = regionF.at(0).at(i).at(0);
					;
					list = generateOtherGroupkeysColumns(point, rowCount);
					columnList.get(mergedColIndxs.indexOf(idx)).addAll(list);
				}
			}
		}
		// generating single points for all the non groupkeycolumns.
		for (int i = 0; i < mergedColIndxs.size(); i++) {
			if (columnList.get(i).isEmpty()) {
				columnList.get(i).add((double) regSum.region.at(0).at(i).at(0));
			}
		}
		if (!numTuples.equals(0L)) {
			for (int i = 0; i < mergedColIndxs.size(); i++) {
				columnList.set(i, generateColumnRoundRobin(columnList.get(i), numTuples));
			}

			for (int i = 0; i < numTuples; i++) {
				ValueCombinationF valComb = new ValueCombinationF();
				for (int j = 0; j < columnList.size(); j++) {
					valComb.colValues.add(columnList.get(j).get(i));
				}
				valComb.rowcount = 1;
				ret.add(valComb);
			}
		} else {
			ValueCombinationF valComb = new ValueCombinationF();
			for (int j = 0; j < columnList.size(); j++) {
				valComb.colValues.add(columnList.get(j).get(0));
			}
			valComb.rowcount = 1;
			ret.add(valComb);
			numTuples++;
		}
		ret.get(0).rowcount += regSum.rowCount - numTuples;// getting extra for no groupkey region
		regSum.valComb = ret;
		sanityCheckForValComb(ret, regSum.region);
		// return ret;
	}

	private void sanityCheckForValComb(List<ValueCombinationF> ret, Region region) {
		// TODO Auto-generated method stub
		for (ValueCombinationF valComb : ret) {
			if (!valComb.isPartOf(region))
				System.out.println("wrong");
			// throw new RuntimeException("generated tuple not a part of region");
		}
	}

	private List<Double> generateColumnRoundRobin(List<Double> list, Long numTuples) {
		Long count = 0L;
		int count2 = 0;
		List<Double> fullColumn = new ArrayList<>();
		while (true) {
			for (double val : list) {
				fullColumn.add(val);
				count++;

				if (count.equals(numTuples))
					return fullColumn;
			}
		}
	}

	private List<Double> generateOtherGroupkeysColumns(double point, Long rowCount) {
		List<Double> col = new ArrayList<>();
		for (int i = 0; i < rowCount; i++)
			col.add(point);
		return col;
	}

	private List<Double> generateColumnValues(Double splitPoint, Double nextSplitPoint, Long splitCount, Long rowCount,
			Long cutOff) {
		Double intervalSize = nextSplitPoint - splitPoint;
		List<Double> fullColumn = new ArrayList<>();
		for (long i = 0; i < rowCount; i++) {
			fullColumn.add(splitPoint + (double) (i + cutOff) * (intervalSize / splitCount));
		}
		Set<Double> temp = new HashSet<>();
		temp.addAll(fullColumn);
		if (temp.size() != fullColumn.size())
			throw new RuntimeException("same value generated; sizes" + fullColumn.size() + "\t" + temp.size());
		return fullColumn;
	}

	private Long getNumTuples(RegionSummary regSum) {
		Map<List<Integer>, List<Pair<RegionF, Pair<Long, Long>>>> groupKeyToRegionF = regSum.groupKeyToRegionF;
		Long numTuples = 0L;
		for (List<Integer> groupkeyIdx : groupKeyToRegionF.keySet()) {

			List<Pair<RegionF, Pair<Long, Long>>> regionsFList = groupKeyToRegionF.get(groupkeyIdx);
			Long curNumTuples = 0L;
			for (Pair<RegionF, Pair<Long, Long>> regionFCur : regionsFList) {
				curNumTuples += regionFCur.getSecond().getFirst();
			}
			numTuples = Math.max(curNumTuples, numTuples);
		}
		if (numTuples == 0L)
			System.out.println("here1");
		return numTuples;
	}

	private void mergeWithAlignmentRegionwise(IntList lhsColIndxs, List<RegionSummary> finalRegionSummay,
			IntList rhsColIndxs, List<RegionSummary> rhsRegionSummary) {

		// Shadab-"snitches get stiches"
		// System.out.println("Shadab has reached here");
		IntList tempColIndxs = new IntArrayList(rhsColIndxs);
		tempColIndxs.removeAll(lhsColIndxs);
		IntList sharedColIndxs = new IntArrayList(rhsColIndxs);
		sharedColIndxs.removeAll(tempColIndxs);
		IntList posInLHS = new IntArrayList(sharedColIndxs.size());
		IntList posInRHS = new IntArrayList(sharedColIndxs.size());
		for (IntIterator iter = sharedColIndxs.iterator(); iter.hasNext();) {
			int sharedColIndx = iter.nextInt();
			posInLHS.add(lhsColIndxs.indexOf(sharedColIndx));
			posInRHS.add(rhsColIndxs.indexOf(sharedColIndx));
		}

		IntList mergedColIndxsList = new IntArrayList(lhsColIndxs);
		mergedColIndxsList.addAll(rhsColIndxs);
		mergedColIndxsList = new IntArrayList(new IntOpenHashSet(mergedColIndxsList));
		Collections.sort(mergedColIndxsList);

		boolean[] fromLHS = new boolean[mergedColIndxsList.size()];
		int[] correspOriginalIndx = new int[mergedColIndxsList.size()];
		for (int i = 0; i < mergedColIndxsList.size(); i++) {
			fromLHS[i] = lhsColIndxs.contains(mergedColIndxsList.get(i));
			if (fromLHS[i]) {
				correspOriginalIndx[i] = lhsColIndxs.indexOf(mergedColIndxsList.get(i));
			} else {
				correspOriginalIndx[i] = rhsColIndxs.indexOf(mergedColIndxsList.get(i));
			}
		}
		// adds the index of merged columns(from rhs or lhs)
		int count = 0;
		List<VariableValuePair> mergedVarValuePairs = new ArrayList<>();// new table after merging
		List<RegionSummary> mergedSummary = new ArrayList<>();
//		for (RegionSummary lhsSum : finalRegionSummay) {
		for (int idx = 0; idx < finalRegionSummay.size(); idx++) {
			RegionSummary lhsSum = finalRegionSummay.get(idx);
//			Region lhsRegion = lhsVarValue.variable;
			Region lhsRegion = lhsSum.region;
			long lhsRowcount = lhsSum.rowCount;
			boolean check = false;
			count++;
			// int ind = 0;
			for (ListIterator<RegionSummary> rhsIter = rhsRegionSummary.listIterator(); rhsIter.hasNext();) {
				RegionSummary rhsSum = rhsIter.next();
				Region rhsVariable = rhsSum.region;
				long rhsRowcount = rhsSum.rowCount;
				// ind++;
				// if (checkCompatibleRegions(posInLHS, lhsRegion, posInRHS, rhsVariable)) {//
				// if rhsregion is compatible
				// check = true;

				Region mergedTemp = new Region();// contains the region for the intersection of the two regions
				// int totBS = 0;

				for (int i = 0; i < lhsRegion.size(); i++) {// iterate over each clause
					BucketStructure lhsBS = lhsRegion.at(i);
					// int totmatch = 0;
					for (int j = 0; j < rhsVariable.size(); j++) {
						BucketStructure rhsBS = rhsVariable.at(j);
						BucketStructure mergedBS = new BucketStructure();// chceck -----------------contains the new
																			// clause
						if (isCompatibleBS(posInLHS, lhsBS, posInRHS, rhsBS)) {
							// totmatch++;
							// totBS++;
							int counter = 0;
							for (int k = 0; k < mergedColIndxsList.size(); k++) {
								if (sharedColIndxs.size() > counter
										&& mergedColIndxsList.get(k) == sharedColIndxs.get(counter)) {
									int lhsInd = posInLHS.get(counter);
									int rhsInd = posInRHS.get(counter);
									counter++;
									Bucket inter = Bucket.getIntersection(lhsBS.at(lhsInd), rhsBS.at(rhsInd));

									mergedBS.add(inter);
								} else if (fromLHS[k]) {
									mergedBS.add(lhsBS.at(correspOriginalIndx[k]));
									// mergedColValues.add(lhsColValues.getInt(correspOriginalIndx[j]));
								} else {
									mergedBS.add(rhsBS.at(correspOriginalIndx[k]));
								}
							}
							mergedTemp.add(mergedBS);
						}

					}
				}
				if (mergedTemp.isEmpty())
					continue;
				else {
					check = true;
					sanityCheckFullOverlap(posInLHS, lhsRegion, posInRHS, rhsVariable);
				}
				// do the rowcount
				if (lhsRowcount <= rhsRowcount) {
					RegionSummary mergedRegionSummary = new RegionSummary();
					mergedRegionSummary.region = mergedTemp;
					mergedRegionSummary.rowCount = lhsRowcount;
					mergedRegionSummary.groupkeys = new HashSet<>(lhsSum.groupkeys);
					mergedRegionSummary.groupkeys.addAll(rhsSum.groupkeys);// checked till here
					mergedRegionSummary.groupKeyToRegionF = lhsSum.groupKeyToRegionF;
					// VariableValuePair mergedVariable = new VariableValuePair(mergedTemp,
					// lhsRowcount);
					// mergedVarValuePairs.add(mergedVariable);
					mergedSummary.add(mergedRegionSummary);
					// lhsValueCombination.reduceRowcount(lhsRowcount);
					lhsSum.rowCount = 0L;

					if (rhsSum.rowCount == lhsRowcount) {
						mergedRegionSummary.groupKeyToRegionF.putAll(rhsSum.groupKeyToRegionF);
						rhsIter.remove();// removes this region from RHSvar
						rhsSum.rowCount -= lhsRowcount;// why needed?
					} else {
						RegionSummary brokenNew = rhsSum.divide(lhsRowcount);// rhs retains lhsrowcount tuples andbroken
																				// gets rest
						mergedRegionSummary.groupKeyToRegionF.putAll(rhsSum.groupKeyToRegionF);
						rhsIter.remove();
						// rhsIter.
						rhsRegionSummary.add(brokenNew);
					}
//				rhsSum.rowCount -= lhsRowcount;
					break;
				} else {
//						VariableValuePair mergedVariable = new VariableValuePair(mergedTemp, rhsRowcount);
					RegionSummary mergedRegionSummary = new RegionSummary();
					mergedRegionSummary.region = mergedTemp;
					mergedRegionSummary.rowCount = rhsRowcount;
					mergedSummary.add(mergedRegionSummary);
					mergedRegionSummary.groupkeys = lhsSum.groupkeys;
					mergedRegionSummary.groupkeys.addAll(rhsSum.groupkeys);
					mergedRegionSummary.groupKeyToRegionF = rhsSum.groupKeyToRegionF;
					RegionSummary brokenNew = lhsSum.divide(rhsRowcount);
					mergedRegionSummary.groupKeyToRegionF.putAll(lhsSum.groupKeyToRegionF);// added
					// lhsSum.rowCount -= rhsRowcount;
					finalRegionSummay.set(idx, brokenNew);
					lhsSum = brokenNew;
					lhsRowcount = lhsSum.rowCount;
					rhsSum.rowCount = 0L;
					rhsIter.remove();
				}

				// }

			}
			if (!check) {
				// System.out.println("failed");
				throw new RuntimeException("You Failed!!!!!Couldn't find a region in rhs for lhs region:" + lhsRegion);
			}

		}
		for (RegionSummary lhsSum : finalRegionSummay) {
			if (lhsSum.rowCount != 0)
				throw new RuntimeException("Found while merge Region " + lhsSum.region + " in LHS with unmet rowcount");
		}
		if (!rhsRegionSummary.isEmpty())
			throw new RuntimeException("Found while merge RHS variables not getting exhausted");

		// Updating targetColIndxs
		lhsColIndxs.clear();
		lhsColIndxs.addAll(mergedColIndxsList);

		finalRegionSummay.clear();
		finalRegionSummay.addAll(mergedSummary);
		int y = 0;
	}

	// private void mergeWithAlignmentRegionwiseTemp(IntList lhsColIndxs,
	// List<VariableValuePair> lhsVarValuePairs,
	// IntList rhsColIndxs, LinkedList<VariableValuePair> rhsVarValuePairs) {

	private void mergeWithAlignmentRegionwiseTemp(IntList lhsColIndxs, List<VariableValuePair> lhsVarValuePairs,
			IntList rhsColIndxs, LinkedList<VariableValuePair> rhsVarValuePairs) {
		// Shadab-"snitches get stiches"
		// System.out.println("Shadab has reached here");
		IntList tempColIndxs = new IntArrayList(rhsColIndxs);
		tempColIndxs.removeAll(lhsColIndxs);
		IntList sharedColIndxs = new IntArrayList(rhsColIndxs);
		sharedColIndxs.removeAll(tempColIndxs);
		IntList posInLHS = new IntArrayList(sharedColIndxs.size());
		IntList posInRHS = new IntArrayList(sharedColIndxs.size());
		for (IntIterator iter = sharedColIndxs.iterator(); iter.hasNext();) {
			int sharedColIndx = iter.nextInt();
			posInLHS.add(lhsColIndxs.indexOf(sharedColIndx));
			posInRHS.add(rhsColIndxs.indexOf(sharedColIndx));
		}

		IntList mergedColIndxsList = new IntArrayList(lhsColIndxs);
		mergedColIndxsList.addAll(rhsColIndxs);
		mergedColIndxsList = new IntArrayList(new IntOpenHashSet(mergedColIndxsList));
		Collections.sort(mergedColIndxsList);

		boolean[] fromLHS = new boolean[mergedColIndxsList.size()];
		int[] correspOriginalIndx = new int[mergedColIndxsList.size()];
		for (int i = 0; i < mergedColIndxsList.size(); i++) {
			fromLHS[i] = lhsColIndxs.contains(mergedColIndxsList.get(i));
			if (fromLHS[i]) {
				correspOriginalIndx[i] = lhsColIndxs.indexOf(mergedColIndxsList.get(i));
			} else {
				correspOriginalIndx[i] = rhsColIndxs.indexOf(mergedColIndxsList.get(i));
			}
		}
		// adds the index of merged columns(from rhs or lhs)
		int count = 0;
		// List<RegionSummary>mergedRegionsSummary=new ArrayList<>();
		List<VariableValuePair> mergedVarValuePairs = new ArrayList<>();// new table after merging
		// List<RegionSummary> mergedSum = new ArrayList<>();// new table after merging
		for (VariableValuePair lhsVarValue : lhsVarValuePairs) {
			Region lhsRegion = lhsVarValue.variable;
			long lhsRowcount = lhsVarValue.rowcount;
			boolean check = false;
			count++;
			int ind = 0;
			for (ListIterator<VariableValuePair> rhsIter = rhsVarValuePairs.listIterator(); rhsIter.hasNext();) {
				VariableValuePair rhsVarValuePair = rhsIter.next();
				Region rhsVariable = rhsVarValuePair.variable;
				long rhsRowcount = rhsVarValuePair.rowcount;
				ind++;
				// if (checkCompatibleRegions(posInLHS, lhsRegion, posInRHS, rhsVariable)) {//
				// if rhsregion is compatible
				// check = true;

				Region mergedTemp = new Region();// contains the region for the intersection of the two regions
				int totBS = 0;

				for (int i = 0; i < lhsRegion.size(); i++) {// iterate over each clause
					BucketStructure lhsBS = lhsRegion.at(i);
					int totmatch = 0;
					for (int j = 0; j < rhsVariable.size(); j++) {
						BucketStructure rhsBS = rhsVariable.at(j);
						BucketStructure mergedBS = new BucketStructure();// chceck -----------------contains the new
						// clause
						if (isCompatibleBS(posInLHS, lhsBS, posInRHS, rhsBS)) {
							totmatch++;
							totBS++;
							int counter = 0;
							for (int k = 0; k < mergedColIndxsList.size(); k++) {
								if (sharedColIndxs.size() > counter
										&& mergedColIndxsList.get(k) == sharedColIndxs.get(counter)) {
									int lhsInd = posInLHS.get(counter);
									int rhsInd = posInRHS.get(counter);
									counter++;
									Bucket inter = Bucket.getIntersection(lhsBS.at(lhsInd), rhsBS.at(rhsInd));

									mergedBS.add(inter);
								} else if (fromLHS[k]) {
									mergedBS.add(lhsBS.at(correspOriginalIndx[k]));
									// mergedColValues.add(lhsColValues.getInt(correspOriginalIndx[j]));
								} else {
									mergedBS.add(rhsBS.at(correspOriginalIndx[k]));
								}
							}
							mergedTemp.add(mergedBS);
						}

					}
				}
				if (mergedTemp.isEmpty())
					continue;
				else {
					check = true;
				}
				// do the rowcount
				if (lhsRowcount <= rhsRowcount) {
					VariableValuePair mergedVariable = new VariableValuePair(mergedTemp, lhsRowcount);
					mergedVarValuePairs.add(mergedVariable);
					// lhsValueCombination.reduceRowcount(lhsRowcount);
					lhsVarValue.rowcount = 0;
					rhsVarValuePair.rowcount -= lhsRowcount;
					if (rhsVarValuePair.rowcount == 0) {
						rhsIter.remove();// removes this region from RHSvar
					}
					break;
				} else {
					VariableValuePair mergedVariable = new VariableValuePair(mergedTemp, rhsRowcount);
					mergedVarValuePairs.add(mergedVariable);
					lhsVarValue.rowcount -= rhsRowcount;
					lhsRowcount = lhsVarValue.rowcount;
					rhsVarValuePair.rowcount = 0;
					rhsIter.remove();
				}

			}
			if (!check) {
				// System.out.println("failed");
				throw new RuntimeException("You Failed!!!!!Couldn't find a region in rhs for lhs region:" + lhsRegion);
			}

		}
		for (VariableValuePair lhsRegion : lhsVarValuePairs) {
			if (lhsRegion.rowcount != 0)
				throw new RuntimeException(
						"Found while merge ValueCombination " + lhsRegion + " in LHS with unmet rowcount");
		}
		if (!rhsVarValuePairs.isEmpty())
			throw new RuntimeException("Found while merge RHS variables not getting exhausted");

		// Updating targetColIndxs
		lhsColIndxs.clear();
		lhsColIndxs.addAll(mergedColIndxsList);

		lhsVarValuePairs.clear();
		lhsVarValuePairs.addAll(mergedVarValuePairs);

	}

	private void mergeWithAlignmentRegionwiseUniform(IntList lhsColIndxs, List<VariableValuePair> lhsVarValuePairs,
			IntList rhsColIndxs, LinkedList<VariableValuePair> rhsVarValuePairs) {
		List<VariableValuePair> mergedVarValuePairs = new ArrayList<>();// new table after merging
		// Shadab-"snitches get stitches"
		// System.out.println("Shadab has reached here");
		IntList tempColIndxs = new IntArrayList(rhsColIndxs);
		tempColIndxs.removeAll(lhsColIndxs);
		IntList sharedColIndxs = new IntArrayList(rhsColIndxs);
		sharedColIndxs.removeAll(tempColIndxs);
		IntList posInLHS = new IntArrayList(sharedColIndxs.size());
		IntList posInRHS = new IntArrayList(sharedColIndxs.size());
		for (IntIterator iter = sharedColIndxs.iterator(); iter.hasNext();) {
			int sharedColIndx = iter.nextInt();
			posInLHS.add(lhsColIndxs.indexOf(sharedColIndx));
			posInRHS.add(rhsColIndxs.indexOf(sharedColIndx));
		}

		IntList mergedColIndxsList = new IntArrayList(lhsColIndxs);
		mergedColIndxsList.addAll(rhsColIndxs);
		mergedColIndxsList = new IntArrayList(new IntOpenHashSet(mergedColIndxsList));
		Collections.sort(mergedColIndxsList);

		boolean[] fromLHS = new boolean[mergedColIndxsList.size()];
		int[] correspOriginalIndx = new int[mergedColIndxsList.size()];
		for (int i = 0; i < mergedColIndxsList.size(); i++) {
			fromLHS[i] = lhsColIndxs.contains(mergedColIndxsList.get(i));
			if (fromLHS[i]) {
				correspOriginalIndx[i] = lhsColIndxs.indexOf(mergedColIndxsList.get(i));
			} else {
				correspOriginalIndx[i] = rhsColIndxs.indexOf(mergedColIndxsList.get(i));
			}
		}
		// adds the index of merged columns(from rhs or lhs)
		int count = 0;
//	List<RegionSummary>mergedRegionsSummary=new ArrayList<>();

		// List<RegionSummary> mergedSum= new ArrayList<>();// new table after merging
		for (VariableValuePair lhsVarValue : lhsVarValuePairs) {
			Region lhsRegion = lhsVarValue.variable;
			long lhsRowcount = lhsVarValue.rowcount;
			List<VariableValuePair> compatitbleRegions = new ArrayList<>();
			List<VariableValuePair> mergedVars = new ArrayList<>();
			boolean check = false;
			count++;
			int ind = 0;
			for (ListIterator<VariableValuePair> rhsIter = rhsVarValuePairs.listIterator(); rhsIter.hasNext();) {
				VariableValuePair rhsVarValuePair = rhsIter.next();
				Region rhsVariable = rhsVarValuePair.variable;
				long rhsRowcount = rhsVarValuePair.rowcount;
				ind++;
				// if (checkCompatibleRegions(posInLHS, lhsRegion, posInRHS, rhsVariable)) {//
				// if rhsregion is compatible
				// check = true;

				Region mergedTemp = new Region();// contains the region for the intersection of the two regions
				int totBS = 0;

				for (int i = 0; i < lhsRegion.size(); i++) {// iterate over each clause
					BucketStructure lhsBS = lhsRegion.at(i);
					int totmatch = 0;
					for (int j = 0; j < rhsVariable.size(); j++) {
						BucketStructure rhsBS = rhsVariable.at(j);
						BucketStructure mergedBS = new BucketStructure();// chceck -----------------contains the new
																			// clause
						if (isCompatibleBS(posInLHS, lhsBS, posInRHS, rhsBS)) {
							totmatch++;
							totBS++;
							int counter = 0;
							for (int k = 0; k < mergedColIndxsList.size(); k++) {
								if (sharedColIndxs.size() > counter
										&& mergedColIndxsList.get(k) == sharedColIndxs.get(counter)) {
									int lhsInd = posInLHS.get(counter);
									int rhsInd = posInRHS.get(counter);
									counter++;
									Bucket inter = Bucket.getIntersection(lhsBS.at(lhsInd), rhsBS.at(rhsInd));

									mergedBS.add(inter);
								} else if (fromLHS[k]) {
									mergedBS.add(lhsBS.at(correspOriginalIndx[k]));
									// mergedColValues.add(lhsColValues.getInt(correspOriginalIndx[j]));
								} else {
									mergedBS.add(rhsBS.at(correspOriginalIndx[k]));
								}
							}
							mergedTemp.add(mergedBS);
						}

					}
				}
				if (mergedTemp.isEmpty())
					continue;
				else {
					VariableValuePair mergedVar = new VariableValuePair(mergedTemp, 0);
					compatitbleRegions.add(rhsVarValuePair);
					mergedVars.add(mergedVar);
					check = true;
				}
			}

			// do the rowcount
//					if (lhsRowcount <= rhsRowcount) { 
//						
//						
//						VariableValuePair mergedVariable = new VariableValuePair(mergedTemp, lhsRowcount);
//						mergedVarValuePairs.add(mergedVariable);
//						
//						// lhsValueCombination.reduceRowcount(lhsRowcount);
//						lhsVarValue.rowcount = 0;
//						rhsVarValuePair.rowcount -= lhsRowcount;
//						if (rhsVarValuePair.rowcount == 0) {
//							rhsIter.remove();// removes this region from RHSvar
//						}
//						break;
//					} else {
//						VariableValuePair mergedVariable = new VariableValuePair(mergedTemp, rhsRowcount);
//						mergedVarValuePairs.add(mergedVariable);
//						lhsVarValue.rowcount -= rhsRowcount;
//						lhsRowcount = lhsVarValue.rowcount;
//						rhsVarValuePair.rowcount = 0;
//						rhsIter.remove();
//					}
//
//				
//
//			}
//			if (!check) {
//				// System.out.println("failed");
//				throw new RuntimeException("You Failed!!!!!Couldn't find a region in rhs for lhs region:" + lhsRegion);
//			}
			if (compatitbleRegions.size() == 0)
				throw new RuntimeException("You Failed!!!!!Couldn't find a region in rhs for lhs region:" + lhsRegion);
			mergeUniform(lhsVarValue, compatitbleRegions, mergedVars);
			mergedVarValuePairs.addAll(mergedVars);
		}
		for (VariableValuePair lhsRegion : lhsVarValuePairs) {
			if (lhsRegion.rowcount != 0)
				throw new RuntimeException(
						"Found while merge ValueCombination " + lhsRegion + " in LHS with unmet rowcount");
		}
		for (VariableValuePair var : rhsVarValuePairs) {
			if (var.rowcount != 0)
				throw new RuntimeException("Found while merge LHS variables not getting exhausted");

		}

		// Updating targetColIndxs
		lhsColIndxs.clear();
		lhsColIndxs.addAll(mergedColIndxsList);

		lhsVarValuePairs.clear();
		lhsVarValuePairs.addAll(mergedVarValuePairs);

	}

	private void mergeUniform(VariableValuePair lhsVarValue, List<VariableValuePair> compatitbleRegions,
			List<VariableValuePair> mergedVars) {
		Long totRowcount = 0L;
		Long lhsRowcount = lhsVarValue.rowcount;
		for (VariableValuePair var : compatitbleRegions) {
			totRowcount += var.rowcount;
		}

		for (int i = 0; i < compatitbleRegions.size() - 1; i++) {
			VariableValuePair rhsvar = compatitbleRegions.get(i);
			VariableValuePair mergedVar = mergedVars.get(i);
			double ratio = rhsvar.rowcount / (double) totRowcount;
			Long card = Math.round(ratio * lhsRowcount);
			mergedVar.rowcount = card;
			lhsVarValue.rowcount -= card;
			rhsvar.rowcount -= card;
			// divideCard(var,card);

		}
		VariableValuePair lastmerged = mergedVars.get(compatitbleRegions.size() - 1);
		lastmerged.rowcount = lhsVarValue.rowcount;
		VariableValuePair lastrhs = compatitbleRegions.get(compatitbleRegions.size() - 1);
		lastrhs.rowcount -= lhsVarValue.rowcount;
		if (lastrhs.rowcount < 0L)
			throw new RuntimeException("check mergeuniform");

	}

	private static boolean isFullOverlap(IntList posInLHS, Region lhsRegion, IntList posInRHS, Region rhsRegion) {
		// returns true if two regions are consistent.
//		System.out.println(getTruncatedRegion(lhsRegion, posInLHS));
//		System.out.println();
//		System.out.println(getTruncatedRegion(rhsRegion, posInRHS));

		for (IntIterator iterLHS = posInLHS.iterator(), iterRHS = posInRHS.iterator(); iterLHS.hasNext();) {
			int posL = iterLHS.nextInt();
			int posR = iterRHS.nextInt();
			Bucket lB = new Bucket();
			Bucket rB = new Bucket();

			for (BucketStructure bs : lhsRegion.getAll())
				lB = Bucket.union(lB, bs.at(posL));
			// lhsUnion.add(lB);
			for (BucketStructure bs : rhsRegion.getAll())
				rB = Bucket.union(rB, bs.at(posR));
			// lhsUnion.add(rB);
			if (!lB.isEqual(rB))//
			{
//				System.out.println(lB);
//				System.out.println(rB);
//				System.out.println("pos in lhs" + posInLHS);
//				System.out.println("pos in rhs" + posInRHS);
//				System.out.println(lhsRegion);
//				System.out.println(rhsRegion);
				System.out.println("Not fully overlappping");
				return false;
			}
			// if (lB.intersection(rB).isEmpty())
//				return false;
		}
		return true;
	}

	private static void sanityCheckFullOverlap(IntList posInLHS, Region lhsRegion, IntList posInRHS, Region rhsRegion) {
		// returns true if two regions are consistent.

		for (IntIterator iterLHS = posInLHS.iterator(), iterRHS = posInRHS.iterator(); iterLHS.hasNext();) {
			int posL = iterLHS.nextInt();
			int posR = iterRHS.nextInt();
			Bucket lB = new Bucket();
			Bucket rB = new Bucket();

			for (BucketStructure bs : lhsRegion.getAll())
				lB = Bucket.union(lB, bs.at(posL));
			// lhsUnion.add(lB);
			for (BucketStructure bs : rhsRegion.getAll())
				rB = Bucket.union(rB, bs.at(posR));
			// lhsUnion.add(rB);
			if (!lB.isEqual(rB))//
			{
//				System.out.println(lB);
//				System.out.println(rB);
//				System.out.println("pos in lhs" + posInLHS);
//				System.out.println("pos in rhs" + posInRHS);
//				System.out.println(lhsRegion);
//				System.out.println(rhsRegion);
				throw new RuntimeException("Not fully overlappping");
			}
			// if (lB.intersection(rB).isEmpty())
//				return false;
		}
	}

	private static boolean isCompatibleBS(IntList posInLHS, BucketStructure lhsBs, IntList posInRHS,
			BucketStructure rhsBs) {
		// returns true if two BS's are consistent wrt common attribute

		for (IntIterator iterLHS = posInLHS.iterator(), iterRHS = posInRHS.iterator(); iterLHS.hasNext();) {
			int posL = iterLHS.nextInt();
			int posR = iterRHS.nextInt();
			Bucket lB = lhsBs.at(posL);
			Bucket rB = rhsBs.at(posR);
			// if (!lhsBs.at(posL).equals(rhsBs.at(posR))
			if (Bucket.getIntersection(lB, rB) == null)
				return false;

			// if (!RhsBs.at(posR).contains(lhsColValues.getInt(posL)))
			// return false;
		}
//        for (IntIterator iterLHS = posInLHS.iterator(), iterRHS = posInRHS.iterator(); iterLHS.hasNext();) {
//            int posL = iterLHS.nextInt();
//            int posR = iterRHS.nextInt();
//            Bucket lVal=lhsBs.at(posL);
//            Bucket rVal=rhsBs.at(posR);
//            if (!lVal.isEqual(rVal)){
//            	//throw new RuntimeException("Buckets intersect but not complete overlap");
//            	System.out.println("Gotcha");
//            }
//        }
		return true;
	}

	private List<LinkedList<VariableValuePair>> formulateAndSolve(List<FormalCondition> conditions,
			List<Region> conditionRegions, FormalCondition consistencyConstraints[],
			ConsistencyMakerType consistencyMakerType) {

		// TODO : remove IntExpr with their original String names in projection code
		// CodeS
		// initializing split points with initial intervals

		/*
		 * Bucket b1 = new Bucket(); b1.add(0); Bucket b2 = new Bucket(); b2.add(1); //
		 * b2.add(1); Bucket b3 = new Bucket(); b3.add(0); Bucket b4 = new Bucket();
		 * b4.add(0);
		 * 
		 * BucketStructure bs1 = new BucketStructure(); BucketStructure bs2 = new
		 * BucketStructure(); bs1.add(b1); bs1.add(b2); bs2.add(b3); bs2.add(b4); Region
		 * reg1 = new Region(); reg1.add(bs1); reg1.add(bs2); List<Integer> columnsIdx =
		 * new ArrayList<>(); // // makeSymmetricTemp(reg1);
		 * 
		 * Map<BucketStructure, Integer> tempMap = new HashMap<>(); tempMap.put(bs1, 1);
		 * tempMap.put(bs2, 2); int ans = tempMap.get(bs2); Set<BucketStructure> set =
		 * new HashSet<>(); set.add(bs1); set.add(bs2);
		 */
		StopWatch regionPartitioning = new StopWatch("Region Partitioning + symmetric refinement" + viewname);
		bucketSplitPoints = new ArrayList<>();
		splitPointsCount = new ArrayList<>();
		for (boolean[] allTrueB : allTrueBS) {

			List<Double> splits = new ArrayList<>();
			for (int i = 0; i < allTrueB.length; i++) {
				splits.add((double) (i));
			}
			bucketSplitPoints.add(splits);
		}

		// endS

//[t07_c027, t07_c005, t05_c005, t11_c002, t11_c011, t07_c018, t07_c019] 146,34,165

		List<List<FormalCondition>> cliqueToAggregateConditions = new ArrayList<>();

		// STEP 1: For each clique find set of applicable conditions and call variable
		// reduction
		// also has codeS

		for (FormalCondition fc : conditions) {
			Set<String> appearingCols = new HashSet<>();
			getAppearingColsTemp(appearingCols, fc);
			appearingCols.retainAll(((FormalConditionAggregate) fc).getGroupKey());
			if (!appearingCols.isEmpty()) {
				System.out.println(fc.getQueryName());
//				Set<String> appearingColsTemp = new HashSet<>();
//				getAppearingColsTemp(appearingColsTemp, fc);
				System.out.println("check this");
//				System.out.println(appearingColsTemp);
//				System.out.println(((FormalConditionAggregate)fc).getGroupKey());
			}
		}
		List<List<Region>> cliqueToCRegions = new ArrayList<>();
		for (int i = 0; i < cliqueCount; i++) {
			System.out.println();
			System.out.println("clique" + i);
			LongList bvalues = new LongArrayList();
			Set<String> clique = arasuCliques.get(i); // set of columns
			System.out.println(i + "  " + clique);
			List<Region> cRegions = new ArrayList<>();
			List<Pair<Integer, Boolean>> applicableConditions = new ArrayList<>();

			// codeS

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
						System.out.println(curCondition.getQueryName());
						applicableConditions.add(new Pair<>(j, true));
						aggregateConditions.add(curCondition);// codeS
					} else
						applicableConditions.add(new Pair<>(j, false));
				}

			}
			cliqueToAggregateConditions.add(aggregateConditions);// codeS

			applicableConditionsOnClique.add(applicableConditions);

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
					// emoveAggregateCols(appearingCols,(FormalConditionAggregate)condition);
					if (clique.containsAll(appearingCols)) {
//						System.out.println("cons const no" + j);
						indexKeeperForConsistency.put(j, tempIndexForConsistency++);
						cRegions.add(conditionRegions.get(conditions.size() + j));
					}
				}

				mappedIndexOfConsistencyConstraint.add(indexKeeperForConsistency);
			}

			cliqueToCRegions.add(cRegions);
			Reducer reducer = new Reducer(allTrueBS, cRegions);
			// Map<IntList, Region> P = reducer.getMinPartition();

			// Using varIds instead of old variable regions
			List<Region> oldVarList = new ArrayList<>(); // List of regions corresponding to below labels
			List<IntList> conditionIdxsList = new ArrayList<>(); // List of labels
			reducer.getVarsAndConditionsSimplified(oldVarList, conditionIdxsList);

			Partition cliqueP = new Partition(new ArrayList<>(oldVarList));
			cliqueIdxtoPList.add(cliqueP);
//			System.out.println("Printing regions for clique=" + i);
			cliqueIdxtoPSimplifiedList.add(conditionIdxsList);
			System.out.println("clique " + i);
			// DatabaseSummaryProjection summ = new
			// DatabaseSummaryProjection(finalRegionSummary);

//				try {
//					FileOutputStream f = new FileOutputStream("regionList" + i + ".txt");
//					ObjectOutputStream o = new ObjectOutputStream(f);
//
//					// Write objects to file
//					o.writeObject(cliqueP);
//					// o.writeObject(p2);
//					System.out.println("RegionList written for view" + viewname);
//
//				} catch (FileNotFoundException e) {
//					System.out.println("File not found");
//				} catch (IOException e) {
//					System.out.println(e);
//					System.out.println("Error initializing stream");
//				}

//			for (Region reg : cliqueP.getAll()) {
//				System.out.println(getTruncatedRegion(reg, arasuCliquesAsColIndxs.get(i)) + "--------end");
//			}

		}

//		
//		boolean end = true;
//		if (end) {
//			System.out.println("Ending now");
//			return null;
//		}

//		Partition partTemp = null;
//		try {
//			FileInputStream fin = new FileInputStream("regionList" + 1 + ".txt");
//			ObjectInputStream in = new ObjectInputStream(fin);
//			partTemp = (Partition) in.readObject();
//			in.close();
//			fin.close();
//		} catch (FileNotFoundException e) {
//			System.out.println("File not found");
//		} catch (IOException e) {
//			System.out.println(e);
//			System.out.println("Error initializing stream");
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		boolean compare = comparePList(partTemp, cliqueIdxtoPList.get(1));
//		System.out.println("printed");
		if (!ignoreProjection) {
//			List<List<Region>> cliqueIdxtoPListComp = new ArrayList<>();
//			for (int i = 0; i < cliqueCount; i++) {
//
//				List<Region> PListComp = new ArrayList<>();
//				IntList colIndxs = arasuCliquesAsColIndxs.get(i);
//
//				for (int idx = 0; idx < cliqueIdxtoPList.get(i).size(); idx++) {
//					Region reg = cliqueIdxtoPList.get(i).getAll().get(idx);
//
//					PListComp.add(getTruncatedRegion(reg, colIndxs));
//				}
//				cliqueIdxtoPListComp.add(PListComp);
//			}

			// ************shadab projection region preprocessing
			// starts*********************//

			// generating a map from groupkey to aggregate condition
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

			/*
			 * List<Map<List<String>, MaformulateAndSolvep<FormalConditionAggregate,
			 * List<Integer>>>> cliqueToGroupkeyConditionToRegionIdx = new ArrayList<>();
			 * List<Map<List<String>, List<Integer>>> cliqueToGroupkeyToRegionIdx = new
			 * ArrayList<>();
			 */

			// end
//			for (int i = 0; i < lhsRegion.size(); i++) {// iterate over each clause
//				BucketStructure lhsBS = lhsRegion.at(i);
//				// int totmatch = 0;
//				for (int j = 0; j < rhsVariable.size(); j++) {
//					BucketStructure rhsBS = rhsVariable.at(j);
//					BucketStructure mergedBS = new BucketStructure();// chceck -----------------contains the new
//																		// clause
//					if (isCompatibleBS(posInLHS, lhsBS, posInRHS, rhsBS)) {

			long varcountDRold = 0;
			long varcountDR = 0;
			for (int i = 0; i < cliqueCount; i++) {
				varcountDRold += cliqueIdxtoPList.get(i).getAll().size();
			}
			DebugHelper
					.printInfo("Number of variables after double reduction before making symmetric " + varcountDRold);

//-----------------------------code done----------------------------------------------------
			int newRegionsCount = 0;
			int numOfUnsymmReg = 0;
			if (wantSymmetric) {
//				boolean checkSymmetryandConsistency = false;
				boolean areUnsymm = false;
				List<List<Region>> cliqueToNewRegions = new ArrayList<>();

				for (int i = 0; i < cliqueCount; i++) {
//						System.out.println("clique" + i);

					List<Region> newRegions = new ArrayList<>();
					List<Region> regionList = cliqueIdxtoPList.get(i).getAll();
					/*
					 * for (List<String> groupKey : cliqueGroupkeyToConditions.get(i).keySet()) {
					 * 
					 * for (int k = 0; k < cliqueGroupkeyToConditions.get(i).get(groupKey).size();
					 * k++) {
					 * 
					 * Region cRegion =
					 * cliqueGroupkeyToConditionRegions.get(i).get(groupKey).get(k);
					 * 
					 * for (int j = 0; j < regionList.size(); j++) { Region region =
					 * regionList.get(j);
					 * 
					 * if (cRegion.contains(region)) { if (!symmetryCheck(region,
					 * getColumnsIdx(groupKey))) { List<Region> symmRegions =
					 * makeSymmetricNew(region, getColumnsIdx(groupKey));
					 * newRegions.addAll(symmRegions); numOfUnsymmReg++; } // j = j +
					 * symmRegions.size() - 1; } } } }
					 */

					for (int j = 0; j < regionList.size(); j++) {
						Region region = regionList.get(j);
						for (List<String> groupKey : cliqueGroupkeyToConditions.get(i).keySet()) {

							for (int k = 0; k < cliqueGroupkeyToConditions.get(i).get(groupKey).size(); k++) {

								Region cRegion = cliqueGroupkeyToConditionRegions.get(i).get(groupKey).get(k);

//							for (int j = 0; j < regionList.size(); j++) {
//								Region region = regionList.get(j);

								if (cRegion.contains(region)) {
									if (!symmetryCheck(region, getColumnsIdx(groupKey))) {
										List<Region> symmRegions = makeSymmetricNew(region, getColumnsIdx(groupKey));
										newRegions.addAll(symmRegions);
										numOfUnsymmReg++;
										areUnsymm = true;
										newRegionsCount += symmRegions.size();
									}
//									System.out.println("break");
									break;
									// j = j + symmRegions.size() - 1;
								}
							}
						}
					}
//						System.out.println("clique "+i);
//						for(Region reg:newRegions) {
//							System.out.println(getTruncatedRegion(reg, arasuCliquesAsColIndxs.get(i)));
//						}
					cliqueToNewRegions.add(newRegions);

				}
				// if (numOfUnsymmReg != 0)
				System.out.println("Number of unsymm regions" + " =" + numOfUnsymmReg);
				System.out.println("Number of broken regions" + " =" + newRegionsCount);

				if (areUnsymm) {// only if there are unsymmetric regions, consitency needs to done
					int consistencyConstraintNum = consistencyConstraints.length;

//				checkSymmetryandConsistency = true;
					// making consistent after making symmetric

					for (int i = 0; i < cliqueCount; i++) {
						IntList clique1 = arasuCliquesAsColIndxs.get(i);
						for (int j = i + 1; j < cliqueCount; j++) {
//							if (i == j) {
//								cliqueToCRegions.get(i).addAll(cliqueToNewRegions.get(i));
//								continue;
//							}
							IntList common = new IntArrayList();
							common.addAll(arasuCliquesAsColIndxs.get(j));

							common.retainAll(clique1);
							if (common.isEmpty())
								continue;
//						List<Region> addRegions1 = new ArrayList<>();
//						List<Region> addRegions2 = new ArrayList<>();
							for (Region reg : cliqueToNewRegions.get(i)) {
								Region currInterval = getIntervalRegion(reg, common);
//								System.out.println(getTruncatedRegion(reg, common));
								cliqueToCRegions.get(i).add(currInterval);
								cliqueToCRegions.get(j).add(currInterval);

								mappedIndexOfConsistencyConstraint.get(i).put(consistencyConstraintNum,
										mappedIndexOfConsistencyConstraint.get(i).size());
								mappedIndexOfConsistencyConstraint.get(j).put(consistencyConstraintNum,
										mappedIndexOfConsistencyConstraint.get(j).size());
								consistencyConstraintNum++;
//								addRegions1.add(currInterval);
//							
							}

							for (Region reg : cliqueToNewRegions.get(j)) {
								Region currInterval = getIntervalRegion(reg, common);
								cliqueToCRegions.get(i).add(currInterval);
								cliqueToCRegions.get(j).add(currInterval);
								mappedIndexOfConsistencyConstraint.get(i).put(consistencyConstraintNum,
										mappedIndexOfConsistencyConstraint.get(i).size());
								mappedIndexOfConsistencyConstraint.get(j).put(consistencyConstraintNum,
										mappedIndexOfConsistencyConstraint.get(j).size());
								consistencyConstraintNum++;
//								addRegions2.add(currInterval);
							}
//							cliqueToNewRegions.get(i).addAll(addRegions1);
//							cliqueToNewRegions.get(j).addAll(addRegions1);
//							cliqueToNewRegions.get(i).addAll(addRegions2);
//							cliqueToNewRegions.get(j).addAll(addRegions2);

						}
					}

					// Domain partitioning on the basis of new regions formed after symmetry

					cliqueIdxtoPSimplifiedList.clear();
					cliqueIdxtoPList.clear();
					for (int i = 0; i < cliqueCount; i++) {
						Reducer reducer = new Reducer(allTrueBS, cliqueToCRegions.get(i));
						// Map<IntList, Region> P = reducer.getMinPartition();

						// Using varIds instead of old variable regions
						List<Region> oldVarList = new ArrayList<>(); // List of regions corresponding to below labels
						List<IntList> conditionIdxsList = new ArrayList<>(); // List of labels
						reducer.getVarsAndConditionsSimplified(oldVarList, conditionIdxsList);

						Partition cliqueP = new Partition(new ArrayList<>(oldVarList));
						cliqueIdxtoPList.add(cliqueP);

						cliqueIdxtoPSimplifiedList.add(conditionIdxsList);
					}

					// checking for symmetry. After making consistent, symmetry can be lost??

//					cliqueToNewRegions.clear();
					for (int i = 0; i < cliqueCount; i++) {
//						System.out.println("here clique" + i);
//					List<Region> newRegions = new ArrayList<>();
						List<Region> regionList = cliqueIdxtoPList.get(i).getAll();

						for (int j = 0; j < regionList.size(); j++) {
							Region region = regionList.get(j);
							for (List<String> groupKey : cliqueGroupkeyToConditions.get(i).keySet()) {

								for (int k = 0; k < cliqueGroupkeyToConditions.get(i).get(groupKey).size(); k++) {

									Region cRegion = cliqueGroupkeyToConditionRegions.get(i).get(groupKey).get(k);

									if (cRegion.contains(region)) {
										if (!symmetryCheck(region, getColumnsIdx(groupKey))) {

											List<Region> symmRegions = makeSymmetricNew(region,
													getColumnsIdx(groupKey));
											region = symmRegions.get(0);
											regionList.set(j, symmRegions.get(0));
											regionList.addAll(symmRegions.subList(1, symmRegions.size()));
										}
										break;
									}
								}
							}
						}

					}

//				---------------Construct to check consistency--------------------------
					for (int i = 0; i < cliqueCount; i++) {

						IntList clique1 = arasuCliquesAsColIndxs.get(i);
						for (int j = i + 1; j < cliqueCount; j++) {
							;
							IntList common = new IntArrayList();
							common.addAll(arasuCliquesAsColIndxs.get(j));
							common.retainAll(clique1);
							if (common.isEmpty())
								continue;
							List<Region> lhsRegions = cliqueIdxtoPList.get(i).getAll();
							List<Region> rhsRegions = cliqueIdxtoPList.get(j).getAll();

							for (int idx = 0; idx < lhsRegions.size(); idx++) {

//							Region lhsRegion = lhsVarValue.variable;
								Region lhsRegion = lhsRegions.get(idx);

								boolean check = false;

								for (Region rhsRegion : rhsRegions) {
									for (int k = 0; k < lhsRegion.size(); k++) {// iterate over each clause
										BucketStructure lhsBS = lhsRegion.at(k);
										// int totmatch = 0;
										for (int l = 0; l < rhsRegion.size(); l++) {
											BucketStructure rhsBS = rhsRegion.at(l);
											if (isCompatibleBS(common, lhsBS, common, rhsBS)) {
												check = true;
//												System.out.println("i=" + i);
//												System.out.println("j=" + j);
												if (!isFullOverlap(common, lhsRegion, common, rhsRegion)) {
													throw new RuntimeException("partially overlapping regions!!!");
												}
												break;
											}
										}
										if (check)
											break;
									}

								}
								if (!check)
									throw new RuntimeException("Not consisitent!");
							}

						}
					}

					List<List<IntList>> cliqueIdxtoPSimplifiedListNew = new ArrayList<>();

					for (int i = 0; i < cliqueCount; i++) {

						List<IntList> PSimplifiedListNew = new ArrayList<>();
						List<Region> regionList = cliqueIdxtoPList.get(i).getAll();

						List<Region> cRegions = cliqueToCRegions.get(i);
//					System.out.println("Clique " + i + "Cregion size" + cRegions.size());
						for (int j = 0; j < regionList.size(); j++) {
							IntList listNew = new IntArrayList();
							Region reg = regionList.get(j);
							for (int k = 0; k < cRegions.size(); k++) {
								Region cReg = cRegions.get(k);
								if (cReg.contains(reg))
									listNew.add(k);
							}

							PSimplifiedListNew.add(listNew);

						}
						cliqueIdxtoPSimplifiedListNew.add(PSimplifiedListNew);

					}
					cliqueIdxtoPSimplifiedList = cliqueIdxtoPSimplifiedListNew;
					List<List<Region>> cliqueIdxtoPListCompTemp = new ArrayList<>();
					for (int i = 0; i < cliqueCount; i++) {

						List<Region> PListComp = new ArrayList<>();
						IntList colIndxs = arasuCliquesAsColIndxs.get(i);

						for (int idx = 0; idx < cliqueIdxtoPList.get(i).size(); idx++) {
							Region reg = cliqueIdxtoPList.get(i).getAll().get(idx);

							PListComp.add(reg);
						}
						cliqueIdxtoPListCompTemp.add(PListComp);

					}

					for (int i = 0; i < cliqueCount; i++) {
						varcountDR += cliqueIdxtoPList.get(i).getAll().size();
						List<Region> regionList = cliqueIdxtoPList.get(i).getAll();
						isUniverseCheck(regionList);
						// check if overlapping regions
						for (int j = 0; j < regionList.size(); j++) {
							for (int k = j + 1; k < regionList.size(); k++)
								if (!(regionList.get(j).intersection(regionList.get(k)).isEmpty()))
									throw new RuntimeException("Overlapping regions!!!");
						}
					}
				}

				DebugHelper
						.printInfo("Number of variables after double reduction after making symmetric " + varcountDR);
//				System.out.println("No. of region broken into more than 2 symmetric regions= " + numOfSymmetricMore);
			}
			System.out.println();
			regionPartitioning.displayTimeAndDispose();

			// ---------------------------------Region Partitioning +Symmetric
			// refinement---------------
			System.out.println();
			StopWatch psdSW = new StopWatch("Projection Subspace Division time for " + viewname);

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
			// finished----------------------------------------

			// -----------------------projection variable formution
			// starts-------------------------------

//			StopWatch variableFormulationSW = new StopWatch("Variable formulation" + viewname);

			List<Map<List<String>, List<ProjectionVariable>>> cliqueToGroupkeyToProjectionVariables = new ArrayList<>();
			List<Map<List<String>, Map<Integer, List<Integer>>>> cliqueToGroupkeyToRegionToProjectionVariables = new ArrayList<>();// same

			List<Map<List<String>, List<ProjectionVariableOptimized>>> cliqueToGroupkeyToProjectionVariablesOptimized = new ArrayList<>();
			List<Map<List<String>, Map<Integer, List<Integer>>>> cliqueToGroupkeyToRegionToProjectionVariablesOptimzed = new ArrayList<>();// same//
																																			// as
			if (wantPowerset) { // cliqueQMap
				for (int i = 0; i < cliqueCount; i++) {
					Map<List<String>, List<ProjectionVariable>> groupkeyToProjectionVariables = new HashMap<>();
					Map<List<String>, Map<Integer, List<Integer>>> groupkeyToRegionToProjectionVariables = new HashMap<>();
					Map<List<String>, List<Integer>> groupkeyToregionIdx = cliqueToGroupkeyToRegionIdx.get(i);
					for (List<String> groupkey : groupkeyToregionIdx.keySet()) {
						Map<Integer, List<Integer>> regionToProjectionVariables = new HashMap<>();

						List<Integer> regionsIdx = groupkeyToregionIdx.get(groupkey);
						List<ProjectionVariable> currProjVariables = getProjectionRegions(regionsIdx, groupkey, i);
						System.out.println("For groupkeys " + groupkey + " count=" + currProjVariables.size());
						// List<ProjectionVariable> currProjVariables = new ArrayList<>();
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
//

			// as
			int regionsCount = 0;
			if (wantPoset) { // cliqueQMap
				int totalProjectionREgions = 0;
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
								regionsCount++;
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
						totalProjectionREgions += varList.size();
//				System.out.println("For groupKey "+ groupkey+" count="+ varList2.size());
						if (varList.size() > 10000)
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

				DebugHelper.printInfo(
						"\n\nNumber of variables after double reduction before making symmetric " + varcountDRold);
				DebugHelper
						.printInfo("Number of variables after double reduction after making symmetric " + varcountDR);
				System.out.println("Number of unsymm regions" + " =" + numOfUnsymmReg);
				System.out.println("Number of broken regions" + " =" + newRegionsCount);
				System.out.println("Total projection regions=" + totalProjectionREgions);
				System.out.println("Total regions being acted on by projection " + regionsCount);
//				variableFormulationSW.displayTimeAndDispose();
				System.out.println(); 
				psdSW.displayTimeAndDispose();
				// --------------------------------variable formulation
				// finished-------------------------------

				StopWatch postVariableFormulationSW = new StopWatch("Post PSD " + viewname);

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
						// List<List<Integer>> projectionVariablesToProjectedSubRegions = new
						// ArrayList<>();
						// Map<Integer,List<Integer>>firstIntervalToProjVar=new HashMap<>();

						for (int j = 0; j < projectionVariablesOptimized.size(); j++) {
							ProjectionVariableOptimized var = projectionVariablesOptimized.get(j);
							Region reg = var.intersection;
							int firstInterval = reg.at(0).at(0).at(0);
//					if(!firstIntervalToProjVar.containsKey(firstInterval)) {
//						firstIntervalToProjVar.put(firstInterval,new ArrayList<>());
//						
//					}
							columnTofirstIntervalToProjVar.get(colIdx).set(firstInterval,
									columnTofirstIntervalToProjVar.get(colIdx).get(firstInterval) + 1);

//					firstIntervalToProjVar.get(firstInterval).add(j);
						}
						// List<Float>splitPoints=new ArrayList<>();
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
				postVariableFormulationSW.displayTimeAndDispose();
			}

			return formAndSolveLP(consistencyMakerType, consistencyConstraints, conditions, cliqueToGroupkeyToRegionIdx,
					cliqueToGroupkeyConditionToRegionIdx, cliqueToGroupkeyToProjectionVariables,
					cliqueToGroupkeyToRegionToProjectionVariables,
					cliqueToGroupkeyToRegionToProjectionVariablesOptimzed,
					cliqueToGroupkeyToProjectionVariablesOptimized);
		}
		return formAndSolveLP(consistencyMakerType, consistencyConstraints, conditions, null, null, null, null, null,
				null);

	}

	private boolean comparePList(Partition partTemp, Partition partition) {
		for (Region reg1 : partTemp.getAll()) {
			boolean found = false;
			for (Region reg2 : partition.getAll()) {
				if (reg1.areEqual(reg2)) {
					found = true;
					break;
				}
			}
			if (!found)
				return false;
		}
		return true;
	}

	private Region getIntervalRegion(Region reg, IntList common) {
		// TODO Auto-generated method stub
		Region interval = reg.getDeepCopy();
		for (BucketStructure bs : interval.getAll()) {
			for (int i = 0; i < allTrueBS.size(); i++) {
				if (common.contains(i))
					continue;
				Bucket b = new Bucket();
				for (int j = 0; j < allTrueBS.get(i).length; j++) {
					b.add(j);
				}
				bs.at(i).clear();
				bs.at(i).addAll(b);

			}

		}
//		Region finalInterval=new Region();
//		for()
		return interval;
	}

	private List<RegionF> getIntervals(Region region, int n, List<String> groupkey) {
		List<RegionF> regions = new ArrayList<>();
		for (int i = 0; i < n; i++)
			regions.add(new RegionF());
//		Bucket b1=new Bucket();
//		b1.add(1);
//		b1.add(2);
//		b1.add(3);
//		
//		BucketStructure bs1=new BucketStructure();
//		bs1.add(b1);
//		bs1.add(b1);
//		Region region=new Region();
//		region.add(bs1);
//		
//		Bucket b2=new Bucket();
//		b2.add(4);
//		b2.add(5);
//		b2.add(6);
//		
//		BucketStructure bs2=new BucketStructure();
//		bs2.add(b2);
//		bs2.add(b2);
//		//Region region2=new Region();
//		region.add(bs2);

		for (BucketStructure bs : region.getAll()) {
			// List<BucketStructureF>bucketStructuresFloat=new ArrayList<>();//n bucket
			// structures one for each interval
			Bucket b = bs.at(0);// making intervals on the first dimension
			List<BucketF> buckets = new ArrayList<>();// n buckets, one for each interval
			// initializing buckets
			for (int i = 0; i < n; i++) {
				buckets.add(new BucketF());// one bucket for each interval
			}
			for (int i = 0; i < b.size(); i++) {// for every ibterval(split point in bucket
				List<Double> cuts = divide(b.at(i), n, sortedViewColumns.indexOf(groupkey.get(0)));// returns n
																									// intervals for the
																									// bucketinterval
				for (int j = 0; j < n; j++) {
					buckets.get(j).add(cuts.get(j));
				}

				// getColumnsIdx(groupkey)
			}
//			for(int i=0;i<n;i++) {
//				bucketStructuresFloat.add(new BucketStructureF());//creating a bucketstructurew for each n interval
//				for(int j=0;j<bs.size();j++)
//					bucketStructuresFloat.get(i).add(new BucketF());
//				
//			}
			for (int j = 0; j < n; j++) {// popoulate each bs
				// bucketStructuresFloat.add(new BucketStructureF());
				BucketStructureF bsF = new BucketStructureF();
				for (int i = 0; i < bs.size(); i++) {
					if (i == 0) {
						bsF.add(buckets.get(j));
					} else {
						BucketF bucketF = new BucketF();
						for (int k = 0; k < bs.at(i).size(); k++) {
							// Float x=((float)(bs.at(i).at(k)));
							bucketF.add(((double) (bs.at(i).at(k))));
						}
						bsF.add(bucketF);
					}
				}
				regions.get(j).add(bsF);
			}
		}
		return regions;
	}

	private List<Double> divide(int at, int n, int colIdx) {
		// TODO Auto-generated method stub
		List<Double> ret = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			ret.add(at + (double) i / n);
			if (i != 0)
				bucketSplitPoints.get(colIdx).add(at + (double) i / n);
		}

		return ret;
	}

	private List<Region> makeSymmetricTemp(Region region) {
		// method to make regions symmetric along a groupkey
		List<Region> symmetricRegions = new ArrayList<>();
		Map<BucketStructure, Region> groupKeyBucketsToRegion = new HashMap<>();
		for (BucketStructure bs : region.getAll()) {
			BucketStructure groupKeyBuckets = getBucketsForGroupKeyTemp(bs, 0);
//			System.out.println(groupKeyBuckets);
//			System.out.println("Done");
			boolean check = false;
			for (BucketStructure bsItr : groupKeyBucketsToRegion.keySet()) {
				if (bsItr.equals(groupKeyBuckets)) {
					groupKeyBucketsToRegion.get(bsItr).add(bs);
					check = true;
				}
			}
			if (!check) {
				groupKeyBucketsToRegion.put(groupKeyBuckets, new Region());
				groupKeyBucketsToRegion.get(groupKeyBuckets).add(bs);
			}
//			if (!groupKeyBucketsToRegion.containsKey(groupKeyBuckets)) {
//				groupKeyBucketsToRegion.put(groupKeyBuckets, new Region());
//				System.out.println("Not present");
//			}
//			groupKeyBucketsToRegion.get(groupKeyBuckets).add(bs);

		}
		if (groupKeyBucketsToRegion.size() > 2)
			numOfSymmetricMore++;
		for (Region reg : groupKeyBucketsToRegion.values()) {
//			List<List<Region>> cliqueIdxtoPListCompTemp = new ArrayList<>();
//
//			IntList colIndxs = arasuCliquesAsColIndxs.get(i);
			// System.out.println(getTruncatedRegion(reg, colIndxs));

			symmetricRegions.add(reg);
		}
		return symmetricRegions;
	}

	private void isUniverseCheck(List<Region> regions) {
		// universe minus all regions should become empty
		Region universe = new Region();
		BucketStructure subConditionBS = new BucketStructure();
		for (int j = 0; j < allTrueBS.size(); j++) {
			Bucket bucket = new Bucket();
			for (int k = 0; k < allTrueBS.get(j).length; k++) {
				if (allTrueBS.get(j)[k]) {
					bucket.add(k);
				}
			}
			subConditionBS.add(bucket);
		}
		universe.add(subConditionBS);

		for (Region region : regions) {
			// DebugHelper.printDebug("Minus operation " + universe.size() + " and " +
			// region.size());
			universe = universe.minus(region);
		}

		if (!universe.isEmpty())
			throw new RuntimeException("Expected empty region but found non-empty");
	}

	private List<Region> makeSymmetric(Region region, List<String> groupKey, int i) {
		// method to make regions symmetric along a groupkey
		if (region.size() > 1)
			System.out.println("Size greater than 2");
		List<Region> symmetricRegions = new ArrayList<>();
		Map<BucketStructure, Region> groupKeyBucketsToRegion = new HashMap<>();
//		BucketStructure groupKeyBucketsInit = getBucketsForGroupKey(region.at(0), groupKey);
//		for (BucketStructure bs : region.getAll()) {
//			BucketStructure groupKeyBuckets = getBucketsForGroupKey(bs, groupKey);
//			if(!groupKeyBuckets.equals(groupKeyBucketsInit))
//				System.out.println("unsymm");
//		}
		for (BucketStructure bs : region.getAll()) {
			BucketStructure groupKeyBuckets = getBucketsForGroupKey(bs, groupKey);
//			System.out.println(groupKeyBuckets);
//			System.out.println("Done");
			boolean check = false;
			for (BucketStructure bsItr : groupKeyBucketsToRegion.keySet()) {
				if (bsItr.equals(groupKeyBuckets)) {
					groupKeyBucketsToRegion.get(bsItr).add(bs);
					check = true;
				}
			}
			if (!check) {
				groupKeyBucketsToRegion.put(groupKeyBuckets, new Region());
				groupKeyBucketsToRegion.get(groupKeyBuckets).add(bs);
			}
//			if (!groupKeyBucketsToRegion.containsKey(groupKeyBuckets)) {
//				groupKeyBucketsToRegion.put(groupKeyBuckets, new Region());
//				System.out.println("Not present");
//			}
//			groupKeyBucketsToRegion.get(groupKeyBuckets).add(bs);

		}
		if (groupKeyBucketsToRegion.size() > 2)
			numOfSymmetricMore++;
		for (Region reg : groupKeyBucketsToRegion.values()) {
//			List<List<Region>> cliqueIdxtoPListCompTemp = new ArrayList<>();
//
//			IntList colIndxs = arasuCliquesAsColIndxs.get(i);
			// System.out.println(getTruncatedRegion(reg, colIndxs));

			symmetricRegions.add(reg);
		}
		return symmetricRegions;
	}

	private BucketStructure getBucketsForGroupKeyTemp(BucketStructure bs, int i) {
		List<Bucket> buckets = new ArrayList<>();
		List<Integer> idx = new ArrayList<>();

		BucketStructure bsRet = new BucketStructure();

		bsRet.add(bs.at(i));
		return bsRet;
	}

	private BucketStructure getBucketsForGroupKey(BucketStructure bs, List<String> groupKey) {
		List<Integer> idx = new ArrayList<>();

		for (String column : groupKey) {
			idx.add(sortedViewColumns.indexOf(column));
		}
		BucketStructure bsRet = new BucketStructure();
		for (Integer id : idx)
			bsRet.add(bs.at(id));
		return bsRet;
	}

	private List<ProjectionVariableOptimized> getProjectionVariablesOptimized(
			UndirectedGraph<String, DefaultEdge> projectionConditionGraph) {
		List<String> nodes = new ArrayList<>(projectionConditionGraph.vertexSet());
		Collections.sort(nodes);
		List<ProjectionVariableOptimized> varList = new ArrayList<>();

		List<String> visited = new ArrayList<>();
		for (String node : nodes) {
//			Region currNodeRegion = cliqueIdxtoPList.get(cliqueIndex).getAll().get(Integer.parseInt(node));
//			Region currRegionProjection = projectRegion(currNodeRegion, groupkey);
			List<String> intersectionNeighbourVisited = Graphs.neighborListOf(projectionConditionGraph, node);

			intersectionNeighbourVisited.retainAll(visited);
			ProjectionVariableOptimized var = new ProjectionVariableOptimized();
			var.positive.add(Integer.parseInt(node));
			// add the visited region in negative region only when it has an intersection
			for (String neg : intersectionNeighbourVisited) {
				// Region currNeg =
				// cliqueIdxtoPList.get(cliqueIndex).getAll().get(Integer.parseInt(neg));
//				if(currRegionProjection.intersection(projectRegion(currNeg,groupkey)).isEmpty())
//					continue;
				var.negative.add(Integer.parseInt(neg));
			}
			// var.addStringNegative(intersection);
			// var.intersection = currRegionProjection;
			varList.addAll(getProjectionVariablesOptimizedHelper(var, new ArrayList<>(), projectionConditionGraph));
			visited.add(node);

		}
		return varList;

	}

	private List<ProjectionVariableOptimized> getProjectionVariablesOptimizedHelper(ProjectionVariableOptimized var,
			List<String> visited, UndirectedGraph<String, DefaultEdge> projectionConditionGraph) {
		List<ProjectionVariableOptimized> ret = new ArrayList<>();

		String vertex = null;

		for (int i = 0; i < var.positive.size(); i++) {
			String currVertex = (var.positive.get(i)).toString();
			if (!visited.contains(currVertex)) {
				vertex = currVertex;
				break;
			}
		}
		if (vertex == null) {
			ret.add(var);

			// System.out.println(countVarOptimized++);
			return ret;
		}
		List<String> neighbours = Graphs.neighborListOf(projectionConditionGraph, vertex);
		neighbours.removeAll(visited);
		for (Integer neg : var.negative)
			neighbours.remove(neg.toString());
		for (Integer pos : var.positive)
			neighbours.remove(pos.toString());

		List<ProjectionVariableOptimized> powerSet = getPowerSetOptimized(var, neighbours);

		for (ProjectionVariableOptimized set : powerSet) {
			List<String> currVisited = new ArrayList<>();
			currVisited.addAll(visited);
			currVisited.add(vertex);
			ret.addAll(getProjectionVariablesOptimizedHelper(set, currVisited, projectionConditionGraph));
		}
		// TODO Auto-generated method stub
		return ret;
	}

	private List<ProjectionVariableOptimized> getPowerSetOptimized(ProjectionVariableOptimized var,
			List<String> neighbours) {
		// TODO Auto-generated method stub
//		if(var.positive.contains(0)&&var.positive.contains(1))
//		{
//			System.out.print("hi");
//		}
		List<ProjectionVariableOptimized> ret = new ArrayList<>();
		if (neighbours.isEmpty()) {
			ret.add(var);
			return ret;
		}
		List<Integer> projectionColumnsIdx = new ArrayList<>();

		// converting col names to index
//		for (String projectionColumn : groupkey) {
//			projectionColumnsIdx.add(sortedViewColumns.indexOf(projectionColumn));
//		}

		// check if var1 is possible
//		Region currRegion = cliqueIdxtoPList.get(cliqueIndex).getAll().get(Integer.parseInt(neighbours.get(0)));
//		Region projectedRegion = projectRegion(currRegion, groupkey);

		// if there is intersection, only then the new variables must be formed
//		if (!projectedRegion.intersection(var.intersection).isEmpty()) {

		ProjectionVariableOptimized var1 = new ProjectionVariableOptimized();

		var1.positive.addAll(var.positive);
		var1.negative.addAll(var.negative);

		ProjectionVariableOptimized var2 = new ProjectionVariableOptimized();
		var2.positive.addAll(var.positive);
		var2.negative.addAll(var.negative);

		var1.addPositive(Integer.parseInt(neighbours.get(0)));
		// var1.intersection = projectedRegion;bug
		// var1.intersection=projectedRegion.intersection(var.intersection);

		var2.addNegative(Integer.parseInt(neighbours.get(0)));
		// var2.intersection = var.intersection;

		ret.addAll(getPowerSetOptimized(var1, neighbours.subList(1, neighbours.size())));
		ret.addAll(getPowerSetOptimized(var2, neighbours.subList(1, neighbours.size())));
//		} else {
//			ret.addAll(getPowerSetOptimized(var, neighbours.subList(1, neighbours.size()), groupkey, cliqueIndex));
//		}
		return ret;
	}

	private List<ProjectionVariableOptimized> getProjectionVariablesOptimized(int cliqueIndex,
			UndirectedGraph<String, DefaultEdge> projectionConditionGraph, List<String> groupkey) {
		List<String> nodes = new ArrayList<>(projectionConditionGraph.vertexSet());
		Collections.sort(nodes);
		List<ProjectionVariableOptimized> varList = new ArrayList<>();

		List<String> visited = new ArrayList<>();
		for (String node : nodes) {
			Region currNodeRegion = cliqueIdxtoPList.get(cliqueIndex).getAll().get(Integer.parseInt(node));
			Region currRegionProjection = projectRegion(currNodeRegion, groupkey);
			List<String> intersectionNeighbourVisited = Graphs.neighborListOf(projectionConditionGraph, node);

			intersectionNeighbourVisited.retainAll(visited);
			ProjectionVariableOptimized var = new ProjectionVariableOptimized();
			var.positive.add(Integer.parseInt(node));
			// add the visited region in negative region only when it has an intersection
			for (String neg : intersectionNeighbourVisited) {
				Region currNeg = cliqueIdxtoPList.get(cliqueIndex).getAll().get(Integer.parseInt(neg));
				if (currRegionProjection.intersection(projectRegion(currNeg, groupkey)).isEmpty())
					continue;
				var.negative.add(Integer.parseInt(neg));
			}
			// var.addStringNegative(intersection);
			var.intersection = currRegionProjection;
//			varList.addAll(getProjectionVariablesOptimizedHelper(var, new ArrayList<>(), projectionConditionGraph,
//					groupkey, cliqueIndex));
			varList.addAll(getProjectionVariablesOptimizedHelper(var, visited, projectionConditionGraph, groupkey,
					cliqueIndex));
			visited.add(node);

		}
		return varList;

	}

	private List<ProjectionVariableOptimized> getProjectionVariablesOptimizedHelper(ProjectionVariableOptimized var,
			List<String> visited, UndirectedGraph<String, DefaultEdge> projectionConditionGraph, List<String> groupkey,
			int cliqueIndex) {
		List<ProjectionVariableOptimized> ret = new ArrayList<>();

		String vertex = null;

		for (int i = 0; i < var.positive.size(); i++) {
			String currVertex = (var.positive.get(i)).toString();
			if (!visited.contains(currVertex)) {
				vertex = currVertex;
				break;
			}
		}
		if (vertex == null) {
			Set<String> tempNeighbours = new HashSet<>();
			for (Integer pos : var.positive)
				tempNeighbours.addAll(Graphs.neighborListOf(projectionConditionGraph, pos.toString()));
//			tempNeighbours.removeAll(var.positive);
//			tempNeighbours.removeAll(var.negative);
			for (Integer neg : var.negative)
				tempNeighbours.remove(neg.toString());
			for (Integer pos : var.positive)
				tempNeighbours.remove(pos.toString());

			for (String neg : tempNeighbours) {
				var.negative.add(Integer.parseInt(neg));
			}
			ret.add(var);

			// System.out.println(countVarOptimized++);
			return ret;
		}
		List<String> neighbours = Graphs.neighborListOf(projectionConditionGraph, vertex);
		neighbours.removeAll(visited);
		for (Integer neg : var.negative)
			neighbours.remove(neg.toString());
		for (Integer pos : var.positive)
			neighbours.remove(pos.toString());

		List<ProjectionVariableOptimized> powerSet = getPowerSetOptimized(var, neighbours, groupkey, cliqueIndex);

		for (ProjectionVariableOptimized set : powerSet) {
			List<String> currVisited = new ArrayList<>();
			currVisited.addAll(visited);
			currVisited.add(vertex);
			ret.addAll(getProjectionVariablesOptimizedHelper(set, currVisited, projectionConditionGraph, groupkey,
					cliqueIndex));
		}
		// TODO Auto-generated method stub
		return ret;
	}

	private List<ProjectionVariableOptimized> getPowerSetOptimized(ProjectionVariableOptimized var,
			List<String> neighbours, List<String> groupkey, int cliqueIndex) {
		// TODO Auto-generated method stub
//		if(var.positive.contains(0)&&var.positive.contains(1))
//		{
//			System.out.print("hi");
//		}
		List<ProjectionVariableOptimized> ret = new ArrayList<>();
		if (neighbours.isEmpty()) {
			ret.add(var);
			return ret;
		}
		List<Integer> projectionColumnsIdx = new ArrayList<>();

		// converting col names to index
		for (String projectionColumn : groupkey) {
			projectionColumnsIdx.add(sortedViewColumns.indexOf(projectionColumn));
		}

		// check if var1 is possible
		Region currRegion = cliqueIdxtoPList.get(cliqueIndex).getAll().get(Integer.parseInt(neighbours.get(0)));
		Region projectedRegion = projectRegion(currRegion, groupkey);

		// if there is intersection, only then the new variables must be formed
		if (!projectedRegion.intersection(var.intersection).isEmpty()) {

			ProjectionVariableOptimized var1 = new ProjectionVariableOptimized();

			var1.positive.addAll(var.positive);
			var1.negative.addAll(var.negative);

			ProjectionVariableOptimized var2 = new ProjectionVariableOptimized();
			var2.positive.addAll(var.positive);
			var2.negative.addAll(var.negative);

			var1.addPositive(Integer.parseInt(neighbours.get(0)));
			// var1.intersection = projectedRegion;bug
			var1.intersection = projectedRegion.intersection(var.intersection);

			var2.addNegative(Integer.parseInt(neighbours.get(0)));
			var2.intersection = var.intersection;

			ret.addAll(getPowerSetOptimized(var1, neighbours.subList(1, neighbours.size()), groupkey, cliqueIndex));
			ret.addAll(getPowerSetOptimized(var2, neighbours.subList(1, neighbours.size()), groupkey, cliqueIndex));
		} else {
			ret.addAll(getPowerSetOptimized(var, neighbours.subList(1, neighbours.size()), groupkey, cliqueIndex));
		}
		return ret;
	}

	public List<LinkedList<VariableValuePair>> formAndSolveLP(ConsistencyMakerType consistencyMakerType,
			FormalCondition[] consistencyConstraints, List<FormalCondition> conditions,
			List<Map<List<String>, List<Integer>>> cliqueToGroupkeyToRegionIdx,
			List<Map<List<String>, Map<FormalConditionAggregate, List<Integer>>>> cliqueToGroupkeyConditionToRegionIdx,
			List<Map<List<String>, List<ProjectionVariable>>> cliqueToGroupkeyToProjectionVariables,
			List<Map<List<String>, Map<Integer, List<Integer>>>> cliqueToGroupkeyToRegionToProjectionVariables,
			List<Map<List<String>, Map<Integer, List<Integer>>>> cliqueToGroupkeyToRegionToProjectionVariablesOptimzed,
			List<Map<List<String>, List<ProjectionVariableOptimized>>> cliqueToGroupkeyToProjectionVariablesOptimized) {

		// Added by tarun starts

		// added by tarun ends

		/////////////// Start dk

		// Map<String, String> contextmaptest = new HashMap<>();
		// contextmaptest.put("model", "true");
		// contextmaptest.put("unsat_core", "true");
		// Context ctxtest = new Context(contextmaptest);
		//
		// Optimize osolver = ctxtest.mkOptimize();
		// IntExpr x = ctxtest.mkIntConst("x");
		// IntExpr y = ctxtest.mkIntConst("y");
		// ArithExpr arithexpr = ctxtest.mkAdd(x, y);
		// osolver.Add(ctxtest.mkGt(arithexpr, ctxtest.mkInt(10)));
		// osolver.Add(ctxtest.mkLt(arithexpr, ctxtest.mkInt(20)));
		//
		// osolver.MkMaximize(arithexpr);
		//
		// osolver.Check();
		//
		// Model modeltest = osolver.getModel();
		// System.out.println(modeltest.evaluate(x, true) + " : " +
		// modeltest.evaluate(y, true));
		///////////////// End dk

		StopWatch onlyFormationSW = new StopWatch("LP-OnlyFormation" + viewname);

		Map<String, String> contextmap = new HashMap<>();
		contextmap.put("model", "true");
		contextmap.put("unsat_core", "true");
		Context ctx = new Context(contextmap);

		// Solver solver = ctx.mkSolver();
		Optimize solver = ctx.mkOptimize();

		// adding expression for optimization function -- Anupam
		// start
		// ArithExpr exp_final = ctx.mkIntConst("");
		// stop

		List<List<List<IntExpr>>> solverConstraintsRequiredForConsistency = new ArrayList<>();
		List<HashMap<String, ProjectionStuffInColumn>> cliqueWColumnWProjectionStuff = new ArrayList<>();

		// Create lp variables for cardinality constraints
		for (int cliqueIndex = 0; cliqueIndex < cliqueCount; cliqueIndex++) {

			LongList bvalues = cliqueIdxtoConditionBValuesList.get(cliqueIndex);
			Partition partition = cliqueIdxtoPList.get(cliqueIndex); // Partition is a list of regions corresponding to
																		// below labels
			List<IntList> conditionIdxsList = cliqueIdxtoPSimplifiedList.get(cliqueIndex); // Getting label list for
//			System.out.println(conditionIdxsList); // this clique

			// Map<Integer,List<Integer>>
			// regionToProjectionVariables=regionToProjectionVariablesList.get(cliqueIndex);

			HashMap<Integer, Integer> indexKeeper = null;
			int solverConstraintSize;

			if (consistencyMakerType == ConsistencyMakerType.CONSISTENCYFILTERS) {
				if (cliqueCount > 1) {
					indexKeeper = mappedIndexOfConsistencyConstraint.get(cliqueIndex);
					solverConstraintSize = bvalues.size() + indexKeeper.size();
				} else
					solverConstraintSize = bvalues.size();
			} else {
				indexKeeper = new HashMap<>();
				solverConstraintSize = bvalues.size() + cliqueWiseTotalSingleSplitPointRegions.get(cliqueIndex);
			}

			List<List<IntExpr>> solverConstraints = new ArrayList<>(solverConstraintSize);
			for (int j = 0; j < solverConstraintSize; j++) {
				solverConstraints.add(new ArrayList<>());
			}
//			System.out.println("solver size=" + solverConstraintSize);

			// Adding projection nonnegativity
//			      for (int j=0;j<cliqueIdxToProjectionRegions.get(cliqueIndex).size();j++) {
//			    	  String varname = "y" + cliqueIndex + "_" + j;
//			          solverStats.solverVarCount++;
//			          
//			          solver.Add(ctx.mkGe(ctx.mkIntConst(varname), ctx.mkInt(0)));
//			      }

			for (int blockIndex = 0; blockIndex < partition.size(); blockIndex++) {
				String varname = "var" + cliqueIndex + "_" + blockIndex;
				solverStats.solverVarCount++;

				// adding expression for computing l2 norm -- Anupam
				// start
				// IntExpr expr = ctx.mkIntConst(varname);
				// L1 norm minimization
				// exp_final = ctx.mkAdd(exp_final, expr);
				// L2 norm minimization
				// exp_final = ctx.mkAdd(exp_final, ctx.mkMul(expr, expr));
				// stop

				// Adding non-negativity constraints
				solver.Add(ctx.mkGe(ctx.mkIntConst(varname), ctx.mkInt(0)));

				// adding projection variable greater than 0

//			          
//			          if(regionToProjectionVariables.containsKey(blockIndex)) {
//			        	  List<IntExpr>projectionExpr=new ArrayList<>();
//			        	  for (Integer projVarIndx:regionToProjectionVariables.get(blockIndex)) {
//			        		  projectionExpr.add(ctx.mkIntConst("y" + cliqueIndex + "_"+projVarIndx));
//			        	  }
//			        	  solver.Add(ctx.mkGe(ctx.mkMul(ctx.mkInt(relationCardinality),ctx.mkAdd(projectionExpr.toArray(new ArithExpr[projectionExpr.size()]))), ctx.mkIntConst(varname)));
//			        	  solver.Add(ctx.mkLe(ctx.mkAdd(projectionExpr.toArray(new ArithExpr[projectionExpr.size()])), ctx.mkIntConst(varname)));
//			          } 
//			          

				// Adds the region to all the constraints it belongs to
				for (IntIterator iter = conditionIdxsList.get(blockIndex).iterator(); iter.hasNext();) {
					int k = iter.nextInt();

					solverConstraints.get(k).add(ctx.mkIntConst(varname));
				}

			}
			// Adding normal constraints
			for (int conditionIndex = 0; conditionIndex < bvalues.size(); conditionIndex++) {
				long outputCardinality = bvalues.getLong(conditionIndex);
				List<IntExpr> addList = solverConstraints.get(conditionIndex);
				solver.Add(ctx.mkEq(ctx.mkAdd(addList.toArray(new ArithExpr[addList.size()])),
						ctx.mkInt(outputCardinality)));
				solverStats.solverConstraintCount++;
			}

			// List<Map<List<String>, List<Integer>>> cliqueToGroupkeyToRegionIdx,
			// List<Map<List<String>, Map<FormalConditionAggregate, List<Integer>>>>
			// cliqueToGroupkeyConditionToRegionIdx, List<Map<List<String>,
			// List<ProjectionVariable>>> cliqueToGroupkeyToProjectionVariables,
			// List<Map<List<String>, Map<Integer, List<Integer>>>>
			// cliqueToGroupkeyToRegionToProjectionVariables

//			      for(int i=0;i<cliqueCount;i++) {
			if (!ignoreProjection) {
				if (!projectionOptimized) {
					Map<List<String>, List<ProjectionVariable>> groupkeyToProjectionVariables = cliqueToGroupkeyToProjectionVariables
							.get(cliqueIndex);
					for (List<String> groupkey : groupkeyToProjectionVariables.keySet()) {
						String groupkeyStr = toStringFunc(groupkey);
						List<ProjectionVariable> projectionVariables = groupkeyToProjectionVariables.get(groupkey);
						for (int j = 0; j < projectionVariables.size(); j++) {
							String varname = "y" + cliqueIndex + "_" + groupkeyStr + "_" + j;
							solver.Add(ctx.mkGe(ctx.mkIntConst(varname), ctx.mkInt(0)));

						}
					}
//			      }

					// check the correctness.

					Map<List<String>, Map<Integer, List<Integer>>> groupkeyToRegionToProjectionVariables = cliqueToGroupkeyToRegionToProjectionVariables
							.get(cliqueIndex);

					for (List<String> groupkey : groupkeyToRegionToProjectionVariables.keySet()) {
						List<ProjectionVariable> projectionVariablesOptimized = groupkeyToProjectionVariables
								.get(groupkey);
						Map<Integer, List<Integer>> regionToProjectionVariables = groupkeyToRegionToProjectionVariables
								.get(groupkey);
						for (Integer regionIdx : regionToProjectionVariables.keySet()) {
							String varname = "var" + cliqueIndex + "_" + regionIdx;
							List<IntExpr> projectionExpr = new ArrayList<>();
							for (Integer projVarIndx : regionToProjectionVariables.get(regionIdx)) {
								projectionExpr.add(ctx.mkIntConst("y" + cliqueIndex + "_" + projVarIndx));
							}

							solver.Add(
									ctx.mkGe(
											ctx.mkMul(ctx.mkInt(relationCardinality),
													ctx.mkAdd(projectionExpr
															.toArray(new ArithExpr[projectionExpr.size()]))),
											ctx.mkIntConst(varname)));
							solver.Add(ctx.mkLe(ctx.mkAdd(projectionExpr.toArray(new ArithExpr[projectionExpr.size()])),
									ctx.mkIntConst(varname)));
						}
					}
					Map<List<String>, Map<FormalConditionAggregate, List<Integer>>> groupkeyConditionToRegionIdx = cliqueToGroupkeyConditionToRegionIdx
							.get(cliqueIndex);
					for (List<String> groupkey : groupkeyConditionToRegionIdx.keySet()) {
						String groupkeyStr = toStringFunc(groupkey);
						Map<Integer, List<Integer>> regionsToProjectionVariables = cliqueToGroupkeyToRegionToProjectionVariables
								.get(cliqueIndex).get(groupkey);
						Map<FormalConditionAggregate, List<Integer>> conditionToRegionIdx = groupkeyConditionToRegionIdx
								.get(groupkey);
						for (FormalConditionAggregate condition : conditionToRegionIdx.keySet()) {
							List<IntExpr> projectionExpr = new ArrayList<>();
							List<Integer> regionsIdx = conditionToRegionIdx.get(condition);
							Set<Integer> projectionVariablesIdx = new HashSet<>();
							for (Integer regionIdx : regionsIdx) {
								projectionVariablesIdx.addAll(regionsToProjectionVariables.get(regionIdx));
							}

							for (Integer projectionVariableIdx : projectionVariablesIdx)
								projectionExpr.add(ctx.mkIntConst(
										"y" + cliqueIndex + "_" + groupkeyStr + "_" + projectionVariableIdx));
							solver.Add(ctx.mkEq(ctx.mkAdd(projectionExpr.toArray(new ArithExpr[projectionExpr.size()])),
									ctx.mkInt(condition.getProjectionCardinality())));
						}
					}

					// Map<List<String>, Map<Integer,
					// List<Integer>>>groupkeyToRegionToProjectionVariables=cliqueToGroupkeyToRegionToProjectionVariables.get(cliqueIndex);
//				List<List<String>> groupkeys = new ArrayList<>();
//				groupkeys.addAll(groupkeyToRegionToProjectionVariables.keySet());// get all the groupkeys
//
//				for (int j = 0; j < groupkeys.size(); j++) {
//					List<String> groupkey1 = groupkeys.get(cliqueIndex);
//					String groupkeyStr1 = toStringFunc(groupkey1);
//					Map<Integer, List<Integer>> regionToProjectionVariables1 = groupkeyToRegionToProjectionVariables
//							.get(groupkey1);
//
//					for (int k = j + 1; k < groupkeys.size(); k++) {
//						List<String> groupkey2 = groupkeys.get(j);
//						if (!subset(groupkey1, groupkey2))
//							continue;
//						String groupkeyStr2 = toStringFunc(groupkey2);
//						Map<Integer, List<Integer>> regionToProjectionVariables2 = groupkeyToRegionToProjectionVariables
//								.get(groupkey2);
//						for (Integer regionIdx : regionToProjectionVariables1.keySet()) {
//							if (!regionToProjectionVariables2.containsKey(regionIdx))
//								continue;
//							List<Integer> projectionVariablesIdx1 = regionToProjectionVariables1.get(regionIdx);
//							List<Integer> projectionVariablesIdx2 = regionToProjectionVariables2.get(regionIdx);
//							List<IntExpr> projectionExpr1 = new ArrayList<>();
//							List<IntExpr> projectionExpr2 = new ArrayList<>();
//							for (Integer projectionVariableIdx : projectionVariablesIdx1)
//								projectionExpr1.add(ctx.mkIntConst(
//										"y" + cliqueIndex + "_" + groupkeyStr1 + "_" + projectionVariableIdx));
//							for (Integer projectionVariableIdx : projectionVariablesIdx2)
//								projectionExpr2.add(ctx.mkIntConst(
//										"y" + cliqueIndex + "_" + groupkeyStr2 + "_" + projectionVariableIdx));
//							solver.Add(
//									ctx.mkLe(ctx.mkAdd(projectionExpr1.toArray(new ArithExpr[projectionExpr1.size()])),
//											ctx.mkAdd(projectionExpr1.toArray(new ArithExpr[projectionExpr1.size()]))));
//
//						}
//					}
//				}
				} else {

					Map<List<String>, List<ProjectionVariableOptimized>> groupkeyToProjectionVariablesOptimized = cliqueToGroupkeyToProjectionVariablesOptimized
							.get(cliqueIndex);

					for (List<String> groupkey : groupkeyToProjectionVariablesOptimized.keySet()) {
						String groupkeyStr = toStringFunc(groupkey);
						List<ProjectionVariableOptimized> projectionVariablesOptimized = groupkeyToProjectionVariablesOptimized
								.get(groupkey);
						for (int j = 0; j < projectionVariablesOptimized.size(); j++) {
							String varname = "y" + cliqueIndex + "_" + groupkeyStr + "_"
									+ projectionVariablesOptimized.get(j).toString();
							solver.Add(ctx.mkGe(ctx.mkIntConst(varname), ctx.mkInt(0)));

						}
					}

					Map<List<String>, Map<Integer, List<Integer>>> groupkeyToRegionToProjectionVariablesOptimzed = cliqueToGroupkeyToRegionToProjectionVariablesOptimzed
							.get(cliqueIndex);

					for (List<String> groupkey : groupkeyToRegionToProjectionVariablesOptimzed.keySet()) {
						String groupkeyStr = toStringFunc(groupkey);
						List<ProjectionVariableOptimized> projectionVariablesOptimized = groupkeyToProjectionVariablesOptimized
								.get(groupkey);
						Map<Integer, List<Integer>> regionToProjectionVariablesOptimized = groupkeyToRegionToProjectionVariablesOptimzed
								.get(groupkey);
						for (Integer regionIdx : regionToProjectionVariablesOptimized.keySet()) {
							String varname = "var" + cliqueIndex + "_" + regionIdx;
							List<IntExpr> projectionExpr = new ArrayList<>();
							for (Integer projVarIndx : regionToProjectionVariablesOptimized.get(regionIdx)) {
								projectionExpr.add(ctx.mkIntConst("y" + cliqueIndex + "_" + groupkeyStr + "_"
										+ projectionVariablesOptimized.get(projVarIndx).toString()));
							}

//							solver.Add(
//									ctx.mkGe(
//											ctx.mkMul(ctx.mkInt(relationCardinality),
//													ctx.mkAdd(projectionExpr
//															.toArray(new ArithExpr[projectionExpr.size()]))),
//											ctx.mkIntConst(varname)));
							solver.Add(ctx.mkLe(ctx.mkAdd(projectionExpr.toArray(new ArithExpr[projectionExpr.size()])),
									ctx.mkIntConst(varname)));
						}
					}

					Map<List<String>, Map<FormalConditionAggregate, List<Integer>>> groupkeyConditionToRegionIdx = cliqueToGroupkeyConditionToRegionIdx
							.get(cliqueIndex);
					for (List<String> groupkey : groupkeyConditionToRegionIdx.keySet()) {
						String groupkeyStr = toStringFunc(groupkey);
						List<ProjectionVariableOptimized> projectionVariablesOptimized = groupkeyToProjectionVariablesOptimized
								.get(groupkey);

						Map<Integer, List<Integer>> regionsToProjectionVariables = cliqueToGroupkeyToRegionToProjectionVariablesOptimzed
								.get(cliqueIndex).get(groupkey);
						Map<FormalConditionAggregate, List<Integer>> conditionToRegionIdx = groupkeyConditionToRegionIdx
								.get(groupkey);
						for (FormalConditionAggregate condition : conditionToRegionIdx.keySet()) {
							List<IntExpr> projectionExpr = new ArrayList<>();
							List<Integer> regionsIdx = conditionToRegionIdx.get(condition);
							Set<Integer> projectionVariablesIdx = new HashSet<>();
							for (Integer regionIdx : regionsIdx) {
								projectionVariablesIdx.addAll(regionsToProjectionVariables.get(regionIdx));
							}

							// nonOverlappingSanityCheck(projectionVariablesIdx,projectionVariablesOptimized);

							for (Integer projectionVariableIdx : projectionVariablesIdx)
								projectionExpr.add(ctx.mkIntConst("y" + cliqueIndex + "_" + groupkeyStr + "_"
										+ projectionVariablesOptimized.get(projectionVariableIdx).toString()));
							solver.Add(ctx.mkEq(ctx.mkAdd(projectionExpr.toArray(new ArithExpr[projectionExpr.size()])),
									ctx.mkInt(condition.getProjectionCardinality())));
						}
					}

				}
			}

			///////////////// Start dk
			if (cliqueCount > 1 && consistencyMakerType == ConsistencyMakerType.CONSISTENCYFILTERS) {
				List<List<IntExpr>> solverConstraintsToExport = new ArrayList<>(indexKeeper.size());
				for (int j = bvalues.size(); j < solverConstraintSize; j++) {
					solverConstraintsToExport.add(solverConstraints.get(j));
				}
				solverConstraintsToExport.add(solverConstraints.get(bvalues.size() - 1)); // Clique size
				solverConstraintsRequiredForConsistency.add(solverConstraintsToExport);
			}

		}

		///////////////// Start dk
//			  DebugHelper.printInfo("variablesRequiredForProjection: " + projectionVarsIndex);

		// Consistency
		List<ProjectionConsistencyInfoPair> projectionConsistencyInfoPairs = new LinkedList<>();
		int countConsistencyConstraint = 0;
		if (cliqueCount > 1) {// TODO is necessary?
			if (consistencyMakerType == ConsistencyMakerType.CONSISTENCYFILTERS) {

				if (consistencyConstraints.length != 0) {
					for (int c1index = 0; c1index < cliqueCount; c1index++) {
						HashMap<Integer, Integer> c1indexKeeper = mappedIndexOfConsistencyConstraint.get(c1index);
						if (c1indexKeeper.isEmpty())
							continue;
						List<List<IntExpr>> c1solverConstraints = solverConstraintsRequiredForConsistency.get(c1index);
						for (int c2index = c1index + 1; c2index < cliqueCount; c2index++) {
							HashMap<Integer, Integer> c2indexKeeper = mappedIndexOfConsistencyConstraint.get(c2index);
							if (c2indexKeeper.isEmpty())
								continue;
							List<List<IntExpr>> c2solverConstraints = solverConstraintsRequiredForConsistency
									.get(c2index);
							Set<Integer> applicableConsistencyConstraints = new HashSet<>(c1indexKeeper.keySet());
							applicableConsistencyConstraints.retainAll(c2indexKeeper.keySet());
							if (applicableConsistencyConstraints.isEmpty())
								continue;
							List<List<IntExpr>> c1ApplicableSolverConstraints = new ArrayList<>();
							List<List<IntExpr>> c2ApplicableSolverConstraints = new ArrayList<>();
							for (Integer originalConsistencyConstraintIndex : applicableConsistencyConstraints) {
								c1ApplicableSolverConstraints.add(
										c1solverConstraints.get(c1indexKeeper.get(originalConsistencyConstraintIndex)));
								c2ApplicableSolverConstraints.add(
										c2solverConstraints.get(c2indexKeeper.get(originalConsistencyConstraintIndex)));
							}

							c1ApplicableSolverConstraints.add(c1solverConstraints.get(c1solverConstraints.size() - 1));
							c2ApplicableSolverConstraints.add(c2solverConstraints.get(c2solverConstraints.size() - 1));

							HashMap<IntList, MutablePair<List<IntExpr>, List<IntExpr>>> conditionListToPairOfVarList = new HashMap<>();
							addTo_ConditionListToPairOfVarList(c1ApplicableSolverConstraints,
									conditionListToPairOfVarList);
							addTo_ConditionListToPairOfVarList(c2ApplicableSolverConstraints,
									conditionListToPairOfVarList);

							// Set<String> commonCols = new HashSet<>(arasuCliques.get(c1index));
							// commonCols.retainAll(arasuCliques.get(c2index));

							for (MutablePair<List<IntExpr>, List<IntExpr>> pair : conditionListToPairOfVarList
									.values()) {
								List<IntExpr> c1Set = pair.getFirst();
								List<IntExpr> c2Set = pair.getSecond();
								ArithExpr set1expr = ctx.mkAdd(c1Set.toArray(new ArithExpr[c1Set.size()]));
								ArithExpr set2expr = ctx.mkAdd(c2Set.toArray(new ArithExpr[c2Set.size()]));
								solver.Add(ctx.mkEq(set1expr, set2expr));
								countConsistencyConstraint++;

								// 1-D projection
								// collectProjectionConsistencyData(solver,ctx, c1Set, c2Set, c1index, c2index,
								// commonCols, projectionConsistencyInfoPairs, cliqueWColumnWProjectionStuff);
							}
						}
					}
				}
			}
			///////////////// End dk

			else if (consistencyMakerType == ConsistencyMakerType.BRUTEFORCE) {
				for (CliqueIntersectionInfo cliqueIntersectionInfo : cliqueIntersectionInfos) { // Create lp variables
																								// for consistency
																								// constraints

					int i = cliqueIntersectionInfo.i;
					int j = cliqueIntersectionInfo.j;
					IntList intersectingColIndices = cliqueIntersectionInfo.intersectingColIndices;

					Partition partitionI = cliqueIdxtoPList.get(i);
					Partition partitionJ = cliqueIdxtoPList.get(j);

					// Recomputing SplitRegions for every pair of intersecting cliques
					// as the SplitRegions might have become more granular due to
					// some other pair of intersecting cliques having its intersectingColIndices
					// overlapping with this pair's intersectingColIndices
					List<Region> splitRegions = getSplitRegions(partitionI, partitionJ, intersectingColIndices);

					Set<String> commonCols = new HashSet<>(arasuCliques.get(i));
					commonCols.retainAll(arasuCliques.get(j));

					for (Region splitRegion : splitRegions) {
						List<IntExpr> consistencyLHS = new ArrayList<>();
						for (int k = 0; k < partitionI.size(); k++) {
							Region iVar = partitionI.at(k);

							Region truncRegion = getTruncatedRegion(iVar, intersectingColIndices);
							Region truncOverlap = truncRegion.intersection(splitRegion);
							if (!truncOverlap.isEmpty()) {
								String varname = "var" + i + "_" + k;
								consistencyLHS.add(ctx.mkIntConst(varname));
							}
						}

						List<IntExpr> consistencyRHS = new ArrayList<>();
						for (int k = 0; k < partitionJ.size(); k++) {
							Region jVar = partitionJ.at(k);

							Region truncRegion = getTruncatedRegion(jVar, intersectingColIndices);
							Region truncOverlap = truncRegion.intersection(splitRegion);
							if (!truncOverlap.isEmpty()) {
								String varname = "var" + j + "_" + k;
								consistencyRHS.add(ctx.mkIntConst(varname));
							}
						}

						// Adding consistency constraints
						solver.Add(ctx.mkEq(ctx.mkAdd(consistencyLHS.toArray(new ArithExpr[consistencyLHS.size()])),
								ctx.mkAdd(consistencyRHS.toArray(new ArithExpr[consistencyRHS.size()]))));
						solverStats.solverConstraintCount++;
						countConsistencyConstraint++;

						// 1-D projection
						collectProjectionConsistencyData(solver, ctx, consistencyLHS, consistencyRHS, i, j, commonCols,
								projectionConsistencyInfoPairs, cliqueWColumnWProjectionStuff);
					}
				}
			} else {
				ctx.close();
				throw new RuntimeException("Unknown consistency maker " + consistencyMakerType);
			}
		}
		DebugHelper.printInfo("countConsistencyConstraint for " + viewname + " = " + countConsistencyConstraint);
		List<List<Region>> cliqueToregionTemp = new ArrayList<>();
		for (int i = 0; i < cliqueCount; i++) {
			List<Region> regionList = new ArrayList<>();

			IntList colIndxs = arasuCliquesAsColIndxs.get(i);
			Partition partition = cliqueIdxtoPList.get(i);
			for (int j = 0; j < partition.size(); j++) {
				Region variable = getTruncatedRegion(partition.at(j), colIndxs);
				regionList.add(variable);
			}
			cliqueToregionTemp.add(regionList);
		}

		onlyFormationSW.displayTimeAndDispose();

		// Dumping LP into a file -- Anupam
		// start
//		FileWriter fw;
//		try {
//			fw = new FileWriter("lpfile-" + viewname + ".txt");
//			fw.write(solver.toString());
//			fw.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		// System.err.println(solver.toString());
		// stop
		System.out.println("SOlving LP");
		System.gc();

		StopWatch onlySolvingSW = new StopWatch("LP-OnlySolving" + viewname);
		Status solverStatus = solver.Check();
		DebugHelper.printInfo("Solver Status: " + solverStatus);

		if (!Status.SATISFIABLE.equals(solverStatus)) {
			ctx.close();
			throw new RuntimeException("solverStatus is not SATISFIABLE");
		}

		Model model = solver.getModel();
		onlySolvingSW.displayTimeAndDispose();

		StopWatch postLPsolutionSW = new StopWatch("post LPsolution" + viewname);
		List<LinkedList<VariableValuePair>> cliqueIdxToVarValuesList = new ArrayList<>(cliqueCount);
		cliqueToRegionsSummary = new ArrayList<>();
		for (int i = 0; i < cliqueCount; i++) {

			IntList colIndxs = arasuCliquesAsColIndxs.get(i);
			Partition partition = cliqueIdxtoPList.get(i);
			LinkedList<VariableValuePair> varValuePairs = new LinkedList<>();
			for (int j = 0; j < partition.size(); j++) {
				String varname = "var" + i + "_" + j;

				// Variable to column indices mapping -- Anupam
				// start
				FileWriter fw1;
				try {
					fw1 = new FileWriter(viewname + "-var-to-colindices.txt", true);
					fw1.write(varname + " " + colIndxs.toString() + "\n");
					fw1.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// stop
				// System.err.println(varname+colIndxs.toString());
				long rowcount = Long.parseLong(model.evaluate(ctx.mkIntConst(varname), true).toString());
				// Variable to value mapping -- Anupam
				// start
				FileWriter fw2;
				try {
					fw2 = new FileWriter(viewname + "-var-to-value.txt", true);
					fw2.write(varname + " " + rowcount + "\n");
					fw2.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// stop
				if (rowcount != 0) {
					Region variable = getTruncatedRegion(partition.at(j), colIndxs);
					VariableValuePair varValuePair = new VariableValuePair(variable, rowcount);
					varValuePairs.add(varValuePair);
				}
			}
			cliqueIdxToVarValuesList.add(varValuePairs);

			if (!ignoreProjection) {
				Map<Integer, RegionSummary> regionsSummary = new HashMap<>();
				// int idx=0;

				for (int idx = 0; idx < cliqueIdxtoPList.get(i).size(); idx++) {
					Region reg = cliqueIdxtoPList.get(i).getAll().get(idx);
					String varname = "var" + i + "_" + idx;
					long rowcount = Long.parseLong(model.evaluate(ctx.mkIntConst(varname), true).toString());
					if (rowcount == 0)
						continue;
					RegionSummary regSum = new RegionSummary();
					regSum.region = getTruncatedRegion(reg, colIndxs);
					regSum.rowCount = rowcount;
					regionsSummary.put(idx, regSum);

				}

				Map<List<String>, Map<Integer, List<Integer>>> groupkeyToRegionToProjectionVariablesOptimzed = cliqueToGroupkeyToRegionToProjectionVariablesOptimzed
						.get(i);
				// int count=0;
				for (List<String> groupKey : groupkeyToRegionToProjectionVariablesOptimzed.keySet()) {

					List<ProjectionVariableOptimized> projectionVariables = cliqueToGroupkeyToProjectionVariablesOptimized
							.get(i).get(groupKey);// all projection variables of the groupkey in the clique
					Map<Integer, List<Integer>> regionToProjectionVariablesOptimzed = groupkeyToRegionToProjectionVariablesOptimzed
							.get(groupKey);
					for (Integer regionIdx : regionToProjectionVariablesOptimzed.keySet()) {
						String varnameRegion = "var" + i + "_" + regionIdx;
						long rowcountR = Long.parseLong(model.evaluate(ctx.mkIntConst(varnameRegion), true).toString());
						if (rowcountR == 0)
							continue;
						regionsSummary.get(regionIdx).groupkeys.add(groupKey);// adding the groupkey for this region in
																				// the
																				// regionsSummary

						List<Integer> projectionRegionsIdx = regionToProjectionVariablesOptimzed.get(regionIdx);
						// regionsSummary.get(regionIdx).groupKeyToRegionF.add(new ArrayList<>());
						regionsSummary.get(regionIdx).groupKeyToRegionF.put(getColumnsIdx(groupKey), new ArrayList<>());
						for (Integer projectionRegionIdx : projectionRegionsIdx) {
							String groupkeyStr = toStringFunc(groupKey);
							String varnameP = "y" + i + "_" + groupkeyStr + "_"
									+ projectionVariables.get(projectionRegionIdx).toString();
							Long rowCountP = Long.parseLong(model.evaluate(ctx.mkIntConst(varnameP), true).toString());
							if (rowCountP == 0)
								continue;
							Pair<Long, Long> pTemp = new Pair<Long, Long>(rowCountP, 0L);// count and cut off
							// The cut off is initially 0 which may be changed when regions are divided
							RegionF interval = projectionVariables.get(projectionRegionIdx).interval;
							Pair<RegionF, Pair<Long, Long>> p = new Pair<RegionF, Pair<Long, Long>>(
									projectionVariables.get(projectionRegionIdx).interval, pTemp);
							regionsSummary.get(regionIdx).groupKeyToRegionF.get(getColumnsIdx(groupKey)).add(p);

							// adding the count to the split count
							int colIdx = getColumnsIdx(groupKey).get(0);

							double splitPoint = interval.at(0).at(0).at(0);// we plan to generate all distinct points
																			// using
																			// the first split point.
							// this split point will be only present in this regionF

							int splitPointIdx = bucketSplitPoints.get(colIdx).indexOf(splitPoint);
// 							if(splitPointsCount.get(colIdx).get(splitPointIdx)!=0)
// 								System.out.println();
// 							if(colIdx==64)
//								System.out.println();
							splitPointsCount.get(colIdx).set(splitPointIdx, rowCountP);

							if (regionsSummary.get(regionIdx).groupKeyToRegionF.get(getColumnsIdx(groupKey)).isEmpty())
								System.out.println("What the ...!?");
						}
					}
					// count++;
				}
				// regions summary for every region in curr clique has been done.
				List<RegionSummary> regionsSummaryList = new ArrayList<>();
				regionsSummaryList.addAll(regionsSummary.values());// map to list. RegionIdx is lost but is no needed
																	// here
																	// on after
				cliqueToRegionsSummary.add(regionsSummaryList);

			}
		}
		/*
		 * 
		 * check for difference in summary size
		 */
		/*
		 * int basicSolSize = 0; for (int i = 0; i < cliqueCount; i++) {
		 * Map<List<String>, List<ProjectionVariableOptimized>>
		 * groupkeyToProjectionVariablesOptimized =
		 * cliqueToGroupkeyToProjectionVariablesOptimized .get(i); int count = 0; for
		 * (List<String> groupkey : groupkeyToProjectionVariablesOptimized.keySet()) {
		 * String groupkeyStr = toStringFunc(groupkey);
		 * List<ProjectionVariableOptimized> projectionVariablesOptimized =
		 * groupkeyToProjectionVariablesOptimized .get(groupkey); for (int j = 0; j <
		 * projectionVariablesOptimized.size(); j++) { ProjectionVariableOptimized
		 * projVar = projectionVariablesOptimized.get(j); String varname = "y" + i + "_"
		 * + groupkeyStr + "_" + projVar.toString();
		 * 
		 * Long rowCountP = Long.parseLong(model.evaluate(ctx.mkIntConst(varname),
		 * true).toString()); if (rowCountP == 0) continue; count +=
		 * projVar.positive.size(); basicSolSize++; } System.out.println("GroupKey=" +
		 * groupkey + ":" + count); }
		 * 
		 * } System.out.println("Basic SOlution ssize=" + basicSolSize);
		 */
		ctx.close();

		postLPsolutionSW.displayTimeAndDispose();
		return cliqueIdxToVarValuesList;

	}

	private void nonOverlappingSanityCheck(Set<Integer> projectionVariablesIdx,
			List<ProjectionVariableOptimized> projectionVariablesOptimized) {
		List<ProjectionVariableOptimized> projectionVariablesOptimizedCondition = new ArrayList<>();
		for (Integer projectionVariableIdx : projectionVariablesIdx) {
			projectionVariablesOptimizedCondition.add(projectionVariablesOptimized.get(projectionVariableIdx));
		}
		for (int i = 0; i < projectionVariablesOptimizedCondition.size(); i++) {
			for (int j = i + 1; j < projectionVariablesOptimizedCondition.size(); j++) {
				if (!checkNonOverlappingProjetionVariables(projectionVariablesOptimizedCondition.get(i),
						projectionVariablesOptimizedCondition.get(j)))
					throw new RuntimeException("projection variables are not disjoint. variable 1="
							+ projectionVariablesOptimizedCondition.get(i).toString() + " variable2="
							+ projectionVariablesOptimizedCondition.get(j).toString());
			}
		}
	}

	private boolean checkNonOverlappingProjetionVariables(ProjectionVariableOptimized projectionVariableOptimized1,
			ProjectionVariableOptimized projectionVariableOptimized2) {
		List<Integer> temp1 = new ArrayList<>();
		temp1.addAll(projectionVariableOptimized1.positive);
		temp1.retainAll(projectionVariableOptimized2.negative);
		if (!temp1.isEmpty())
			return true;

		List<Integer> temp2 = new ArrayList<>();
		temp2.addAll(projectionVariableOptimized2.positive);
		temp2.retainAll(projectionVariableOptimized1.negative);
		if (!temp2.isEmpty())
			return true;
		if (projectionVariableOptimized1.intersection.intersection(projectionVariableOptimized2.intersection).isEmpty())
			return true;

		System.out.print("false");
		return false;
	}

	// codeS
	// **********Helper functions for projection by shadab start******************//

	private IntList getColumnsIdx(List<String> groupkey) {
//		List<Integer> projectionColumnsIdx = new ArrayList<>();
		IntList projectionColumnsIdx = new IntArrayList();

		// converting col names to index
		for (String projectionColumn : groupkey) {
			projectionColumnsIdx.add(sortedViewColumns.indexOf(projectionColumn));
		}
		return projectionColumnsIdx;
	}

	private boolean subset(List<String> set1, List<String> set2) {
		if (set1.size() > set2.size())
			return false;
		for (int i = 0; i < set1.size(); i++) {
			if (!set1.get(i).equals(set2.get(i))) {
				return false;
			}
		}
		return true;
	}

	private String toStringFunc(List<String> groupkey) {
		// TODO Auto-generated method stub
		String ans = "";
		for (String group : groupkey.subList(0, groupkey.size() - 1)) {
			ans = ans + "," + group;
		}
		return ans + groupkey.get(groupkey.size() - 1);
	}

	private Map<Integer, List<Integer>> getRegionToProjectionVaribles(List<Region> regionList,
			List<List<Integer>> aggregateRegionIdxList, List<ProjectionVariable> projectionVariables, int i) {

		// regionList,aggregateRegionIdxList,projectionVariables,i
		Map<Integer, List<Integer>> regionToProjectionVariables = new HashMap<>();
		for (int j = 0; j < regionList.size(); j++) {
			if (aggregateRegionIdxList.get(i).contains(j)) {
				regionToProjectionVariables.put(j, new ArrayList<>());
				List<Integer> projectionVarIdxList = new ArrayList<>();
				for (int k = 0; k < projectionVariables.size(); k++) {
					if (projectionVariables.get(k).regionList.contains(j)) {
						projectionVarIdxList.add(k);
					}

				}
				regionToProjectionVariables.put(j, projectionVarIdxList);
			}
		}
		// TODO Auto-generated method stub
		return regionToProjectionVariables;
	}

	public boolean isAggregateRegion(Region currRegion, List<Region> conditionAggregateRegions) {
		// TODO Auto-generated method stub
		for (int i = 0; i < conditionAggregateRegions.size(); i++) {
			Region conditionRegion = conditionAggregateRegions.get(i);
			if (currRegion.minus(conditionRegion).isEmpty())
				return true;
		}
		return false;
	}

	private List<ProjectionVariable> getProjectionRegions(List<Integer> aggregateRegionIdx,
			List<String> projectionColumns, int cliqueIdx) {

		List<ProjectionVariable> projectionVariableList = new ArrayList<>();// stores the final list of projection
																			// variables.
		List<ProjectionVariable> prevProjectionVariableList = new ArrayList<>();
		List<Integer> projectionColumnsIdx = new ArrayList<>();
		List<Region> regionList = cliqueIdxtoPList.get(cliqueIdx).getAll();
		for (String projectionColumn : projectionColumns) {
			projectionColumnsIdx.add(sortedViewColumns.indexOf(projectionColumn));
		}
		for (Integer regionId : aggregateRegionIdx) {
			ProjectionVariable currVar = new ProjectionVariable(regionId);
			currVar.groupkey = projectionColumns;
			currVar.intersectionRegion = projectRegion(regionList.get(regionId), projectionColumns);
			projectionVariableList.add(currVar);
			prevProjectionVariableList.add(currVar);
		}
		System.out.println(projectionVariableList.size());
		int maxLength = aggregateRegionIdx.size(); // the maximum length is upper bounded by the no. of regions(when all
													// regions intersect)_
		for (int k = 2; k <= maxLength; k++) {
			// generates k-length projection regions
			List<ProjectionVariable> currProjectionVariableList = new ArrayList<>();
			for (int i = 0; i < prevProjectionVariableList.size(); i++) {
				for (int j = i + 1; j < prevProjectionVariableList.size(); j++) {
					ProjectionVariable newVariable = areCompatible(prevProjectionVariableList.get(i),
							prevProjectionVariableList.get(j), projectionColumnsIdx, k - 1, cliqueIdx);
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

	private ProjectionVariable areCompatible(ProjectionVariable projectionVariable1,
			ProjectionVariable projectionVariable2, List<Integer> projectionColumnsIdx, int len, int cliqueIdx) {
		// Makes projection regions of length len+1 using 2 PRs of length len. Two PRs
		// are compatible iff their regionlist till len-1 is identical.
		List<Integer> regionList1 = projectionVariable1.regionList;
		List<Integer> regionList2 = projectionVariable2.regionList;

		for (int i = 0; i < len - 1; i++) {
			if (!(regionList1.get(i) == regionList2.get(i))) {

				return null;
			}
		}
//		Region reg1=cliqueIdxtoPList.get(cliqueIdx).getAll().get(projectionVariable1.regionList.get(len-1));
//		Region reg2=cliqueIdxtoPList.get(cliqueIdx).getAll().get(projectionVariable2.regionList.get(len-1));
//		Region overlap=getOverlap(reg1,reg2,projectionColumnsIdx,cliqueIdx); 
//		if(overlap!=null) {
		Region intersection = projectionVariable1.intersectionRegion
				.intersection(projectionVariable2.intersectionRegion);// take intersection of both PRs
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

//
//	private boolean doOverlap(Integer regionIdx1, Integer regionIdx2, List<Integer> projectionColumnsIdx, int cliqueIdx) {
//		// TODO Auto-generated method stub
//		Region projectionRegion1=new Region();
//		Region projectionRegion2=new Region();
//		Region region1=cliqueIdxtoPList.get(cliqueIdx).getAll().get(regionIdx1);
//		Region region2=cliqueIdxtoPList.get(cliqueIdx).getAll().get(regionIdx2);
//		
//		projectionRegion1=projectRegion(region1,projectionColumnsIdx);
//		projectionRegion2=projectRegion(region2,projectionColumnsIdx);
//		if(projectionRegion1.intersection(projectionRegion2).size()!=0)
//			return true;
//		return false;
//	}
//
//	private Region getOverlap(Region projectedRegion1, Region projectedRegion2) {
//		// TODO Auto-generated method stub
//		
////		Region region1=cliqueIdxtoPList.get(cliqueIdx).getAll().get(regionIdx1);
////		Region region2=cliqueIdxtoPList.get(cliqueIdx).getAll().get(regionIdx2);		
//		
////		if(projectedRegion1.intersection(projectionRegion2).size()!=0)
////			return true;
////		return false;
//		return projectedRegion1.intersection(projectionRegion2);
//	}
	private Region projectRegionTemp(Region region, List<Integer> projectionColumnsIdx) {
		// TODO Auto-generated method stub
		Region region1 = region.getDeepCopy();

//		for (String projectionColumn : projectionColumns) {
//			projectionColumnsIdx.add(sortedViewColumns.indexOf(projectionColumn));
//		}
//		Region projectedRegion = new Region();
//		for (BucketStructure bs : region1.getAll()) {
//			BucketStructure bsNew = new BucketStructure();
//			for (int i = 0; i < bs.size(); i++) {
//				if (projectionColumnsIdx.contains(i))
//					bsNew.add(bs.at(i));
//			}
//			projectedRegion.add(bsNew);
//
//		}
//		compressRegions(projectedRegion);// many different BS can have the same projections. Hence, merge them.
//		if (projectedRegion.size() == 1)
//			return projectedRegion;
//
//		if (projectedRegion.size() >= 2)
//			throw new RuntimeException("Handle the projectRegion Method better");

		Region result = new Region();
		result.add(region1.at(0).projectBS(projectionColumnsIdx));
		for (int i = 1; i < region1.size(); i++) {
			BucketStructure projBS = region1.at(i).projectBS(projectionColumnsIdx);
			Region temp = new Region();
			temp.add(projBS);// project the BS and create a new region to use minus method.
			result.addAll(temp.minus(result).getAll());// doing union between the two regions. A U B = A + B\A
			compressRegions(result);
		}

//		BucketStructure bs1 = projectedRegion.getAll().get(0);
//		BucketStructure bs2 = projectedRegion.getAll().get(1);
//		projectedRegion = new Region();
//		projectedRegion.add(bs1);
//		projectedRegion.addAll(bs2.minus(bs1));

		return result;
	}

	private Region projectRegion(Region region1, List<String> projectionColumns) {
		// TODO Auto-generated method stub
		List<Integer> projectionColumnsIdx = getColumnsIdx(projectionColumns);

//		for (String projectionColumn : projectionColumns) {
//			projectionColumnsIdx.add(sortedViewColumns.indexOf(projectionColumn));
//		}
//		Region projectedRegion = new Region();
//		for (BucketStructure bs : region1.getAll()) {
//			BucketStructure bsNew = new BucketStructure();
//			for (int i = 0; i < bs.size(); i++) {
//				if (projectionColumnsIdx.contains(i))
//					bsNew.add(bs.at(i));
//			}
//			projectedRegion.add(bsNew);
//
//		}
//		compressRegions(projectedRegion);// many different BS can have the same projections. Hence, merge them.
//		if (projectedRegion.size() == 1)
//			return projectedRegion;
//
//		if (projectedRegion.size() >= 2)
//			throw new RuntimeException("Handle the projectRegion Method better");
		Region result = new Region();
		result.add(region1.at(0).projectBS(projectionColumnsIdx));
		for (int i = 1; i < region1.size(); i++) {
			BucketStructure projBS = region1.at(i).projectBS(projectionColumnsIdx);
			Region temp = new Region();
			temp.add(projBS);// project the BS and create a new region to use minus method.
			result.addAll(temp.minus(result).getAll());// doing union between the two regions. A U B = A + B\A
			compressRegions(result);
		}

//		BucketStructure bs1 = projectedRegion.getAll().get(0);
//		BucketStructure bs2 = projectedRegion.getAll().get(1);
//		projectedRegion = new Region();
//		projectedRegion.add(bs1);
//		projectedRegion.addAll(bs2.minus(bs1));

		return result;
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

	public Map<Integer, List<Integer>> getAggregateToRegionMap(List<Region> aggregateConditionRegions,
			List<Region> regionList) {
		Map<Integer, List<Integer>> aggregateConditionToRegions = new HashMap<>();
		for (int j = 0; j < aggregateConditionRegions.size(); j++) {
			Region cRegion = aggregateConditionRegions.get(j);
			List<Integer> regionIdTemp = new ArrayList<>();
			// for (Region region:regionList) {
			for (int k = 0; k < regionList.size(); k++) {
				if (regionList.get(k).minus(cRegion).isEmpty())// checksif region is contained in cRegion
				{

					regionIdTemp.add(k);
				}
			}
			if (regionIdTemp.size() != 0) {
				aggregateConditionToRegions.put(j, regionIdTemp);
			}
		}
		return aggregateConditionToRegions;
	}

	// All methods below are borrowed from z3
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

	// **********Helper functions for projection by shadab end******************//

	private void createProjVarToIntervalAndSSPRegionToMaxUniquePoints_sequential(Model model, Context ctx,
			List<HashMap<IntExpr, DirtyValueInterval>> cliqueWiseProjVarToInterval,
			List<HashMap<String, ProjectionStuffInColumn>> cliqueWColumnWProjectionStuff,
			HashMap<String, HashMap<Region, Long>> columnWSSPRegionToMaxUniqePoints) {
		for (int i = 0; i < cliqueCount; i++) {
			HashMap<String, ProjectionStuffInColumn> columnWiseProjectionStuff = cliqueWColumnWProjectionStuff.get(i);
			HashMap<IntExpr, DirtyValueInterval> projVarToInterval = new HashMap<>();
			for (Entry<String, ProjectionStuffInColumn> entry : columnWiseProjectionStuff.entrySet()) {
				String columnname = entry.getKey();
				ProjectionStuffInColumn projectionStuffInColumn = entry.getValue();
				HashMap<Region, Long> sspRegionToMaxUniqePoints = columnWSSPRegionToMaxUniqePoints.get(columnname);
				if (sspRegionToMaxUniqePoints == null) {
					sspRegionToMaxUniqePoints = new HashMap<>();
					columnWSSPRegionToMaxUniqePoints.put(columnname, sspRegionToMaxUniqePoints);
				}
				for (Entry<Region, ProjectionStuffInSSPRegion> regionAndProjectionStuff : projectionStuffInColumn
						.getMapSSPRegionToProjectionStuff().entrySet()) {
					Region region = regionAndProjectionStuff.getKey();
					ProjectionStuffInSSPRegion projectionStuff = regionAndProjectionStuff.getValue();
					Set<IntExpr> projVarsInSSPRegion = projectionStuff.getAllProjVars();
					long start = 0;
					long end = 0; // excluding end

					for (IntExpr projVar : projVarsInSSPRegion) {
						long count = Long.parseLong(model.evaluate(projVar, true).toString());
						if (count != 0) {
							end += count;
							projVarToInterval.put(projVar, new DirtyValueInterval(start, end));
							start = end;
						}
					}

					if (!sspRegionToMaxUniqePoints.containsKey(region))
						sspRegionToMaxUniqePoints.put(region, end);
					else {
						long maxEnd = sspRegionToMaxUniqePoints.get(region);
						if (end > maxEnd)
							sspRegionToMaxUniqePoints.put(region, end);
					}

					long maxint = Integer.MAX_VALUE;
					if (end >= maxint) // Search "LongIndexNeeded" in this code
						DirtyCode.throwError("problem");
				}
			}
			cliqueWiseProjVarToInterval.add(projVarToInterval);
		}
	}

	private void createProjVarToIntervalAndSSPRegionToMaxUniquePoints_hueristic(Model model, Context ctx,
			List<HashMap<IntExpr, DirtyValueInterval>> cliqueWiseProjVarToInterval,
			HashMap<String, HashMap<Region, List<ProjectionConsistencyInfoPair>>> columnWSSPRegionToAllProjectionConsistencyInfoPairs,
			List<HashMap<String, ProjectionStuffInColumn>> cliqueWColumnWProjectionStuff,
			HashMap<String, HashMap<Region, Long>> columnWSSPRegionToMaxUniqePoints) {
		HashMap<String, HashMap<Region, List<LinkedList<DirtyValueInterval>>>> columnWRegionWCliqueWAvailableIntervals = new HashMap<>();
		for (int i = 0; i < cliqueCount; ++i) {
			cliqueWiseProjVarToInterval.add(new HashMap<>());
		}

		// Assigning intervals to projVars which take part in any consistency constraint
		for (Entry<String, HashMap<Region, List<ProjectionConsistencyInfoPair>>> outerEntry : columnWSSPRegionToAllProjectionConsistencyInfoPairs
				.entrySet()) {
			String columnname = outerEntry.getKey();
			HashMap<Region, List<ProjectionConsistencyInfoPair>> sspRegionToAllProjectionConsistencyInfoPairs = outerEntry
					.getValue();
			HashMap<Region, List<LinkedList<DirtyValueInterval>>> regionWCliqueWAvailableIntervals = new HashMap<>();
			for (Entry<Region, List<ProjectionConsistencyInfoPair>> innerEntry : sspRegionToAllProjectionConsistencyInfoPairs
					.entrySet()) {
				Region region = innerEntry.getKey();
				SimpleGraph<ProjectionConsistencyInfo, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
				for (ProjectionConsistencyInfoPair projectionConsistencyInfoPair : innerEntry.getValue()) {
					ProjectionConsistencyInfo c1ProjectionConsistencyInfo = projectionConsistencyInfoPair.getFirst();
					ProjectionConsistencyInfo c2ProjectionConsistencyInfo = projectionConsistencyInfoPair.getSecond();

					graph.addVertex(c1ProjectionConsistencyInfo);
					graph.addVertex(c2ProjectionConsistencyInfo);
					graph.addEdge(c1ProjectionConsistencyInfo, c2ProjectionConsistencyInfo);
				}
				ConnectivityInspector<ProjectionConsistencyInfo, DefaultEdge> connectivityInspector = new ConnectivityInspector<>(
						graph);
				List<Set<ProjectionConsistencyInfo>> listOfConnectedProjectionConsistencyInfos = connectivityInspector
						.connectedSets();

				ArrayList<LinkedList<DirtyValueInterval>> cliqueWAvailableIntervals = new ArrayList<>(cliqueCount);
				for (int i = 0; i < cliqueCount; ++i) {
					LinkedList<DirtyValueInterval> temp = new LinkedList<>();
					temp.add(new DirtyValueInterval(0, relationCardinality));
					cliqueWAvailableIntervals.add(temp);
				}
				for (Set<ProjectionConsistencyInfo> connectedProjectionConsistencyInfos : listOfConnectedProjectionConsistencyInfos) {
					ProjectionConsistencyInfo infoWithZeroSlack = null;
					for (ProjectionConsistencyInfo projectionConsistencyInfo : connectedProjectionConsistencyInfos) {
						int cliqueIndex = projectionConsistencyInfo.cindex;
						long slackVarSum = 0;
						for (Integer slackVarIndex : projectionConsistencyInfo.slackVarsIndexes) {
							String slackVar = getSlackVarStringName(cliqueIndex, columnname, slackVarIndex);
							slackVarSum += Long.parseLong(model.evaluate(ctx.mkIntConst(slackVar), true).toString());
						}
						if (slackVarSum == 0) {
							infoWithZeroSlack = projectionConsistencyInfo;
							break;
						}
					}
					if (infoWithZeroSlack == null)
						DirtyCode.throwError("Wrong assumption");

					LinkedList<DirtyValueInterval> existingIntervalsAcrossOtherCliques = new LinkedList<>();
					for (ProjectionConsistencyInfo projectionConsistencyInfo : connectedProjectionConsistencyInfos) {
						if (projectionConsistencyInfo == infoWithZeroSlack)
							continue;
						HashMap<IntExpr, DirtyValueInterval> projVarToInterval = cliqueWiseProjVarToInterval
								.get(projectionConsistencyInfo.cindex);
						for (IntExpr projVar : projectionConsistencyInfo.projVars) {
							DirtyValueInterval interval = projVarToInterval.get(projVar);
							if (interval != null)
								DirtyCode.mergeValueInterval(existingIntervalsAcrossOtherCliques, interval);
						}
					}

					LinkedList<DirtyValueInterval> availableIntervalsOfInfoWithZeroSlack = cliqueWAvailableIntervals
							.get(infoWithZeroSlack.cindex);
					DirtyCode.intersectWithIntervals(existingIntervalsAcrossOtherCliques,
							availableIntervalsOfInfoWithZeroSlack, relationCardinality);

					HashMap<IntExpr, DirtyValueInterval> projVarToIntervalOfInfoWithZeroSlack = cliqueWiseProjVarToInterval
							.get(infoWithZeroSlack.cindex);
					for (IntExpr projVar : infoWithZeroSlack.projVars) {
						if (!projVarToIntervalOfInfoWithZeroSlack.containsKey(projVar)) {
							long count = Long.parseLong(model.evaluate(projVar, true).toString());
							if (count != 0) {
								DirtyValueInterval bestInterval = DirtyCode.getSuitableIntervalForProjVar(count,
										availableIntervalsOfInfoWithZeroSlack, existingIntervalsAcrossOtherCliques);
								DirtyCode.removeFromIntervals(bestInterval, existingIntervalsAcrossOtherCliques);
								DirtyCode.removeFromIntervals(bestInterval, availableIntervalsOfInfoWithZeroSlack);
								projVarToIntervalOfInfoWithZeroSlack.put(projVar, bestInterval);
							}
						}
					}

					LinkedList<DirtyValueInterval> existingIntervalsInInfoWithZeroSlack = new LinkedList<>();
					for (IntExpr projVar : infoWithZeroSlack.projVars) {
						DirtyValueInterval interval = projVarToIntervalOfInfoWithZeroSlack.get(projVar);
						if (interval != null) {
							DirtyCode.mergeValueInterval(existingIntervalsInInfoWithZeroSlack, interval);
						}
					}

					for (ProjectionConsistencyInfo projectionConsistencyInfo : connectedProjectionConsistencyInfos) {
						if (projectionConsistencyInfo == infoWithZeroSlack)
							continue;
						LinkedList<DirtyValueInterval> availableIntervals = cliqueWAvailableIntervals
								.get(projectionConsistencyInfo.cindex);
						HashMap<IntExpr, DirtyValueInterval> projVarToInterval = cliqueWiseProjVarToInterval
								.get(projectionConsistencyInfo.cindex);
						LinkedList<DirtyValueInterval> existingIntervals = DirtyCode
								.createCopyIntervals(existingIntervalsInInfoWithZeroSlack);
						DirtyCode.intersectWithIntervals(existingIntervals, availableIntervals, relationCardinality);
						for (IntExpr projVar : projectionConsistencyInfo.projVars) {
							if (!projVarToInterval.containsKey(projVar)) {
								long count = Long.parseLong(model.evaluate(projVar, true).toString());
								if (count != 0) {
									DirtyValueInterval bestInterval = DirtyCode.getSuitableIntervalForProjVar(count,
											availableIntervals, existingIntervals);
									DirtyCode.removeFromIntervals(bestInterval, existingIntervals);
									DirtyCode.removeFromIntervals(bestInterval, availableIntervals);
									projVarToInterval.put(projVar, bestInterval);
								}
							}
						}
					}

				}
				regionWCliqueWAvailableIntervals.put(region, cliqueWAvailableIntervals);
			}
			columnWRegionWCliqueWAvailableIntervals.put(columnname, regionWCliqueWAvailableIntervals);
		}

		// Assigning intervals to projVars which do not take part in any consistency
		// constraint
		for (int cliqueIndex = 0; cliqueIndex < cliqueCount; cliqueIndex++) {
			HashMap<String, ProjectionStuffInColumn> columnWiseProjectionStuff = cliqueWColumnWProjectionStuff
					.get(cliqueIndex);
			HashMap<IntExpr, DirtyValueInterval> projVarToInterval = cliqueWiseProjVarToInterval.get(cliqueIndex);
			for (Entry<String, ProjectionStuffInColumn> entry : columnWiseProjectionStuff.entrySet()) {
				String columnname = entry.getKey();
				ProjectionStuffInColumn projectionStuffInColumn = entry.getValue();
				for (Entry<Region, ProjectionStuffInSSPRegion> regionAndProjectionStuff : projectionStuffInColumn
						.getMapSSPRegionToProjectionStuff().entrySet()) {
					Region region = regionAndProjectionStuff.getKey();
					ProjectionStuffInSSPRegion projectionStuff = regionAndProjectionStuff.getValue();
					Set<IntExpr> projVarsInSSPRegion = projectionStuff.getAllProjVars();

					HashMap<Region, List<LinkedList<DirtyValueInterval>>> regionWCliqueWAvailableIntervals = columnWRegionWCliqueWAvailableIntervals
							.get(columnname);
					if (regionWCliqueWAvailableIntervals == null) {
						regionWCliqueWAvailableIntervals = new HashMap<>();
						columnWRegionWCliqueWAvailableIntervals.put(columnname, regionWCliqueWAvailableIntervals);
					}
					List<LinkedList<DirtyValueInterval>> cliqueWAvailableIntervals = regionWCliqueWAvailableIntervals
							.get(region);
					if (cliqueWAvailableIntervals == null) {
						cliqueWAvailableIntervals = new ArrayList<>(cliqueCount);
						for (int j = 0; j < cliqueCount; ++j) {
							LinkedList<DirtyValueInterval> temp = new LinkedList<>();
							temp.add(new DirtyValueInterval(0, relationCardinality));
							cliqueWAvailableIntervals.add(temp);
						}
						regionWCliqueWAvailableIntervals.put(region, cliqueWAvailableIntervals);
					}
					LinkedList<DirtyValueInterval> availableIntervals = cliqueWAvailableIntervals.get(cliqueIndex);

					for (IntExpr projVar : projVarsInSSPRegion) {
						if (!projVarToInterval.containsKey(projVar)) {
							long count = Long.parseLong(model.evaluate(projVar, true).toString());
							if (count != 0) {
								DirtyValueInterval bestInterval = DirtyCode.getSuitableIntervalForProjVar(count,
										availableIntervals, null);
								DirtyCode.removeFromIntervals(bestInterval, availableIntervals);
								projVarToInterval.put(projVar, bestInterval);
							}
						}
					}
				}
			}
		}

		// Finding MaxUniqePoints per sspRegion
		for (Entry<String, HashMap<Region, List<LinkedList<DirtyValueInterval>>>> outerEntry : columnWRegionWCliqueWAvailableIntervals
				.entrySet()) {
			String columnname = outerEntry.getKey();
			HashMap<Region, List<LinkedList<DirtyValueInterval>>> regionWCliqueWAvailableIntervals = outerEntry
					.getValue();
			HashMap<Region, Long> sspRegionToMaxUniqePoints = new HashMap<>();
			for (Entry<Region, List<LinkedList<DirtyValueInterval>>> innerEntry : regionWCliqueWAvailableIntervals
					.entrySet()) {
				Region region = innerEntry.getKey();
				List<LinkedList<DirtyValueInterval>> cliqueWAvailableIntervals = innerEntry.getValue();
				long maxEnd = 0;
				for (LinkedList<DirtyValueInterval> availableIntervals : cliqueWAvailableIntervals) {
					long end = availableIntervals.getLast().start;
					if (end > maxEnd)
						maxEnd = end;
				}
				sspRegionToMaxUniqePoints.put(region, maxEnd);

				long maxint = Integer.MAX_VALUE;
				if (maxEnd >= maxint) // Search "LongIndexNeeded" in this code
					DirtyCode.throwError("problem");
			}
			columnWSSPRegionToMaxUniqePoints.put(columnname, sspRegionToMaxUniqePoints);
		}
	}

	private boolean containsOnlySlacks(List<IntExpr> c2NewVars) {
		for (IntExpr intExpr : c2NewVars) {
			if (!intExpr.toString().startsWith(slackVarNamePrefix))
				return false;
		}
		return true;
	}

	private void createNonAggVarsToProjectionValues(Model model, Context ctxPhase2, Model modelPhase2,
			ProjectionStuffInColumn projectionStuffInColumn, Set<Integer> slackVarsIndexes, String columnname,
			Long maxUniqePoints, HashMap<IntExpr, ProjectionValues> varsToProjectionValues, int colIndx, int cindex) {
		List<Set<IntExpr>> listOfAtomicSetOfNonAggVars = projectionStuffInColumn.getListOfAtomicSetOfNonAggVars();
		for (Integer slackVarIndex : slackVarsIndexes) {
			Set<IntExpr> atomicSetOfNonAggVars = listOfAtomicSetOfNonAggVars.get(slackVarIndex);
			long atomicSetRowCount = 0;
			for (IntExpr nonAggVar : atomicSetOfNonAggVars) {
				long rowcount = Long.parseLong(model.evaluate(nonAggVar, true).toString());
				atomicSetRowCount += rowcount;
			}
			if (atomicSetRowCount > 0) {
				String slackVarName = getSlackVarStringName(cindex, columnname, slackVarIndex);

				Iterator<IntExpr> it = atomicSetOfNonAggVars.iterator();
				IntExpr curNonAggVar = null;
				long curNonAggVarUsableSize = 0;
				do {
					curNonAggVar = it.next(); // This should not throw error!
					curNonAggVarUsableSize = Long.parseLong(model.evaluate(curNonAggVar, true).toString());
				} while (curNonAggVarUsableSize == 0);
				ProjectionValues projectionValues = getOrAdd(varsToProjectionValues, curNonAggVar); // projectionValues
																									// in one nonAggVar

				long prevOccurrence = -1;
				long startFrom = -1;
				long lastSeenI = -1;

				outerloop: for (long i = -1, curOccurrence = 0;;) {
					do {
						++i;
						if (i == maxUniqePoints)
							break;
						IntExpr intExpr = ctxPhase2.mkIntConst(slackVarName + "_" + i);
						curOccurrence = Long.parseLong(modelPhase2.evaluate(intExpr, true).toString());
					} while (curOccurrence == 0);

					if (i == maxUniqePoints)
						break;

					if (lastSeenI + 1 != i) { // If i's are not continuous then need to end interval
						if (prevOccurrence > 0) {
							projectionValues.addToList(colIndx, startFrom, lastSeenI + 1, prevOccurrence); // excluding
																											// i i.e.
																											// last
																											// point
						}

						boolean changed = false;
						while (curNonAggVarUsableSize == 0) {
							if (it.hasNext())
								curNonAggVar = it.next();
							else
								break outerloop;
							curNonAggVarUsableSize = Long.parseLong(model.evaluate(curNonAggVar, true).toString());
							changed = true;
						}
						if (changed)
							projectionValues = getOrAdd(varsToProjectionValues, curNonAggVar);
						prevOccurrence = -1;
						startFrom = -1;
					}

					if (curOccurrence == prevOccurrence) {
						if (curNonAggVarUsableSize >= curOccurrence) {
							curNonAggVarUsableSize -= curOccurrence;
						} else {
							// Saving previous points
							projectionValues.addToList(colIndx, startFrom, i, prevOccurrence);
							// Saving current point instances which can be accommodated in curNonAggVar
							projectionValues.addToList(colIndx, i, i + 1, curNonAggVarUsableSize);
							curOccurrence -= curNonAggVarUsableSize;
							while (true) {
								do {
									if (it.hasNext())
										curNonAggVar = it.next();
									else
										break outerloop;
									curNonAggVarUsableSize = Long
											.parseLong(model.evaluate(curNonAggVar, true).toString());
								} while (curNonAggVarUsableSize == 0);
								projectionValues = getOrAdd(varsToProjectionValues, curNonAggVar);
								if (curNonAggVarUsableSize >= curOccurrence) {
									curNonAggVarUsableSize -= curOccurrence; // space used by i'th value
									prevOccurrence = curOccurrence;
									startFrom = i;
									break;
								} else {
									projectionValues.addToList(colIndx, i, i + 1, curNonAggVarUsableSize);
									curOccurrence -= curNonAggVarUsableSize;
								}
							}
						}
					} else {
						if (prevOccurrence > 0) {
							projectionValues.addToList(colIndx, startFrom, i, prevOccurrence); // excluding i i.e. last
																								// point
						}
						if (curNonAggVarUsableSize >= curOccurrence) {
							curNonAggVarUsableSize -= curOccurrence;
						} else { // curNonAggVarUsableSize can't accommodate curCount
							do {
								projectionValues.addToList(colIndx, i, i + 1, curNonAggVarUsableSize);
								curOccurrence -= curNonAggVarUsableSize;
								do {
									if (it.hasNext())
										curNonAggVar = it.next();
									else
										break outerloop;
									curNonAggVarUsableSize = Long
											.parseLong(model.evaluate(curNonAggVar, true).toString());
								} while (curNonAggVarUsableSize == 0);
								projectionValues = getOrAdd(varsToProjectionValues, curNonAggVar);
							} while (curNonAggVarUsableSize < curOccurrence);
							curNonAggVarUsableSize -= curOccurrence; // space used by i'th value
						}
						prevOccurrence = curOccurrence;
						startFrom = i;
					}

					if (curNonAggVarUsableSize == 0) {
						if (prevOccurrence > 0) {
							projectionValues.addToList(colIndx, startFrom, i + 1, prevOccurrence); // excluding i i.e.
																									// last point
						}
						prevOccurrence = -1;
						startFrom = -1;
						do {
							if (it.hasNext())
								curNonAggVar = it.next();
							else
								break outerloop;
							curNonAggVarUsableSize = Long.parseLong(model.evaluate(curNonAggVar, true).toString());
						} while (curNonAggVarUsableSize == 0);
						projectionValues = getOrAdd(varsToProjectionValues, curNonAggVar);
					}
					lastSeenI = i;
				}

//				if (i == maxUniqePoints) it.hasNext()

				if (prevOccurrence > 0)
					DirtyCode.throwError("Problem!");
//					projectionValues.addToList(colIndx, startFrom, lastSeenI+1, prevOccurrence);

				// Filling leftover space with default value
				if (curNonAggVarUsableSize != 0)
					DirtyCode.throwError("Problem!");
//					projectionValues.fillLeftoverSpaceWithDefault(colIndx, curNonAggVarUsableSize);

				// Filling leftover space of leftover nonAggVars with default value
				while (it.hasNext()) {
					DirtyCode.throwError("Problem!");
//					curNonAggVar = it.next();
//					curNonAggVarUsableSize = Long.parseLong(modelouuuuu.evaluate(curNonAggVar, true).toString());
//					if (curNonAggVarUsableSize != 0) {
//						projectionValues = getOrAdd(varsToProjectionValues, curNonAggVar);
//						projectionValues.fillLeftoverSpaceWithDefault(colIndx, curNonAggVarUsableSize);
//					}
				}
			}
		}

		// sanity check: total projection values = count of selection values
		for (Entry<IntExpr, ProjectionValues> entry : varsToProjectionValues.entrySet()) {
			IntExpr var = entry.getKey();
			ProjectionValues values = entry.getValue();
			long rowcount = Long.parseLong(model.evaluate(var, true).toString());
			long projectionCount = values.getTotalCount(colIndx);
			if (projectionCount == -1)
				continue;
			if (rowcount != projectionCount)
				DirtyCode.throwError("Problem!");
		}
	}

	private void createAggVarsToProjectionValues(Model model, Context ctxPhase2, Model modelPhase2,
			ProjectionStuffInSSPRegion projectionStuffInSSPRegion,
			HashMap<IntExpr, DirtyValueInterval> projVarToInterval, String columnname, Long maxUniqePoints,
			HashMap<IntExpr, ProjectionValues> varsToProjectionValues, int colIndx, int cindex) {

		for (Entry<IntExpr, List<IntExpr>> entry : projectionStuffInSSPRegion.getAggVarsToProjVars().entrySet()) {
			IntExpr aggVar = entry.getKey();
			List<IntExpr> projVars = entry.getValue();
			long rowcount = Long.parseLong(model.evaluate(aggVar, true).toString());
			if (rowcount == 0)
				continue;
			ProjectionValues projectionValues = getOrAdd(varsToProjectionValues, aggVar); // projectionValues in one
																							// aggVar
			String aggVarName = aggVar.toString();
			for (IntExpr projVar : projVars) { // Every interval is disjoint
				DirtyValueInterval interval = projVarToInterval.get(projVar); // If interval was null then projVar value
																				// was 0
				if (interval != null) {
					long prevCount = -1;
					long startFrom = -1;
					for (long i = interval.start; i < interval.end; ++i) {
						IntExpr intExpr = ctxPhase2.mkIntConst("n_" + aggVarName + "_" + i);
						long curCount = Long.parseLong(modelPhase2.evaluate(intExpr, true).toString());
						if (curCount != prevCount) {
							if (prevCount != -1)
								projectionValues.addToList(colIndx, startFrom, i, prevCount); // excluding i i.e. last
																								// point
							prevCount = curCount;
							startFrom = i;
						}
					}
					projectionValues.addToList(colIndx, startFrom, interval.end, prevCount);
				}
			}
		}
	}

	private void createPointWiseConstraints(Model model, Solver solverPhase2, Context ctxPhase2,
			ProjectionConsistencyInfo projectionConsistencyInfo,
			List<HashMap<String, ProjectionStuffInColumn>> cliqueWColumnWProjectionStuff,
			List<HashMap<IntExpr, DirtyValueInterval>> cliqueWiseProjVarToInterval,
			List<List<IntExpr>> pointWListOfNewVars, String columnname, long maxUniqePoints) {

		int cindex = projectionConsistencyInfo.cindex;
		ProjectionStuffInColumn projectionStuffInColumn = cliqueWColumnWProjectionStuff.get(cindex).get(columnname);
		HashMap<IntExpr, DirtyValueInterval> projVarToInterval = cliqueWiseProjVarToInterval.get(cindex);

		HashMap<IntExpr, List<IntExpr>> aggVarsToProjVars = projectionStuffInColumn.getAggVarsToProjVars();
		Set<IntExpr> aggVars = projectionConsistencyInfo.aggVars;
		for (IntExpr aggVar : aggVars) {
			long rowcount = Long.parseLong(model.evaluate(aggVar, true).toString());
			if (rowcount == 0)
				continue;
			String aggVarName = aggVar.toString();
			List<IntExpr> projVars = aggVarsToProjVars.get(aggVar);
			List<IntExpr> addList = new ArrayList<>();
			for (IntExpr projVar : projVars) { // Every interval is disjoint
				DirtyValueInterval interval = projVarToInterval.get(projVar); // If interval was null then projVar value
																				// was 0
				if (interval != null)
					for (long i = interval.start; i < interval.end; ++i) {
						IntExpr intExpr = ctxPhase2.mkIntConst("n_" + aggVarName + "_" + i);
						addList.add(intExpr);
						solverPhase2.add(ctxPhase2.mkGe(intExpr, ctxPhase2.mkInt(1))); // Must be greater than 0
						pointWListOfNewVars.get((int) i).add(intExpr);
					}
			}
			solverPhase2.add(ctxPhase2.mkEq(ctxPhase2.mkAdd(addList.toArray(new ArithExpr[addList.size()])),
					ctxPhase2.mkInt(rowcount)));
		}

		List<Integer> slackVarsIndexes = projectionConsistencyInfo.slackVarsIndexes;
		List<Set<IntExpr>> listOfAtomicSetOfNonAggVars = projectionStuffInColumn.getListOfAtomicSetOfNonAggVars();

		for (Integer slackVarIndex : slackVarsIndexes) {
			Set<IntExpr> atomicSetOfNonAggVars = listOfAtomicSetOfNonAggVars.get(slackVarIndex);
			long atomicSetRowCount = 0;
			for (IntExpr nonAggVar : atomicSetOfNonAggVars) {
				long rowcount = Long.parseLong(model.evaluate(nonAggVar, true).toString());
				atomicSetRowCount += rowcount;
			}
			if (atomicSetRowCount > 0) {
				List<IntExpr> addList = new ArrayList<>();
				String slackVarName = getSlackVarStringName(cindex, columnname, slackVarIndex);
				for (long j = 0; j < maxUniqePoints; ++j) {
					IntExpr intExpr = ctxPhase2.mkIntConst(slackVarName + "_" + j);
					addList.add(intExpr);
					solverPhase2.add(ctxPhase2.mkGe(intExpr, ctxPhase2.mkInt(0)));
					pointWListOfNewVars.get((int) j).add(intExpr);
				}
				solverPhase2.add(ctxPhase2.mkEq(ctxPhase2.mkAdd(addList.toArray(new ArithExpr[addList.size()])),
						ctxPhase2.mkInt(atomicSetRowCount)));
			}
		}
	}

	private String getSlackVarStringName(int cliqueIndex, String columnname, int slackVarIndex) {
		return slackVarNamePrefix + cliqueIndex + "_" + columnname + "_" + slackVarIndex;
	}

	private List<ProjectionConsistencyInfoPair> getCorrespondingRegionList(
			HashMap<Region, List<ProjectionConsistencyInfoPair>> sspRegionToAllProjectionConsistencyInfoPairs,
			HashMap<Region, ProjectionStuffInSSPRegion> sspRegionToProjectionStuff, IntExpr aVar) {
		for (Entry<Region, ProjectionStuffInSSPRegion> entry : sspRegionToProjectionStuff.entrySet()) {
			Region region = entry.getKey();
			ProjectionStuffInSSPRegion projectionStuffInSSPRegion = entry.getValue();
			HashSet<IntExpr> unionAggAndNonAggVars = new HashSet<>(projectionStuffInSSPRegion.getAggVars());
			unionAggAndNonAggVars.addAll(projectionStuffInSSPRegion.getNonAggVars());
			if (unionAggAndNonAggVars.contains(aVar)) {
				List<ProjectionConsistencyInfoPair> listOfProjectionConsistencyInfoPairs = sspRegionToAllProjectionConsistencyInfoPairs
						.get(region);
				if (listOfProjectionConsistencyInfoPairs == null) {
					listOfProjectionConsistencyInfoPairs = new ArrayList<>();
					sspRegionToAllProjectionConsistencyInfoPairs.put(region, listOfProjectionConsistencyInfoPairs);
				}
				return listOfProjectionConsistencyInfoPairs;
			}
		}
		throw new RuntimeException("Corresponding SSP region of aVar not found!");
	}

	private void collectProjectionConsistencyData(Optimize solver, Context ctx, List<IntExpr> c1Vars,
			List<IntExpr> c2Vars, int c1index, int c2index, Set<String> commonCols,
			List<ProjectionConsistencyInfoPair> projectionConsistencyInfoPairs,
			List<HashMap<String, ProjectionStuffInColumn>> cliqueWColumnWProjectionStuff) {
		HashMap<String, ProjectionStuffInColumn> c1ColumnWProjectionStuffInColumn = cliqueWColumnWProjectionStuff
				.get(c1index);
		HashMap<String, ProjectionStuffInColumn> c2ColumnWProjectionStuffInColumn = cliqueWColumnWProjectionStuff
				.get(c2index);

		for (String columnname : commonCols) {
			ProjectionStuffInColumn c1ProjectionStuffInColumn = c1ColumnWProjectionStuffInColumn.get(columnname);
			ProjectionStuffInColumn c2ProjectionStuffInColumn = c2ColumnWProjectionStuffInColumn.get(columnname);
			if (c1ProjectionStuffInColumn == null && c2ProjectionStuffInColumn == null) // No projection on this column
				continue;

			if (c1ProjectionStuffInColumn == null || c2ProjectionStuffInColumn == null)
				throw new RuntimeException("Must not happen! Code must have handled this!");

			// Clique 1
			Set<IntExpr> c1AggVars = new HashSet<>();
			Set<IntExpr> c1NonAggVars = new HashSet<>();
			Set<IntExpr> c1ProjVars = new HashSet<>();
			getProjectionRelatedVars(c1Vars, c1ProjectionStuffInColumn, c1AggVars, c1NonAggVars, c1ProjVars);

			// Clique 2
			Set<IntExpr> c2AggVars = new HashSet<>();
			Set<IntExpr> c2NonAggVars = new HashSet<>();
			Set<IntExpr> c2ProjVars = new HashSet<>();
			getProjectionRelatedVars(c2Vars, c2ProjectionStuffInColumn, c2AggVars, c2NonAggVars, c2ProjVars);

			ProjectionConsistencyInfoPair projectionConsistencyInfoPair = new ProjectionConsistencyInfoPair(c1index,
					c2index, c1ProjVars, c1AggVars, c1NonAggVars, c2ProjVars, c2AggVars, c2NonAggVars, columnname);
			projectionConsistencyInfoPairs.add(projectionConsistencyInfoPair);
			c1ColumnWProjectionStuffInColumn.get(columnname).getConsistencyConstraintSetWNonAggVars().add(c1NonAggVars);
			c2ColumnWProjectionStuffInColumn.get(columnname).getConsistencyConstraintSetWNonAggVars().add(c2NonAggVars);
		}
	}

	private void getProjectionRelatedVars(List<IntExpr> vars, ProjectionStuffInColumn projectionStuffInColumn,
			Set<IntExpr> aggVars, Set<IntExpr> nonAggVars, Set<IntExpr> projVars) {
		Set<IntExpr> allAggVars;
		allAggVars = new HashSet<>(projectionStuffInColumn.getAggVarsToProjVars().keySet());
		aggVars.addAll(vars);
		aggVars.retainAll(allAggVars);
		nonAggVars.addAll(vars);
		nonAggVars.removeAll(allAggVars);

		HashMap<IntExpr, List<IntExpr>> aggVarsToProjVars = projectionStuffInColumn.getAggVarsToProjVars();
		for (IntExpr aggVar : aggVars) {
			projVars.addAll(aggVarsToProjVars.get(aggVar));
		}
	}

	private void dirtyMergeWithAlignment(IntList lhsColIndxs,
			List<DirtyValueCombinationWithProjectionValues> lhsValueCombinations, IntList rhsColIndxs,
			LinkedList<DirtyVariableValuePairWithProjectionValues> rhsVarValuePairs) {

		// Raghav didn't used retainAll in original mergeWithAlignment maybe because
		// ordering was getting changed!
		IntList tempColIndxs = new IntArrayList(rhsColIndxs);
		tempColIndxs.removeAll(lhsColIndxs);
		IntList sharedColIndxs = new IntArrayList(rhsColIndxs);
		sharedColIndxs.removeAll(tempColIndxs);

		IntList posInLHS = new IntArrayList(sharedColIndxs.size());
		IntList posInRHS = new IntArrayList(sharedColIndxs.size());
		for (IntIterator iter = sharedColIndxs.iterator(); iter.hasNext();) {
			int sharedColIndx = iter.nextInt();
			posInLHS.add(lhsColIndxs.indexOf(sharedColIndx));
			posInRHS.add(rhsColIndxs.indexOf(sharedColIndx));
		}

		IntList mergedColIndxsList = new IntArrayList(lhsColIndxs);
		mergedColIndxsList.addAll(rhsColIndxs);
		mergedColIndxsList = new IntArrayList(new IntOpenHashSet(mergedColIndxsList));
		Collections.sort(mergedColIndxsList);

		boolean[] fromLHS = new boolean[mergedColIndxsList.size()];
		int[] correspOriginalIndx = new int[mergedColIndxsList.size()];

		for (int i = 0; i < mergedColIndxsList.size(); i++) {
			fromLHS[i] = lhsColIndxs.contains(mergedColIndxsList.get(i));
			if (fromLHS[i]) {
				correspOriginalIndx[i] = lhsColIndxs.indexOf(mergedColIndxsList.get(i));
			} else {
				correspOriginalIndx[i] = rhsColIndxs.indexOf(mergedColIndxsList.get(i));
			}
		}
		List<DirtyValueCombinationWithProjectionValues> mergedValueCombinations = new ArrayList<>();
		for (DirtyValueCombinationWithProjectionValues lhsValueCombination : lhsValueCombinations) {
			IntList lhsColValues = lhsValueCombination.getColValues();
			long lhsRowcount = lhsValueCombination.getRowcount();
			ProjectionValues lhsProjectionValues = lhsValueCombination.getProjectionValues();

			for (ListIterator<DirtyVariableValuePairWithProjectionValues> rhsIter = rhsVarValuePairs
					.listIterator(); rhsIter.hasNext();) {
				DirtyVariableValuePairWithProjectionValues rhsVarValuePair = rhsIter.next();
				Region rhsVariable = rhsVarValuePair.variable;
				long rhsRowcount = rhsVarValuePair.rowcount;

				BucketStructure rhsCompatibleBS = getCompatibleBS(posInLHS, lhsColValues, posInRHS, rhsVariable);
				if (rhsCompatibleBS != null) {

					ProjectionValues rhsProjectionValues = rhsVarValuePair.getProjectionValues();
//                	if (lhsProjectionValues == null ^ rhsProjectionValues == null)		// Can happen if there is no projection on shared column but projection on non common column of one of the cliques
//                		DirtyCode.throwError("Should not happen!");

					IntList mergedColValues = new IntArrayList(mergedColIndxsList.size());
					for (int j = 0; j < mergedColIndxsList.size(); j++) {
						if (fromLHS[j]) {
							mergedColValues.add(lhsColValues.getInt(correspOriginalIndx[j]));
						} else {
							mergedColValues.add(rhsCompatibleBS.at(correspOriginalIndx[j]).at(0));
						}
					}

					ProjectionValues mergedProjectionvalues = null;
					long minMergable = -1;
					if (lhsRowcount <= rhsRowcount)
						minMergable = lhsRowcount;
					else
						minMergable = rhsRowcount;

					if (lhsProjectionValues == null && rhsProjectionValues == null) { // No projection on any of columns
																						// of any clique
					} else {
						if (lhsProjectionValues == null || rhsProjectionValues == null) { // No shared projection column
							mergedProjectionvalues = new ProjectionValues();
							if (lhsProjectionValues != null)
								mergedProjectionvalues.takeFrom(lhsProjectionValues, minMergable, null);
							else
								mergedProjectionvalues.takeFrom(rhsProjectionValues, minMergable, null);
						} else {
							Set<Integer> sharedProjectionCols = new HashSet<>(lhsProjectionValues.keySet());
							sharedProjectionCols.retainAll(rhsProjectionValues.keySet());
							if (sharedProjectionCols.size() == 0) { // No shared projection column
								mergedProjectionvalues = new ProjectionValues();
								if (lhsProjectionValues != null)
									mergedProjectionvalues.takeFrom(lhsProjectionValues, minMergable, null);
								if (rhsProjectionValues != null)
									mergedProjectionvalues.takeFrom(rhsProjectionValues, minMergable, null);
							} else { // Shared projection columns present
								mergedProjectionvalues = DirtyCode.getIntersectingProjectionValues(sharedColIndxs,
										lhsProjectionValues, rhsProjectionValues); // Returns intersecting projection
																					// values for shared indexes and
																					// remove those values from
																					// lhsProjectionValues and
																					// rhsProjectionValues
								minMergable = mergedProjectionvalues.getTotalCount();
								if (minMergable > 0) {
									mergedProjectionvalues.takeFrom(lhsProjectionValues, minMergable,
											sharedProjectionCols); // Getting projection values from lhsProjectionValues
																	// for those columns which were not present in
																	// sharedProjectionCols
									mergedProjectionvalues.takeFrom(rhsProjectionValues, minMergable,
											sharedProjectionCols); // Getting projection values from rhsProjectionValues
																	// for those columns which were not present in
																	// sharedProjectionCols
								}
							}
						}
					}
					if (minMergable > 0) {
						DirtyValueCombinationWithProjectionValues mergedValueCombination = new DirtyValueCombinationWithProjectionValues(
								mergedColValues, minMergable, mergedProjectionvalues);
						mergedValueCombinations.add(mergedValueCombination);
						lhsValueCombination.reduceRowcount(minMergable);
						lhsRowcount = lhsValueCombination.getRowcount();
						rhsVarValuePair.rowcount -= minMergable;
						if (rhsVarValuePair.rowcount == 0) {
							rhsIter.remove();
						}
						if (lhsRowcount == 0)
							break;
					}
				}
			}
		}

		boolean acceptErrorsWhenRegionMismatch = true;

		// if (DebugHelper.sanityChecksNeeded()) {
		for (DirtyValueCombinationWithProjectionValues lhsValueCombination : lhsValueCombinations) {
			if (lhsValueCombination.getRowcount() != 0) {
				if (!acceptErrorsWhenRegionMismatch)
					throw new RuntimeException("Found while merge ValueCombination " + lhsValueCombination
							+ " in LHS with unmet rowcount");
				Pair<DirtyVariableValuePairWithProjectionValues, BucketStructure> bestMatchPair = DirtyCode
						.findBestMatch(lhsValueCombination, rhsVarValuePairs, posInLHS, posInRHS);
				if (bestMatchPair.getFirst() == null)
					throw new RuntimeException("No rhs Var found with same rowcount and projectionValues!");
				DirtyVariableValuePairWithProjectionValues bestMatchRHSVariableValuePair = bestMatchPair.getFirst();
				BucketStructure bestMatchRHS_BS = bestMatchPair.getSecond();
				long rowCount = lhsValueCombination.getRowcount();
				IntList mergedColValues = new IntArrayList(mergedColIndxsList.size());
				for (int j = 0; j < mergedColIndxsList.size(); j++) {
					if (fromLHS[j]) {
						mergedColValues.add(lhsValueCombination.getColValues().getInt(correspOriginalIndx[j]));
					} else {
						mergedColValues.add(bestMatchRHS_BS.at(correspOriginalIndx[j]).at(0));
					}
				}
				ProjectionValues mergedProjectionvalues = lhsValueCombination.getProjectionValues();
				mergedProjectionvalues.takeFrom(bestMatchRHSVariableValuePair.getProjectionValues(), rowCount,
						mergedProjectionvalues.keySet());

				DirtyValueCombinationWithProjectionValues mergedValueCombination = new DirtyValueCombinationWithProjectionValues(
						mergedColValues, rowCount, mergedProjectionvalues);
				mergedValueCombinations.add(mergedValueCombination);
				lhsValueCombination.reduceRowcount(rowCount);
				rhsVarValuePairs.remove(bestMatchRHSVariableValuePair);
			}
		}
		if (!rhsVarValuePairs.isEmpty())
			throw new RuntimeException("Found while merge RHS variables not getting exhausted");

		// Updating targetColIndxs
		lhsColIndxs.clear();
		lhsColIndxs.addAll(mergedColIndxsList);

		// Updating targetValueCombinations
		lhsValueCombinations.clear();
		lhsValueCombinations.addAll(mergedValueCombinations);
	}

	private ProjectionValues getOrAdd(HashMap<IntExpr, ProjectionValues> varsToProjectionValues, IntExpr key) {
		ProjectionValues value = varsToProjectionValues.get(key);
		if (value == null) {
			value = new ProjectionValues();
			varsToProjectionValues.put(key, value);
		}
		return value;
	}

	private ViewSolution mergeCliqueSolutions(List<LinkedList<VariableValuePair>> cliqueIdxToVarValuesList) {

		IntList mergedColIndxs = new IntArrayList();
		List<ValueCombination> mergedValueCombinations = new ArrayList<>();

		mergedColIndxs.addAll(arasuCliquesAsColIndxs.get(0));
		// Instantiating variables of first clique
		for (VariableValuePair varValuePair : cliqueIdxToVarValuesList.get(0)) {
			IntList columnValues = getFloorInstantiation(varValuePair.variable);
			ValueCombination valueCombination = new ValueCombination(columnValues, varValuePair.rowcount);
			mergedValueCombinations.add(valueCombination);
		}

		for (int i = 1; i < cliqueCount; i++) { // merging with other cliques
			mergeWithAlignment(mergedColIndxs, mergedValueCombinations, arasuCliquesAsColIndxs.get(i),
					cliqueIdxToVarValuesList.get(i));
		}

		for (ListIterator<ValueCombination> iter = mergedValueCombinations.listIterator(); iter.hasNext();) {
			ValueCombination valueCombination = iter.next();
			valueCombination = getReverseMappedValueCombination(valueCombination);
			valueCombination = getExpandedValueCombination(valueCombination);
			iter.set(valueCombination);
		}

		ViewSolutionInMemory viewSolutionInMemory = new ViewSolutionInMemory(mergedValueCombinations.size());
		for (ValueCombination mergedValueCombination : mergedValueCombinations) {
			viewSolutionInMemory.addValueCombination(mergedValueCombination);
		}
		return viewSolutionInMemory;
	}

	private IntList getFloorInstantiation(Region variable) {
		// choose BS with min bucket floors
		BucketStructure leadingBS = variable.getLeadingBS();
		IntList columnValues = new IntArrayList(leadingBS.getAll().size());
		for (Bucket b : leadingBS.getAll()) {
			columnValues.add(b.min());
		}
		return columnValues;
	}

	/**
	 * lhs has instantiated ValueCombinations. Each lhs ValueCombination is extended
	 * by widthwise appending instantiated tuples from appropriate BucketStructure
	 * of compatible rhs variable(s). lhsColIndxs and lhsValueCombinations get side
	 * effects. rhsColIndxs gets NO side effects. rhsVarValuePairs gets exhausted
	 * and becomes empty. TODO: Could have been some smart alignment which prevents
	 * extra tuples to be added to primary view
	 * 
	 * @param lhsColIndxs
	 * @param lhsValueCombinations
	 * @param rhsColIndxs
	 * @param sourceValueCombinations
	 */
	private void mergeWithAlignment(IntList lhsColIndxs, List<ValueCombination> lhsValueCombinations,
			IntList rhsColIndxs, LinkedList<VariableValuePair> rhsVarValuePairs) {

		IntList tempColIndxs = new IntArrayList(rhsColIndxs);
		tempColIndxs.removeAll(lhsColIndxs);
		IntList sharedColIndxs = new IntArrayList(rhsColIndxs);
		sharedColIndxs.removeAll(tempColIndxs);

		IntList posInLHS = new IntArrayList(sharedColIndxs.size());
		IntList posInRHS = new IntArrayList(sharedColIndxs.size());
		for (IntIterator iter = sharedColIndxs.iterator(); iter.hasNext();) {
			int sharedColIndx = iter.nextInt();
			posInLHS.add(lhsColIndxs.indexOf(sharedColIndx));
			posInRHS.add(rhsColIndxs.indexOf(sharedColIndx));
		}

		IntList mergedColIndxsList = new IntArrayList(lhsColIndxs);
		mergedColIndxsList.addAll(rhsColIndxs);
		mergedColIndxsList = new IntArrayList(new IntOpenHashSet(mergedColIndxsList));
		Collections.sort(mergedColIndxsList);

		boolean[] fromLHS = new boolean[mergedColIndxsList.size()];
		int[] correspOriginalIndx = new int[mergedColIndxsList.size()];

		for (int i = 0; i < mergedColIndxsList.size(); i++) {
			fromLHS[i] = lhsColIndxs.contains(mergedColIndxsList.get(i));
			if (fromLHS[i]) {
				correspOriginalIndx[i] = lhsColIndxs.indexOf(mergedColIndxsList.get(i));
			} else {
				correspOriginalIndx[i] = rhsColIndxs.indexOf(mergedColIndxsList.get(i));
			}
		}

		List<ValueCombination> mergedValueCombinations = new ArrayList<>();
		for (ValueCombination lhsValueCombination : lhsValueCombinations) {
			IntList lhsColValues = lhsValueCombination.getColValues();
			long lhsRowcount = lhsValueCombination.getRowcount();

			for (ListIterator<VariableValuePair> rhsIter = rhsVarValuePairs.listIterator(); rhsIter.hasNext();) {
				VariableValuePair rhsVarValuePair = rhsIter.next();
				Region rhsVariable = rhsVarValuePair.variable;
				long rhsRowcount = rhsVarValuePair.rowcount;

				BucketStructure rhsCompatibleBS = getCompatibleBS(posInLHS, lhsColValues, posInRHS, rhsVariable);
				if (rhsCompatibleBS != null) {
					IntList mergedColValues = new IntArrayList(mergedColIndxsList.size());
					for (int j = 0; j < mergedColIndxsList.size(); j++) {
						if (fromLHS[j]) {
							mergedColValues.add(lhsColValues.getInt(correspOriginalIndx[j]));
						} else {
							mergedColValues.add(rhsCompatibleBS.at(correspOriginalIndx[j]).at(0));
						}
					}

					if (lhsRowcount <= rhsRowcount) {
						ValueCombination mergedValueCombination = new ValueCombination(mergedColValues, lhsRowcount);
						mergedValueCombinations.add(mergedValueCombination);
						lhsValueCombination.reduceRowcount(lhsRowcount);
						rhsVarValuePair.rowcount -= lhsRowcount;
						if (rhsVarValuePair.rowcount == 0) {
							rhsIter.remove();
						}
						break;
					} else {
						ValueCombination mergedValueCombination = new ValueCombination(mergedColValues, rhsRowcount);
						mergedValueCombinations.add(mergedValueCombination);
						lhsValueCombination.reduceRowcount(rhsRowcount);
						lhsRowcount = lhsValueCombination.getRowcount();
						rhsVarValuePair.rowcount = 0;
						rhsIter.remove();
					}
				}
			}
		}

		// if (DebugHelper.sanityChecksNeeded()) {
		for (ValueCombination lhsValueCombination : lhsValueCombinations) {
			if (lhsValueCombination.getRowcount() != 0)
				throw new RuntimeException(
						"Found while merge ValueCombination " + lhsValueCombination + " in LHS with unmet rowcount");
		}
		if (!rhsVarValuePairs.isEmpty())
			throw new RuntimeException("Found while merge RHS variables not getting exhausted");

		// Updating targetColIndxs
		lhsColIndxs.clear();
		lhsColIndxs.addAll(mergedColIndxsList);

		// Updating targetValueCombinations
		lhsValueCombinations.clear();
		lhsValueCombinations.addAll(mergedValueCombinations);
	}

	private static BucketStructure getCompatibleBS(IntList posInLHS, IntList lhsColValues, IntList posInRHS,
			Region var) {
		for (BucketStructure bs : var.getAll()) {
			if (isCompatible(posInLHS, lhsColValues, posInRHS, bs))
				return bs;
		}
		return null;
	}

	private static boolean isCompatible(IntList posInLHS, IntList lhsColValues, IntList posInRHS, BucketStructure bs) {
		for (IntIterator iterLHS = posInLHS.iterator(), iterRHS = posInRHS.iterator(); iterLHS.hasNext();) {
			int posL = iterLHS.nextInt();
			int posR = iterRHS.nextInt();

			if (!bs.at(posR).contains(lhsColValues.getInt(posL)))
				return false;
		}
		return true;
	}

	private HashMap<String, ProjectionStuffInColumn> getColumnWiseAggAndNonAggVarsInSingleSplitPointRegion(
			Set<String> clique, List<List<IntExpr>> solverConstraints,
			HashMap<String, Set<IntExpr>> columnWiseVarsInAllAggregateConditions,
			Map<String, List<Region>> aggregateColumnsToSingleSplitPointRegions, int offsetForSingleSplitPointRegions,
			ArrayList<String> sortedAggCols) {
		HashMap<String, ProjectionStuffInColumn> result = new HashMap<>();
		int tempIndex = 0;
		for (String columnname : sortedAggCols) {
			if (clique.contains(columnname)) {
				ProjectionStuffInColumn projectionStuffInColumn = new ProjectionStuffInColumn();

				Set<IntExpr> blocksInAllAggregateConditions = columnWiseVarsInAllAggregateConditions
						.getOrDefault(columnname, new HashSet<>()); // This clique might not have any aggregate
																	// condition on this column, hence getOrDefault
				List<Region> splitPointRegions = aggregateColumnsToSingleSplitPointRegions.get(columnname);
				for (int splitPointIndex = 0; splitPointIndex < splitPointRegions.size(); ++splitPointIndex) {
					// offsetForSingleSplitPointRegions + tempIndex is correct index because of
					// consistent
					// ordering during multiple iterations of
					// aggregateColumnsToSingleSplitPointRegions
					Set<IntExpr> aggBlocks = new HashSet<>(
							solverConstraints.get(offsetForSingleSplitPointRegions + tempIndex));
					Set<IntExpr> nonAggBlocks = new HashSet<>(aggBlocks);

					aggBlocks.retainAll(blocksInAllAggregateConditions); // Only retain those blocks which are also in
																			// aggregate conditions
					nonAggBlocks.removeAll(aggBlocks); // Remove those blocks which are in aggregate conditions

					projectionStuffInColumn.putProjectionStuffInSSPRegion(splitPointRegions.get(splitPointIndex),
							new ProjectionStuffInSSPRegion(aggBlocks, nonAggBlocks));
					tempIndex++;
				}
				result.put(columnname, projectionStuffInColumn);
			}
		}
		return result;
	}

	private HashMap<String, Set<IntExpr>> getColumnWiseAggVarsInAllAggregateConditions(List<FormalCondition> conditions,
			List<List<IntExpr>> solverConstraints, List<Pair<Integer, Boolean>> applicableConditions) {

		HashMap<String, Set<IntExpr>> result = new HashMap<>();
		for (int i = 0; i < applicableConditions.size(); ++i) {
			Pair<Integer, Boolean> applicableConditionInfo = applicableConditions.get(i);
			if (applicableConditionInfo.getSecond()) { // Is the condition applied with projection or without projection
				FormalCondition formalCondition = conditions.get(applicableConditionInfo.getFirst());
				List<String> completeGroupKey = ((FormalConditionAggregate) formalCondition).getGroupKey();
				if (completeGroupKey.size() != 1)
					throw new RuntimeException("Only 1-D projection supported!");
				String groupKey = completeGroupKey.get(0);
				if (!result.containsKey(groupKey)) {
					result.put(groupKey, new HashSet<>());
				}
				result.get(groupKey).addAll(solverConstraints.get(i));
			}
		}
		return result;
	}

	/**
	 * @cite https://www.geeksforgeeks.org/finding-all-subsets-of-a-given-set-in-java/
	 */
	private Set<List<IntExpr>> getPowerSet(List<IntExpr> elements) {
		System.out.println("Power Set of : " + elements.size());
		Set<List<IntExpr>> powerSet = new HashSet<>();
		int size = elements.size();
		if (size > 62)
			throw new RuntimeException(
					"one << 63 will overflow and won't work correctly because long is used. Change type of 'one' to BigInt or something like that!");
		long one = 1;
		for (int i = 0; i < (one << size); i++) {
			List<IntExpr> newSet = new ArrayList<>(); // using list to save on memory because set properties are not
														// required
			for (int j = 0; j < size; j++)
				// (1<<j) is a number with jth bit 1
				// so when we 'and' them with the
				// subset number we get which numbers
				// are present in the subset and which
				// are not
				if ((i & (1 << j)) > 0)
					newSet.add(elements.get(j));
			powerSet.add(newSet);
		}
		powerSet.remove(new ArrayList<>()); // removing empty set
		return powerSet;
	}

	private IntList getIntersectionColIndices(Set<String> cliqueI, Set<String> cliqueJ) {
		Set<String> temp = new HashSet<>(cliqueI);
		temp.removeAll(cliqueJ);
		Set<String> intersectingCols = new HashSet<>(cliqueI);
		intersectingCols.removeAll(temp);
		IntList intersectingColIndices = getSortedIndxs(intersectingCols);
		return intersectingColIndices;
	}

	/**
	 * cliqueIndextoPList stores current partition (set of variables) of every
	 * clique. This method takes two (intersecting) cliques i and j as input and
	 * replaces cliqueIndextoPList[i] with a newer partition (a fresh set of
	 * variables) cliqueIndextoPList[j] with a newer partition (a fresh set of
	 * variables) such that these newer variables of the two cliques can be used to
	 * frame consistency conditions later.
	 *
	 * @param cliqueIdxtoPList
	 * @param cliqueIdxtoPSimplifiedList
	 * @param i
	 * @param j
	 * @param intersectingColIndices
	 * @return
	 */
	private CliqueIntersectionInfo replaceWithFreshVariablesToEnsureConsistency(List<Partition> cliqueIdxtoPList,
			List<List<IntList>> cliqueIdxtoPSimplifiedList, int i, int j, IntList intersectingColIndices) {
		Partition partitionI = cliqueIdxtoPList.get(i);
		Partition partitionJ = cliqueIdxtoPList.get(j);

		List<Region> splitRegions = getSplitRegions(partitionI, partitionJ, intersectingColIndices);

		IntList parentOldVarIdsI = new IntArrayList();
		Partition freshPartitionI = getFreshVariables(parentOldVarIdsI, partitionI, splitRegions,
				intersectingColIndices);
		cliqueIdxtoPList.set(i, freshPartitionI);
		List<IntList> sourceListI = cliqueIdxtoPSimplifiedList.get(i);
		List<IntList> freshListI = new ArrayList<>(freshPartitionI.size());
		for (int a = 0; a < freshPartitionI.size(); a++) {
			freshListI.add(sourceListI.get(parentOldVarIdsI.getInt(a)));
		}
		cliqueIdxtoPSimplifiedList.set(i, freshListI);

		IntArrayList parentOldVarIdsJ = new IntArrayList();
		Partition freshPartitionJ = getFreshVariables(parentOldVarIdsJ, partitionJ, splitRegions,
				intersectingColIndices);
		cliqueIdxtoPList.set(j, freshPartitionJ);
		List<IntList> sourceListJ = cliqueIdxtoPSimplifiedList.get(j);
		List<IntList> freshListJ = new ArrayList<>(freshPartitionJ.size());
		for (int a = 0; a < freshPartitionJ.size(); a++) {
			freshListJ.add(sourceListJ.get(parentOldVarIdsJ.getInt(a)));
		}
		cliqueIdxtoPSimplifiedList.set(j, freshListJ);

		CliqueIntersectionInfo cliqueIntersectionInfo = new CliqueIntersectionInfo(i, j, intersectingColIndices);
		// cliqueIntersectionInfo.splitRegions = splitRegions;
		return cliqueIntersectionInfo;
	}

	private List<Region> getSplitRegions(Partition partitionI, Partition partitionJ, IntList intersectingColIndices) {
		List<Region> bothRegions = new ArrayList<>();
		bothRegions.addAll(partitionI.getAll());
		bothRegions.addAll(partitionJ.getAll());

		List<boolean[]> truncAllTrueBS = getTruncatedAllTrueBS(intersectingColIndices);
		List<Region> truncRegions = getTruncatedRegions(bothRegions, intersectingColIndices);

		Reducer reducer = new Reducer(truncAllTrueBS, truncRegions);
		Map<IntList, Region> P = reducer.getMinPartition();
		List<Region> splitRegions = new ArrayList<>(P.values());
		return splitRegions;
	}

	/**
	 * Returns a Partition with freshVariables and also populated parentOldVarIds of
	 * same length storing which oldVarId it came from
	 * 
	 * @param parentOldVarIds
	 * @param oldPartition
	 * @param splitRegions
	 * @param intersectingColIndices
	 * @return
	 */
	private Partition getFreshVariables(IntList parentOldVarIds, Partition oldPartition, List<Region> splitRegions,
			IntList intersectingColIndices) {

		List<Region> freshRegions = new ArrayList<>();
		List<Region> oldRegions = oldPartition.getAll();

		IntList oldRegionIds = new IntArrayList(oldRegions.size());
		for (int i = 0; i < oldRegions.size(); i++) {
			oldRegionIds.add(i);
		}

		for (Region splitRegion : splitRegions) {
			List<Region> tempOldRegions = new ArrayList<>();
			IntList tempOldRegionIds = new IntArrayList();

			for (int i = 0; i < oldRegions.size(); i++) {
				Region oldRegion = oldRegions.get(i);
				Integer oldRegionId = oldRegionIds.get(i);

				Region freshRegion = new Region(); // stores list of untruncated bs which come from intersection of
													// oldRegion and splitRegion
				Region remainRegion = new Region(); // stores list of untruncated bs which come from oldRegion minus
													// splitRegion
				for (BucketStructure oldBS : oldRegion.getAll()) { // need to do bs by bs so that untruncing is possible
					Region oldSingleBSRegion = new Region();
					oldSingleBSRegion.add(oldBS);
					Region truncOldSingleBSRegion = getTruncatedRegion(oldSingleBSRegion, intersectingColIndices);
					Region truncOverlap = truncOldSingleBSRegion.intersection(splitRegion);
					if (truncOverlap.isEmpty()) {
						remainRegion.add(oldBS);
					} else {
						Region untruncOverlap = getUntruncatedRegion(truncOverlap, oldSingleBSRegion,
								intersectingColIndices);
						freshRegion.addAll(untruncOverlap.getAll());

						Region remainTruncRegion = truncOldSingleBSRegion.minus(truncOverlap);
						if (!remainTruncRegion.isEmpty()) {
							Region remainUntruncRegion = getUntruncatedRegion(remainTruncRegion, oldSingleBSRegion,
									intersectingColIndices);
							remainRegion.addAll(remainUntruncRegion.getAll());
						}
					}
				}
				if (!freshRegion.isEmpty()) {
					freshRegions.add(freshRegion);
					parentOldVarIds.add(oldRegionId);
				}
				if (!remainRegion.isEmpty()) {
					tempOldRegions.add(remainRegion);
					tempOldRegionIds.add(oldRegionId);
				}

			}
			oldRegions = tempOldRegions;
			oldRegionIds = tempOldRegionIds;
		}

		if (!oldRegions.isEmpty() || !oldRegionIds.isEmpty())
			throw new RuntimeException("Expected oldRegions to be empty here. oldRegions " + oldRegions.size()
					+ " and oldRegionIds " + oldRegionIds.size());

		Partition freshVariables = new Partition(freshRegions);
		return freshVariables;
	}

	private List<boolean[]> getTruncatedAllTrueBS(IntList intersectingColIndices) {
		List<boolean[]> truncatedAllTrueBS = new ArrayList<>();
		for (int i = 0; i < allTrueBS.size(); i++) {
			if (intersectingColIndices.contains(i)) {
				truncatedAllTrueBS.add(allTrueBS.get(i));
			}
		}
		return truncatedAllTrueBS;
	}

	private List<Region> getTruncatedRegions(List<Region> regions, IntList intersectingColIndices) {
		List<Region> truncatedRegions = new ArrayList<>(regions.size());
		for (Region region : regions) {
			Region truncatedRegion = getTruncatedRegion(region, intersectingColIndices);
			truncatedRegions.add(truncatedRegion);
		}
		return truncatedRegions;
	}

	private static Region getTruncatedRegion(Region region, IntList intersectingColIndices) {
		Region truncatedRegion = new Region();
		for (BucketStructure bs : region.getAll()) {
			BucketStructure truncatedBS = new BucketStructure();
			List<Bucket> buckets = bs.getAll();
			for (int i = 0; i < buckets.size(); i++) {
				if (intersectingColIndices.contains(i)) {
					truncatedBS.add(buckets.get(i));
				}
			}
			truncatedRegion.add(truncatedBS);
		}
		return truncatedRegion;
	}

	private Region getUntruncatedRegion(Region truncatedRegion, Region donorRegion, IntList intersectingColIndices) {

		if (donorRegion.getAll().size() != 1)
			throw new RuntimeException("Can only untruncate Regions with single BucketStructure in donor but found "
					+ donorRegion.getAll().size());

		BucketStructure donorBS = donorRegion.getAll().get(0);
		Region untruncatedRegion = new Region();
		for (BucketStructure truncatedBS : truncatedRegion.getAll()) {

			BucketStructure untruncatedBS = new BucketStructure();
			int k = -1;
			for (int i = 0; i < donorBS.size(); i++) {
				if (intersectingColIndices.contains(i)) {
					untruncatedBS.add(truncatedBS.at(++k));
				} else {
					untruncatedBS.add(donorBS.at(i));
				}
			}
			untruncatedRegion.add(untruncatedBS);
		}
		return untruncatedRegion;
	}

	private void addTo_ConditionListToPairOfVarList(List<List<IntExpr>> applicableSolverConstraints,
			HashMap<IntList, MutablePair<List<IntExpr>, List<IntExpr>>> conditionListToSetOfVarList) {
		HashMap<IntExpr, IntList> varToConditionList = new HashMap<>();
		for (int i = 0; i < applicableSolverConstraints.size(); i++) {
			for (IntExpr var : applicableSolverConstraints.get(i)) { // For every region which satisfy i'th condition
				if (!varToConditionList.containsKey(var))
					varToConditionList.put(var, new IntArrayList());
				varToConditionList.get(var).add(i);
			}
		}
		HashMap<IntList, List<IntExpr>> conditionListToVarList = new HashMap<>();
		for (Entry<IntExpr, IntList> entry : varToConditionList.entrySet()) {
			IntExpr var = entry.getKey();
			IntList conditionsList = entry.getValue();
			if (!conditionListToVarList.containsKey(conditionsList)) {
				conditionListToVarList.put(conditionsList, new ArrayList<>());
			}
			conditionListToVarList.get(conditionsList).add(var);
		}

		for (Entry<IntList, List<IntExpr>> entry : conditionListToVarList.entrySet()) {
			MutablePair<List<IntExpr>, List<IntExpr>> mutablePair = conditionListToSetOfVarList.get(entry.getKey());
			if (mutablePair == null) {
				mutablePair = new MutablePair<List<IntExpr>, List<IntExpr>>(entry.getValue(), null);
				conditionListToSetOfVarList.put(entry.getKey(), mutablePair);
			} else {
				mutablePair.setSecond(entry.getValue());
			}
//			if(!conditionListToSetOfVarList.containsKey(entry.getKey())) {
//				conditionListToSetOfVarList.put(entry.getKey(), new ArrayList<>(2));
//			}
//			conditionListToSetOfVarList.get(entry.getKey()).add(entry.getValue());
		}
	}

}

//class CliqueIntersectionInfo {
//    final int     i;
//    final int     j;
//    final IntList intersectingColIndices;
//    //List<Region> splitRegions;
//
//    public CliqueIntersectionInfo(int i, int j, IntList intersectingColIndices) {
//        this.i = i;
//        this.j = j;
//        this.intersectingColIndices = intersectingColIndices;
//    }
//}
//
//
//
//class ProjectionConsistencyInfoPair {
//	final Pair<ProjectionConsistencyInfo, ProjectionConsistencyInfo> pair;
//	final String columnname;
//	public ProjectionConsistencyInfoPair(int c1index, int c2index, Set<IntExpr> c1ProjVars, Set<IntExpr> c1AggVars, Set<IntExpr> c1NonAggVars, Set<IntExpr> c2ProjVars, Set<IntExpr> c2AggVars, Set<IntExpr> c2NonAggVars, String columnname) {
//		ProjectionConsistencyInfo first = new ProjectionConsistencyInfo(c1index, c1ProjVars, c1AggVars, c1NonAggVars);
//		ProjectionConsistencyInfo second = new ProjectionConsistencyInfo(c2index, c2ProjVars, c2AggVars, c2NonAggVars);
//		this.columnname = columnname;
//		pair = new Pair<ProjectionConsistencyInfo, ProjectionConsistencyInfo>(first, second);
//	}
//	
//	public ProjectionConsistencyInfo getFirst() {
//		return pair.getFirst();
//	}
//	
//	public ProjectionConsistencyInfo getSecond() {
//		return pair.getSecond();
//	}
//	
//	public void setSlackVarsIndexes(List<Integer> c1SlackVarsIndexes, List<Integer> c2SlackVarsIndexes) {
//		pair.getFirst().setSlackVarsIndexes(c1SlackVarsIndexes);
//		pair.getSecond().setSlackVarsIndexes(c2SlackVarsIndexes);
//	}
//}
//
//class ProjectionConsistencyInfo {
//	final int cindex;
//	final Set<IntExpr> projVars;
//	final Set<IntExpr> aggVars;
//	final Set<IntExpr> nonAggVars;
//	List<Integer> slackVarsIndexes;
//	public ProjectionConsistencyInfo(int cindex, Set<IntExpr> projVars, Set<IntExpr> aggVars, Set<IntExpr> nonAggVars) {
//		this.cindex = cindex;
//		this.projVars = projVars;
//		this.aggVars = aggVars;
//		this.nonAggVars = nonAggVars;
//	}
//	
//	public void setSlackVarsIndexes(List<Integer> slackVarsIndexes) {
//		this.slackVarsIndexes = slackVarsIndexes;
//	}
//	
//	@Override
//    public int hashCode() {
//        final int prime = 31;
//        int result = cindex;
//        result = prime * result + aggVars.hashCode();
//        result = prime * result + nonAggVars.hashCode();
//        return result;
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//    	if (this == obj) return true;
//    	if (getClass() != obj.getClass())
//    		return false;
//    	@SuppressWarnings("unchecked")
//    	ProjectionConsistencyInfo that = (ProjectionConsistencyInfo)obj;
//    	if (!aggVars.equals(that.aggVars))
//    		return false;
//    	if (!nonAggVars.equals(that.nonAggVars))
//    		return false;
//    	
//    	return true;
//    }
//}
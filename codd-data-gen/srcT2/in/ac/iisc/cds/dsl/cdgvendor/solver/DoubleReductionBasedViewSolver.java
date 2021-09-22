package in.ac.iisc.cds.dsl.cdgvendor.solver;

/*
 * How to read code by dk:
 * Before every chunk of code a variable is defined. The value of that variable is found out in the corresponding code. The name of variable tells what the code is doing
 */

import java.util.ArrayList;
import java.util.Arrays;
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
import java.io.FileWriter;
import java.io.IOException;
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
import in.ac.iisc.cds.dsl.cdgvendor.model.SolverViewStats;
import in.ac.iisc.cds.dsl.cdgvendor.model.ValueCombination;
import in.ac.iisc.cds.dsl.cdgvendor.model.VariableValuePair;
import in.ac.iisc.cds.dsl.cdgvendor.model.ViewInfo;
import in.ac.iisc.cds.dsl.cdgvendor.model.ViewSolution;
import in.ac.iisc.cds.dsl.cdgvendor.model.ViewSolutionInMemory;
import in.ac.iisc.cds.dsl.cdgvendor.model.ViewSolutionRegion;
import in.ac.iisc.cds.dsl.cdgvendor.model.ViewSolutionWithSolverStats;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalCondition;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionAggregate;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionAndAggregate;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionOr;
import in.ac.iisc.cds.dsl.cdgvendor.reducer.Bucket;
import in.ac.iisc.cds.dsl.cdgvendor.reducer.BucketStructure;
import in.ac.iisc.cds.dsl.cdgvendor.reducer.Partition;
import in.ac.iisc.cds.dsl.cdgvendor.reducer.Reducer;
import in.ac.iisc.cds.dsl.cdgvendor.reducer.Region;
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

public class DoubleReductionBasedViewSolver extends AbstractCliqueFinder {

    private final SolverViewStats solverStats;
    private final boolean intervalization = false;
    private final String slackVarNamePrefix = "slack_";
    
    // added by tarun 
    List<LongList> cliqueIdxtoConditionBValuesList;
    List<Partition> cliqueIdxtoPList;
    List<List<IntList>> cliqueIdxtoPSimplifiedList;
    
    List<HashMap<Integer, Integer>> mappedIndexOfConsistencyConstraint;	// Required by CONSISTENCYFILTERS type of consistency maker
    List<Integer> cliqueWiseTotalSingleSplitPointRegions;	// per clique
    List<List<Pair<Integer, Boolean>>> applicableConditionsOnClique;
    List<CliqueIntersectionInfo> cliqueIntersectionInfos; // Required by BRUTEFORCE type of consistency make
    
    
    

    public DoubleReductionBasedViewSolver(String viewname, ViewInfo viewInfo, List<boolean[]> allTrueBS, List<Set<String>> arasuCliques,
            Map<String, IntList> bucketFloorsByColumns) {
        super(viewname, viewInfo, allTrueBS, arasuCliques, bucketFloorsByColumns);
        solverStats = new SolverViewStats();
        solverStats.relRowCount = viewInfo.getRowcount();
        
        // variables added here by tarun  -- starts
        cliqueIdxtoConditionBValuesList = new ArrayList<>(cliqueCount);
        cliqueIdxtoPList = new ArrayList<>(cliqueCount);
        cliqueIdxtoPSimplifiedList = new ArrayList<>(cliqueCount);
        
        mappedIndexOfConsistencyConstraint = new ArrayList<>();	// Required by CONSISTENCYFILTERS type of consistency maker
        cliqueWiseTotalSingleSplitPointRegions = new ArrayList<>();	// per clique
        applicableConditionsOnClique = new ArrayList<>();
        cliqueIntersectionInfos = new ArrayList<>();	// Required by BRUTEFORCE type of consistency make
        // variables added here by tarun ends --
        
        
    }

    @Override
    public ViewSolutionWithSolverStats solveView(List<FormalCondition> conditions, List<Region> conditionRegions, FormalCondition consistencyConstraints[], ConsistencyMakerType consistencyMakerType, Map<String, List<Region>> aggregateColumnsToSingleSplitPointRegions,List<FormalCondition>conditionsAggregate,List<Region> conditionAggregateRegions, List<Region> projectedConditionRegions, Map<Set<String>, List<Region>> groupKeyToConsistencyFilterRegions, Map<Set<String>, List<Region>> groupKeyTocRegion) {

        StopWatch formulationPlusSolvingSW = new StopWatch("LP-SolvingPlusPostSolving-" + viewname);
        beginLPFormulation();
        List<LinkedList<VariableValuePair>> cliqueIdxToVarValuesList = formulateAndSolve(conditions, conditionRegions, consistencyConstraints, consistencyMakerType, aggregateColumnsToSingleSplitPointRegions,conditionsAggregate,conditionAggregateRegions,projectedConditionRegions,groupKeyToConsistencyFilterRegions, groupKeyTocRegion);
        finishSolving();
        //ViewSolution viewSolution = mergeCliqueSolutions(cliqueIdxToVarValuesList); //commented for shadab code
        ViewSolutionRegion viewSolutionRegion = mergeCliqueSolutionsRegionwise(cliqueIdxToVarValuesList); // to be used for region merging
        finishPostSolving();
        solverStats.millisToSolve = formulationPlusSolvingSW.getTimeAndDispose();
        return new ViewSolutionWithSolverStats(viewSolutionRegion.viewSolution,viewSolutionRegion, solverStats);
    }
    public void solveView1(List<FormalCondition> conditions, List<Region> conditionRegions, FormalCondition consistencyConstraints[], ConsistencyMakerType consistencyMakerType, Map<String, List<Region>> aggregateColumnsToSingleSplitPointRegions) {
    	
    	StopWatch formulationPlusSolvingSW = new StopWatch("LP-Solving (meging cliques not included)" + viewname);
        beginLPFormulation();
        //commented for now
        //List<LinkedList<VariableValuePair>> cliqueIdxToVarValuesList = formulateAndSolve(conditions, conditionRegions, consistencyConstraints, consistencyMakerType, aggregateColumnsToSingleSplitPointRegions);
        finishSolving();
        solverStats.millisToSolve = formulationPlusSolvingSW.getTimeAndDispose();
        return ;
	}
    
    public ViewSolutionWithSolverStats mergeCliques(List<LinkedList<VariableValuePair>> cliqueIdxToVarValuesList) {
		
    	ViewSolution viewSolution = mergeCliqueSolutions(cliqueIdxToVarValuesList);
    	finishPostSolving();
    	return new ViewSolutionWithSolverStats(viewSolution, solverStats);
	}
    
    private ViewSolutionRegion mergeCliqueSolutionsRegionwise(List<LinkedList<VariableValuePair>> cliqueIdxToVarValuesList) {

        IntList mergedColIndxs = new IntArrayList();
        List<ValueCombination> mergedValueCombinations = new ArrayList<>();
        //S
        List<VariableValuePair>mergedVarValuePairs=new ArrayList();
        //arasuCliquesAsColIndxs --list of list of columns index (only those present in some constraint) in a subview.  
        mergedColIndxs.addAll(arasuCliquesAsColIndxs.get(0));
        //Instantiating variables of first clique
        for (VariableValuePair varValuePair : cliqueIdxToVarValuesList.get(0)) {
            mergedVarValuePairs.add(varValuePair);
        }
        //Shadab, mergeWithAlignmentRegionwise deletes the regions from arasuCliquesAsColIndxs. So if regions are needed post merging too, then make a deep copy in the function.
        for (int i = 1; i < cliqueCount; i++) {
            mergeWithAlignmentRegionwise(mergedColIndxs, mergedVarValuePairs, arasuCliquesAsColIndxs.get(i), cliqueIdxToVarValuesList.get(i));
        }

        
        for(VariableValuePair var:mergedVarValuePairs) {
        	IntList columnValues = getFloorInstantiation(var.variable);
        	
        	ValueCombination valueCombination = new ValueCombination(columnValues, var.rowcount);
        	mergedValueCombinations.add(valueCombination);
        	var.variable=getExpandedRegion(var.variable);
        	
        	
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
        ViewSolutionRegion viewSolution=new ViewSolutionRegion(viewSolutionInMemory,mergedVarValuePairs);

      return viewSolution;
       //commented by shadab return viewSolutionInMemory;
    }

    private void mergeWithAlignmentRegionwise(IntList lhsColIndxs, List<VariableValuePair>lhsVarValuePairs, IntList rhsColIndxs,
            LinkedList<VariableValuePair> rhsVarValuePairs) {
    	//Shadab-"snitches get stiches"
    	//System.out.println("Shadab has reached here");
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
        //adds the index of merged columns(from rhs or lhs)
        int count=0;
        List<VariableValuePair> mergedVarValuePairs = new ArrayList<>();//new table after merging
        for (VariableValuePair lhsVarValue: lhsVarValuePairs) {
        	Region lhsRegion=lhsVarValue.variable;
        	long lhsRowcount=lhsVarValue.rowcount;
        	boolean check=false;
        	count++;
        	int ind=0;
        	for (ListIterator<VariableValuePair> rhsIter = rhsVarValuePairs.listIterator(); rhsIter.hasNext();) {
	            VariableValuePair rhsVarValuePair = rhsIter.next();
	            Region rhsVariable = rhsVarValuePair.variable;
	            long rhsRowcount = rhsVarValuePair.rowcount;
	             ind++;
	            if (checkCompatibleRegions(posInLHS,lhsRegion, posInRHS, rhsVariable)) {//if rhsregion is compatible
	            	check=true;
	            	
	            	Region mergedTemp=new Region();//contains the region for the intersection of the two regions
	            	int totBS=0;
	            	
	            	for (int i=0; i<lhsRegion.size();i++) {//iterate over each clause
	            		BucketStructure lhsBS=lhsRegion.at(i);
	            		int totmatch=0;
	            		for(int j=0;j<rhsVariable.size();j++) {
	            			BucketStructure rhsBS=rhsVariable.at(j);
	            			BucketStructure mergedBS=new BucketStructure();//chceck  -----------------contains the new clause
	            			if (isCompatibleBS(posInLHS, lhsBS, posInRHS, rhsBS)){
	            				totmatch++;
	            				totBS++;
	            				int counter=0;
	            				for (int k = 0; k < mergedColIndxsList.size(); k++) {
	                                if (sharedColIndxs.size()>counter&&mergedColIndxsList.get(k)==sharedColIndxs.get(counter)) {
	                                	int lhsInd=posInLHS.get(counter);
	                                	int rhsInd=posInRHS.get(counter);
	                                	counter++;
	                                	Bucket inter=Bucket.getIntersection(lhsBS.at(lhsInd), rhsBS.at(rhsInd));
	                                	
	                                	mergedBS.add(inter);
	                                }
	                                else if (fromLHS[k]) {
	                                    mergedBS.add(lhsBS.at(correspOriginalIndx[k]));
	                                	//mergedColValues.add(lhsColValues.getInt(correspOriginalIndx[j]));
	                                } else {
	                                    mergedBS.add(rhsBS.at(correspOriginalIndx[k]));
	                                }
	                            }
	            				mergedTemp.add(mergedBS);
	            			}
	            			
	            		}
	            	}
	            	//do the rowcount
	            	if (lhsRowcount <= rhsRowcount) {
                        VariableValuePair mergedVariable = new VariableValuePair(mergedTemp,lhsRowcount);
                        mergedVarValuePairs.add(mergedVariable);
                        //lhsValueCombination.reduceRowcount(lhsRowcount);
                        lhsVarValue.rowcount=0;
                        rhsVarValuePair.rowcount -= lhsRowcount;
                        if (rhsVarValuePair.rowcount == 0) {
                            rhsIter.remove();// removes this region from RHSvar
                        }
                        break;
	            	}
	            	else {
	            		VariableValuePair mergedVariable = new VariableValuePair(mergedTemp, rhsRowcount);
                        mergedVarValuePairs.add(mergedVariable);
                        lhsVarValue.rowcount-=rhsRowcount;
                        lhsRowcount = lhsVarValue.rowcount;
                        rhsVarValuePair.rowcount = 0;
                        rhsIter.remove();
                    }
	            
	            	
	            }
	           
	        }
	        if(!check) {
	        	//System.out.println("failed");
	        	throw new RuntimeException("You Failed!!!!!Couldn't find a region in rhs for lhs region:" + lhsRegion );
	        }
	        
	        
        }
        for (VariableValuePair lhsRegion : lhsVarValuePairs) {
            if (lhsRegion.rowcount!= 0)
                throw new RuntimeException("Found while merge ValueCombination " + lhsRegion + " in LHS with unmet rowcount");
        }
        if (!rhsVarValuePairs.isEmpty())
            throw new RuntimeException("Found while merge RHS variables not getting exhausted");

        //Updating targetColIndxs
        lhsColIndxs.clear();
        lhsColIndxs.addAll(mergedColIndxsList);

        lhsVarValuePairs.clear();
        lhsVarValuePairs.addAll(mergedVarValuePairs);
	            
	      
    }



    private static boolean checkCompatibleRegions(IntList posInLHS, Region lhsRegion, IntList posInRHS, Region rhsRegion) {
    	//returns true if two regions are consistent. 
    		
		for (IntIterator iterLHS = posInLHS.iterator(), iterRHS = posInRHS.iterator(); iterLHS.hasNext();) {
			int posL = iterLHS.nextInt();
            int posR = iterRHS.nextInt();
            Bucket lB=new Bucket();
    		Bucket rB=new Bucket();

			for (BucketStructure bs : lhsRegion.getAll()) 
				lB=Bucket.union(lB,bs.at(posL));
			//lhsUnion.add(lB);
			for (BucketStructure bs : rhsRegion.getAll()) 
				rB=Bucket.union(rB,bs.at(posR));
			//lhsUnion.add(rB);
			if(!lB.isEqual(rB))
				return false;
		}
		return true;
	}
    
    private static boolean isCompatibleBS(IntList posInLHS, BucketStructure lhsBs, IntList posInRHS, BucketStructure rhsBs) {
    	//returns true if two BS's are consistent wrt common attribute 
    	
        for (IntIterator iterLHS = posInLHS.iterator(), iterRHS = posInRHS.iterator(); iterLHS.hasNext();) {
            int posL = iterLHS.nextInt();
            int posR = iterRHS.nextInt();
            Bucket lB=lhsBs.at(posL);
            Bucket rB=rhsBs.at(posR);
          //  if (!lhsBs.at(posL).equals(rhsBs.at(posR))
            if (Bucket.getIntersection(lB, rB)==null)
            	return false;
            
            //if (!RhsBs.at(posR).contains(lhsColValues.getInt(posL)))
            //    return false;
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

    

    
    

    private List<LinkedList<VariableValuePair>> formulateAndSolve(List<FormalCondition> conditions, List<Region> conditionRegions, FormalCondition consistencyConstraints[]
    		, ConsistencyMakerType consistencyMakerType, Map<String, List<Region>> aggregateColumnsToSingleSplitPointRegions,List<FormalCondition>conditionsAggregate,List<Region> conditionAggregateRegions, List<Region> projectedConditionRegions, Map<Set<String>, List<Region>> groupKeyToConsistencyFilterRegions, Map<Set<String>, List<Region>> groupKeyTocRegion) {
    	
    	// TODO : remove IntExpr with their original String names in projection code
    	
    	ArrayList<String> sortedAggCols = new ArrayList<>(aggregateColumnsToSingleSplitPointRegions.keySet());
    	Collections.sort(sortedAggCols);
    	
    	System.out.println("number of projected columns: " + sortedAggCols.size());
    	
    	StopWatch onlyReductionSW = new StopWatch("LP-OnlyReduction" + viewname);
    	
        //STEP 1: For each clique find set of applicable conditions and call variable reduction
        

        for (int i = 0; i < cliqueCount; i++) {
            LongList bvalues = new LongArrayList();
            Set<String> clique = arasuCliques.get(i);   // set of columns
            List<Region> cRegions = new ArrayList<>();
            List<Pair<Integer, Boolean>> applicableConditions = new ArrayList<>();
            for (int j = 0; j < conditions.size(); j++) {      
            	FormalCondition curCondition = conditions.get(j);
                Set<String> appearingCols = new HashSet<>();
                getAppearingCols(appearingCols, curCondition);   // appearing columns will have columns appeared in current condition

                if (clique.containsAll(appearingCols)) {
                    cRegions.add(conditionRegions.get(j));
                    bvalues.add(curCondition.getOutputCardinality());
                    if (curCondition instanceof FormalConditionAggregate)
                    	applicableConditions.add(new Pair<>(j ,true));
                    else
                    	applicableConditions.add(new Pair<>(j ,false));
                } else if (curCondition instanceof FormalConditionAggregate) {
                	removeAggregateCols(appearingCols, curCondition);
                	if (clique.containsAll(appearingCols)) {
                        cRegions.add(conditionRegions.get(j));
                        bvalues.add(curCondition.getOutputCardinality());
                        applicableConditions.add(new Pair<>(j ,false));
                	}
                }
            }
            applicableConditionsOnClique.add(applicableConditions);

            //Adding extra cRegion for all 1's condition
            Region allOnesCRegion = new Region();
            BucketStructure subConditionBS = new BucketStructure();
            for (int j = 0; j < allTrueBS.size(); j++) {
                Bucket bucket = new Bucket();
                for (int k = 0; k < allTrueBS.get(j).length; k++) {
                    if (allTrueBS.get(j)[k]) {		// Is this check needed?
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
	                getAppearingCols(appearingCols, condition);
	
	                if (clique.containsAll(appearingCols)) {
	                	indexKeeperForConsistency.put(j, tempIndexForConsistency++);
	                    cRegions.add(conditionRegions.get(conditions.size() + j));
	                }
				}
	            //commented for now by shadab
	            
	            // 1-D projection handling
//	            int offset = consistencyConstraints.length;
//	            int totalSingleSplitPointRegion = 0;
//	            for (String columnname : sortedAggCols) {
//	            	List<Region> singleSplitPointRegions = aggregateColumnsToSingleSplitPointRegions.get(columnname);
//					if (clique.contains(columnname)) {
//						cRegions.addAll(singleSplitPointRegions);	// Adding all because anyways all required for consistency
//						for (int j = 0; j < singleSplitPointRegions.size(); ++j)
//		                	indexKeeperForConsistency.put(offset + j, tempIndexForConsistency++);
//						totalSingleSplitPointRegion += singleSplitPointRegions.size();
//					}
//					offset += singleSplitPointRegions.size();
//				}
//				cliqueWiseTotalSingleSplitPointRegions.add(totalSingleSplitPointRegion);
//				
//	            mappedIndexOfConsistencyConstraint.add(indexKeeperForConsistency);
            }
            else {
	            // 1-D projection handling
	            int totalSingleSplitPointRegion = 0;
	            for (String columnname : sortedAggCols) {
					if (clique.contains(columnname)) {
	            	List<Region> singleSplitPointRegions = aggregateColumnsToSingleSplitPointRegions.get(columnname);
						cRegions.addAll(singleSplitPointRegions);	// Adding all because anyways all required for consistency
						totalSingleSplitPointRegion += singleSplitPointRegions.size();
					}
				}
	            cliqueWiseTotalSingleSplitPointRegions.add(totalSingleSplitPointRegion);	// Keeping track of totalSingleSplitPointRegion in cliques just to make solverConstraints size consistent
            }
            
///////////////// End dk
            DebugHelper.printInfo("Bachaaaao!");
            DebugHelper.printInfo(viewname + " " +clique + " " + cRegions);
            
            Reducer reducer = new Reducer(allTrueBS, cRegions);
            //Map<IntList, Region> P = reducer.getMinPartition();

            //Added by Anupam for Skew Mimicking
            reducer.refineRegionsforSkewMimicking();

            //Using varIds instead of old variable regions
            List<Region> oldVarList = new ArrayList<>();	// List of regions corresponding to below labels
            List<IntList> conditionIdxsList = new ArrayList<>();	// List of labels
            reducer.getVarsAndConditionsSimplified(oldVarList, conditionIdxsList);

            Partition cliqueP = new Partition(new ArrayList<>(oldVarList));
            cliqueIdxtoPList.add(cliqueP);

            cliqueIdxtoPSimplifiedList.add(conditionIdxsList);
        }
        
        //codeS
       
        
        
        List<Map<Set<String>,List<Integer>>>cliqueToQMap=new ArrayList<>(); //map from groupkey to list of region indices belonging to that group.
        for (int i =0;i<cliqueCount;i++) {
    
        	Map<Set<String>,List<Integer>> QMap=new HashMap<>();
        	Iterator<Map.Entry<Set<String>, List<Region>>> iterator = groupKeyTocRegion.entrySet().iterator();
		      while (iterator.hasNext()) {
		    	  Map.Entry<Set<String>, List<Region>> entry=iterator.next();
		    	  Set<String>groupKeys=entry.getKey();
		    	  List<Region> cRegions=entry.getValue();
		    	  List<Integer>Qcurr=new ArrayList<>();
		    	  List<Region>regionList=cliqueIdxtoPList.get(i).getAll();//added later

		    	  for(int j=0;j<regionList.size();j++) {
		    		  Region reg=regionList.get(j);
		    		  for (Region cRegion:cRegions) {
		    			  if(cRegion.contains(reg)) {
		    				  Qcurr.add(j);
		    				  break;
		    			  }
		    		  }
		    		  
		    	  }
		    	  if (Qcurr.isEmpty())
		    		  continue;
		    	  QMap.put(groupKeys, Qcurr);
        	}
		      cliqueToQMap.add(QMap);
        }
        
        
        //intervalization code starts
        if(intervalization) {
        	//generating regions for intervalization
            Map<Set<String>,List<Region>> groupkeysToIntervalRegions=new HashMap<>();
            
            for(int i=0;i<cliqueCount;i++) {
            	//groupKeyToConsistencyFilter
            	for(Set<String>groupKeys:groupKeyToConsistencyFilterRegions.keySet()) {
            		List<Region> cRegionsCurr=groupKeyToConsistencyFilterRegions.get(groupKeys);
            		Reducer reducerCurr = new Reducer(allTrueBS, cRegionsCurr);
                    
                    List<Region> oldVarListCurr = new ArrayList<>();	// List of regions corresponding to below labels
                    List<IntList> conditionIdxsListCurr = new ArrayList<>();	// List of labels
                    reducerCurr.getVarsAndConditionsSimplified(oldVarListCurr, conditionIdxsListCurr);

                    Partition cliquePCurr = new Partition(new ArrayList<>(oldVarListCurr));
                    groupkeysToIntervalRegions.put(groupKeys, cliquePCurr.getAll());
            		
            	}
            }
            
	        List<List<List<Region>>> broken=new ArrayList<>();
	        
	        for(int i=0;i<cliqueCount;i++) {
	        	List<Region>regionList=cliqueIdxtoPList.get(i).getAll();
	        	List<List<Region>> currCliqueBroken=new ArrayList<>();
	        	for (Region region:regionList) {
	        		List<Region> tempList=new ArrayList<>();
	        		tempList.add(region);
	        		currCliqueBroken.add(tempList);
	        		
	        	}
	        	broken.add(currCliqueBroken);
	        }
	        
	        //generating Q's for each groupkey
	       
	        //doing intervalization
	        //TODO if theres only a fixed groupkey then intervalization is not must. Set a configuration parameter for that
	        for(int i=0;i<cliqueCount;i++) {
	        	for(Set<String>groupKey:cliqueToQMap.get(i).keySet()) {
	        		List<Region>filterRegions=groupkeysToIntervalRegions.get(groupKey);
	        		for(Integer regionIdx:cliqueToQMap.get(i).get(groupKey)) {
	        			//for(Region reg:broken.get(i).get(regionIdx)) {
	        			List<Region>brokenRegions=broken.get(i).get(regionIdx);
	        			//int tempSize=brokenRegions.size();//done so that it does not iterate over the newly added minus regions which do not require refining. can be done for intersection regions too
	        			
	        			for(int j=0;j<brokenRegions.size();j++){
	        				
	        				for(Region filterRegion:filterRegions) {
	        					Region reg=brokenRegions.get(j);
	        					Region intersection=filterRegion.intersection(reg);
	        					if(intersection.isEmpty())
	        						continue;
	        					Region minus=reg.minus(intersection);
	        					if(minus.isEmpty())
	        						continue;
	        					brokenRegions.set(j, intersection);
	        					brokenRegions.add(j+1,minus);//temp
	        					
	        				}
	        				
	        			}
	        		}
	        	}
	        }
	        
	        //finished intervalization
	        //we have broken down the regions with intervalization. We have the QMap for the broken down regions.
	        //Need to update cliquePList.
	        
	        //First we flatten the regions from broken
	        List<Partition> newCliqueToP=new ArrayList<>();
	        for (int i=0;i<cliqueCount;i++) {
	        	Partition flattened=new Partition();
	        	List<Region> flattenBroken=new ArrayList<>();
	        	List<List<Region>>idxList=broken.get(i);
	        	for(List<Region> regions:idxList) {
	        		for (Region reg:regions) {
	        			//flattenBroken.add(reg);
	        			flattened.regionList.add(reg);
	        		}
	        	}
	        	newCliqueToP.add(flattened);
	        }
	        cliqueIdxtoPList=newCliqueToP;
	        
	        
	        //Generating new region to conditionIdx list (cliqueIdxtoPSimplifiedList)
	        //Can be done with the original cliqueIdx... more efficiently
	        List<List<IntList>> cliqueIdxtoPSimplifiedListNew=new ArrayList<>();
	 	        
	        for(int i=0;i<cliqueCount;i++) {
	        	List<IntList>PSimplifiedListNew=new ArrayList<>();
	        	List<IntList>PSimplifiedList=cliqueIdxtoPSimplifiedList.get(i);
	        	for(int j=0;j<PSimplifiedList.size();j++) {
	        		IntList condIdx=PSimplifiedList.get(j);
	        		for(int k=0; k<broken.get(i).get(j).size();k++) {
	        			PSimplifiedListNew.add(condIdx);
	        		}
	        		
	        	}
	        	cliqueIdxtoPSimplifiedListNew.add(PSimplifiedListNew);
	        }
	        cliqueIdxtoPSimplifiedList=cliqueIdxtoPSimplifiedListNew;
	        
	        
	        //TODO update QMAP
        }
        
        onlyReductionSW.displayTimeAndDispose();
        
               
        
        if (consistencyMakerType == ConsistencyMakerType.BRUTEFORCE) {	// Further divide regions for consistency
	        for (int i = 0; i < cliqueCount; i++) {
	            for (int j = i + 1; j < cliqueCount; j++) {
	                IntList intersectingColIndices = getIntersectionColIndices(arasuCliques.get(i), arasuCliques.get(j));
	                if (intersectingColIndices.isEmpty()) {
	                    continue;
	                }
	                CliqueIntersectionInfo cliqueIntersectionInfo =
	                        replaceWithFreshVariablesToEnsureConsistency(cliqueIdxtoPList, cliqueIdxtoPSimplifiedList, i, j, intersectingColIndices);
	                cliqueIntersectionInfos.add(cliqueIntersectionInfo);
	            }
	        }
        }

        long varcountDR = 0;
        for (int i = 0; i < cliqueCount; i++) {
            varcountDR += cliqueIdxtoPList.get(i).getAll().size();
        }
        DebugHelper.printInfo("Number of variables after double reduction " + varcountDR);
        
        
 // ************shadab projection region preprocessing starts*********************// 
        
        //codeS
        List<List<Region>> cliqueIdxtoAggregateRegion =new ArrayList<>();//list of regions which satisfy some projection filter.   
        List<List<Integer>> aggregateRegionIdxList =new ArrayList<>();//list of index of regions which satisfy some projection filter.
        
        List<Map<Integer,List<Integer>>> aggregateConditionToRegionsList=new ArrayList<>(); //creating a list of maps (aggregate condition index -> the region indices that are contained in that agrgregate condition. Needed for making constraints to identify all the regions in the condition regions
        
        List<List<ProjectionVariable>> cliqueIdxToProjectionRegions=new ArrayList<>(); //List of projection variables
    	List<Map<Integer,List<Integer>>> regionToProjectionVariablesList=new ArrayList<>(); //Map from region index to list of projection region indices. Needed for writing projection constraints
        
    	for (int i = 0; i < cliqueCount; i++) {
        	
        	//creating  list of regions satisfying some filter constraints
        	List<Integer> aggregateRegionsIdxTemp=new ArrayList<>();
        	List<Region> regionList=cliqueIdxtoPList.get(i).getAll();
        	System.out.println("here");
        	List<Region> regionsAggregate=new ArrayList<>();
        	for (int j=0;j<regionList.size();j++) {
        		Region currRegion=regionList.get(j);
        		if(isAggregateRegion(currRegion,conditionAggregateRegions)) {
        			regionsAggregate.add(currRegion);
        			aggregateRegionsIdxTemp.add(j);
        		}
        	}       	
        	cliqueIdxtoAggregateRegion.add(regionsAggregate);
        	aggregateRegionIdxList.add(aggregateRegionsIdxTemp);
        
        
        	//creating a map from aggregate conditon index to list of index regions in it
        	Map<Integer,List<Integer>> aggregateConditionToRegions=getAggregateToRegionMap(conditionAggregateRegions, cliqueIdxtoPList.get(i).getAll()); //added
        	aggregateConditionToRegionsList.add(aggregateConditionToRegions);                       
        	//done aggregatecondtions to list of regions map
        


    	
        	//creating list of projectionVariables and a map from region index to projection variable indices.. Only aggregate regions get mapped
    	    	
    		List<String> projectionColumns=((FormalConditionAggregate)(conditionsAggregate.get(0))).getGroupKey();
    		List<ProjectionVariable> projectionVariables=getProjectionRegions(aggregateRegionIdxList.get(i),projectionColumns,i);
    		cliqueIdxToProjectionRegions.add(projectionVariables);
    		
    		//creating a map which maps a region to all projection variables that it is contained in
    		regionList=cliqueIdxtoPList.get(i).getAll();
    		Map<Integer,List<Integer>> regionToProjectionVariables=new HashMap<>();
    		regionToProjectionVariables=getRegionToProjectionVaribles(regionList,aggregateRegionIdxList,projectionVariables,i);
 
    		regionToProjectionVariablesList.add(regionToProjectionVariables);
    		
    	}

        
        //**********Projection REgion preprocessing ends***********************//

                
        return formAndSolveLP(consistencyMakerType, consistencyConstraints, conditions, aggregateColumnsToSingleSplitPointRegions,conditionsAggregate,aggregateConditionToRegionsList,cliqueIdxtoAggregateRegion,aggregateRegionIdxList,cliqueIdxToProjectionRegions,regionToProjectionVariablesList);
        		
        		
    }
    
   
    
	private Map<Integer, List<Integer>> getRegionToProjectionVaribles(List<Region> regionList,
			List<List<Integer>> aggregateRegionIdxList,List<ProjectionVariable> projectionVariables, int i) {
		
		//regionList,aggregateRegionIdxList,projectionVariables,i
		Map<Integer,List<Integer>> regionToProjectionVariables=new HashMap<>();
		for(int j=0;j<regionList.size();j++) {
			if(aggregateRegionIdxList.get(i).contains(j)) {
				regionToProjectionVariables.put(j,new ArrayList<>());
				List<Integer> projectionVarIdxList=new ArrayList<>(); 
				for(int k=0;k<projectionVariables.size();k++) {
					if(projectionVariables.get(k).regionList.contains(j)) {
						projectionVarIdxList.add(k);
					}
					
				}
				regionToProjectionVariables.put(j, projectionVarIdxList);
			} 
		}
		// TODO Auto-generated method stub
		return regionToProjectionVariables;
	}

	public List<LinkedList<VariableValuePair>> formAndSolveLP(ConsistencyMakerType consistencyMakerType,FormalCondition[] consistencyConstraints, 
							List<FormalCondition> conditions, Map<String, List<Region>> aggregateColumnsToSingleSplitPointRegions,List<FormalCondition>conditionsAggregate,List<Map<Integer,List<Integer>>> aggregateConditionToRegionsList,List<List<Region>> cliqueIdxtoAggregateRegion,List<List<Integer>> aggregateRegionIdxList, List<List<ProjectionVariable>> cliqueIdxToProjectionRegions, List<Map<Integer, List<Integer>>> regionToProjectionVariablesList) {
		
		
		//  Added by tarun starts
		ArrayList<String> sortedAggCols = new ArrayList<>(aggregateColumnsToSingleSplitPointRegions.keySet());
    	Collections.sort(sortedAggCols);
    	// added by tarun ends
		
		
		/////////////// Start dk
			 
			//  Map<String, String> contextmaptest = new HashMap<>();
			//  contextmaptest.put("model", "true");
			//  contextmaptest.put("unsat_core", "true");
			//  Context ctxtest = new Context(contextmaptest);
			//
			//  Optimize osolver = ctxtest.mkOptimize();
			//  IntExpr x = ctxtest.mkIntConst("x");
			//  IntExpr y = ctxtest.mkIntConst("y");
			//  ArithExpr arithexpr = ctxtest.mkAdd(x, y);
			//  osolver.Add(ctxtest.mkGt(arithexpr, ctxtest.mkInt(10)));
			//  osolver.Add(ctxtest.mkLt(arithexpr, ctxtest.mkInt(20)));
			//  
			//  osolver.MkMaximize(arithexpr);
			//  
			//  osolver.Check();
			//  
			//  Model modeltest = osolver.getModel();
			//  System.out.println(modeltest.evaluate(x, true) + " : " + modeltest.evaluate(y, true));
			///////////////// End dk
			  
    	
    	
    	
    	
    	
    	
    	
    	
    	
			  StopWatch onlyFormationSW = new StopWatch("LP-OnlyFormation" + viewname);
			  
			  Map<String, String> contextmap = new HashMap<>();
			  contextmap.put("model", "true");
			  contextmap.put("unsat_core", "true");
			  Context ctx = new Context(contextmap);
			
			//Solver solver = ctx.mkSolver();
			  Optimize solver = ctx.mkOptimize();
			  
			  //adding expression for optimization function -- Anupam
			  //start
			  //ArithExpr exp_final = ctx.mkIntConst("");
			  //stop
			  
			  List<List<List<IntExpr>>> solverConstraintsRequiredForConsistency = new ArrayList<>();
			  List<HashMap<String, ProjectionStuffInColumn>> cliqueWColumnWProjectionStuff = new ArrayList<>();
			  
			  // Create lp variables for cardinality constraints
			  for (int cliqueIndex = 0; cliqueIndex < cliqueCount; cliqueIndex++) {
			      LongList bvalues = cliqueIdxtoConditionBValuesList.get(cliqueIndex);
			      Partition partition = cliqueIdxtoPList.get(cliqueIndex);		// Partition is a list of regions corresponding to below labels
			      List<IntList> conditionIdxsList = cliqueIdxtoPSimplifiedList.get(cliqueIndex);	// Getting label list for this clique
			
			      Map<Integer,List<Integer>> regionToProjectionVariables=regionToProjectionVariablesList.get(cliqueIndex);
			      
			      HashMap<Integer, Integer> indexKeeper=null;
			      int solverConstraintSize;
			      
			      if (consistencyMakerType == ConsistencyMakerType.CONSISTENCYFILTERS) {
			      	if(cliqueCount>1) {
			    	  indexKeeper = mappedIndexOfConsistencyConstraint.get(cliqueIndex);
			      		solverConstraintSize = bvalues.size() + indexKeeper.size();
			      	}
			      	else 
			      		solverConstraintSize = bvalues.size() ;
			      }
			      else {
			      	indexKeeper = new HashMap<>();
			      	solverConstraintSize = bvalues.size() + cliqueWiseTotalSingleSplitPointRegions.get(cliqueIndex);
			      }
			      
			      List<List<IntExpr>> solverConstraints = new ArrayList<>(solverConstraintSize);
			      for (int j = 0; j < solverConstraintSize; j++) {
			          solverConstraints.add(new ArrayList<>());
			      }
			      
			    //Adding projection nonnegativity
			      for (int j=0;j<cliqueIdxToProjectionRegions.get(cliqueIndex).size();j++) {
			    	  String varname = "y" + cliqueIndex + "_" + j;
			          solverStats.solverVarCount++;
			          
			          solver.Add(ctx.mkGe(ctx.mkIntConst(varname), ctx.mkInt(0)));
			      }
			      
			      for (int blockIndex = 0; blockIndex < partition.size(); blockIndex++) {
			          String varname = "var" + cliqueIndex + "_" + blockIndex;
			          solverStats.solverVarCount++;
			
			        //adding expression for computing l2 norm -- Anupam
			          //start
			          //IntExpr expr = ctx.mkIntConst(varname);
			          //L1 norm minimization
			          //exp_final = ctx.mkAdd(exp_final, expr);
			          //L2 norm minimization
			          //exp_final = ctx.mkAdd(exp_final, ctx.mkMul(expr, expr));
			          //stop
			          
			          
			          
			          //Adding non-negativity constraints
			          solver.Add(ctx.mkGe(ctx.mkIntConst(varname), ctx.mkInt(0)));
			          
			          //adding projection variable greater than 0 
			          
//			          
			          if(regionToProjectionVariables.containsKey(blockIndex)) {
			        	  List<IntExpr>projectionExpr=new ArrayList<>();
			        	  for (Integer projVarIndx:regionToProjectionVariables.get(blockIndex)) {
			        		  projectionExpr.add(ctx.mkIntConst("y" + cliqueIndex + "_"+projVarIndx));
			        	  }
			        	  solver.Add(ctx.mkGe(ctx.mkMul(ctx.mkInt(relationCardinality),ctx.mkAdd(projectionExpr.toArray(new ArithExpr[projectionExpr.size()]))), ctx.mkIntConst(varname)));
			        	  solver.Add(ctx.mkLe(ctx.mkAdd(projectionExpr.toArray(new ArithExpr[projectionExpr.size()])), ctx.mkIntConst(varname)));
			          } 
//			          
			
			          //Adds the region to all the constraints it belongs to
			          for (IntIterator iter = conditionIdxsList.get(blockIndex).iterator(); iter.hasNext();) {
			              int k = iter.nextInt();
			              solverConstraints.get(k).add(ctx.mkIntConst(varname));
			          }
			          
			          
			          
			      }
			      //Adding normal constraints
			      for (int conditionIndex = 0; conditionIndex < bvalues.size(); conditionIndex++) {
			          long outputCardinality = bvalues.getLong(conditionIndex);
			          List<IntExpr> addList = solverConstraints.get(conditionIndex);
			          solver.Add(ctx.mkEq(ctx.mkAdd(addList.toArray(new ArithExpr[addList.size()])), ctx.mkInt(outputCardinality)));
			          solverStats.solverConstraintCount++;
			      }
			      
			      ///codeS
			      //adding projection constraints
			      Map<Integer, List<Integer>>aggregateConditionToRegions =aggregateConditionToRegionsList.get(cliqueIndex);
			      Map<Integer,List<Integer>>regionToProjectionVar=regionToProjectionVariablesList.get(cliqueIndex);

			      Iterator<Map.Entry<Integer, List<Integer>>> iterator = aggregateConditionToRegions.entrySet().iterator();
			      while (iterator.hasNext()) {
			    	  Map.Entry<Integer, List<Integer>> entry = iterator.next();
			    	  Set<Integer>projRegionList=new HashSet<>();
			    	  for (Integer regionIdx: entry.getValue()) {
			    		  projRegionList.addAll(regionToProjectionVar.get(regionIdx));
			    		  
			    	  }
			    	  List<IntExpr>constraint=new ArrayList<>();
			    	  for (Integer projVarIdx:projRegionList)
			    		  constraint.add(ctx.mkIntConst("y"+cliqueIndex+"_"+projVarIdx));
			    	  
			    	  solver.Add(ctx.mkEq(ctx.mkAdd(constraint.toArray(new ArithExpr[constraint.size()])), ctx.mkInt(((FormalConditionAggregate) (conditionsAggregate.get(entry.getKey()))).getProjectionCardinality())));
			    	  
			      }
			      
	      
			///////////////// Start dk
			      if (cliqueCount>1&&consistencyMakerType == ConsistencyMakerType.CONSISTENCYFILTERS) {
			          List<List<IntExpr>> solverConstraintsToExport = new ArrayList<>(indexKeeper.size());
			          for(int j = bvalues.size(); j < solverConstraintSize; j++) {
			          	solverConstraintsToExport.add(solverConstraints.get(j));
			          }
			          solverConstraintsToExport.add(solverConstraints.get(bvalues.size() - 1));   // Clique size
			          solverConstraintsRequiredForConsistency.add(solverConstraintsToExport);
			      }
			      
			      //commented BY shadab
			      
			      // 1-D projection handling
			      // NOTE: vars (x) are selection variables of LP, aggVars (are also x) are selection variables of LP whose regions fall under some projection condition, projVars (y) are LP variables for projection conditions
//			      List<Pair<Integer, Boolean>> applicableConditions = applicableConditionsOnClique.get(cliqueIndex);
//			  	// Column wise - Set of all vars which satisfy any aggregate condition
//			      HashMap<String, Set<IntExpr>> columnWiseAggVarsInAllAggregateConditions = getColumnWiseAggVarsInAllAggregateConditions(conditions, solverConstraints, applicableConditions);
//			      
//			      int offsetForSingleSplitPointRegions = solverConstraintSize - cliqueWiseTotalSingleSplitPointRegions.get(cliqueIndex);
//					// Column wise - Split point wise - set of all blocks which satisfy any aggregate condition and set of all blocks which does not satisfy any aggregate condition 
//			      HashMap<String, ProjectionStuffInColumn> columnWiseProjectionStuff = 
//			      		getColumnWiseAggAndNonAggVarsInSingleSplitPointRegion(arasuCliques.get(cliqueIndex), solverConstraints, columnWiseAggVarsInAllAggregateConditions, aggregateColumnsToSingleSplitPointRegions, offsetForSingleSplitPointRegions, sortedAggCols);
//			      
//			      for (Entry<String, ProjectionStuffInColumn> entry : columnWiseProjectionStuff.entrySet()) {
//			      	String columnname = entry.getKey();
//			      	ProjectionStuffInColumn projectionStuffInColumn = entry.getValue();
//			      	
//			      	HashMap<List<IntExpr>, IntExpr> containerSetsToProjVar = projectionStuffInColumn.getMapContainerSetsToProjVar();
//			      	HashMap<IntExpr, Set<List<IntExpr>>> aggVarsToContainerSets = projectionStuffInColumn.getMapAggVarsToContainerSets();
//			      	
//			      	Set<IntExpr> aggVarsInAllAggregateConditions = columnWiseAggVarsInAllAggregateConditions.get(columnname);
//			      	if (aggVarsInAllAggregateConditions != null) {
//			          	for (IntExpr aggVar : aggVarsInAllAggregateConditions) {
//			          		aggVarsToContainerSets.put(aggVar, new HashSet<>());
//							}
//			      	} else {
//			      		continue;
//			      	}
//			      	
//			      	// Creating LP variables for required for projection constraints
//			      	for (ProjectionStuffInSSPRegion projectionStuffInSSPRegion : projectionStuffInColumn.getMapSSPRegionToProjectionStuff().values()) {		// for each SSP region
//			      		Set<IntExpr> aggVars = projectionStuffInSSPRegion.getAggVars();
//			      		if (aggVars.size() == 0)
//			      			continue;
//			      		Set<List<IntExpr>> powerSet = getPowerSet(new ArrayList<>(aggVars));
//			      		for (List<IntExpr> set : powerSet) {
//			      			IntExpr projVar = ctx.mkIntConst("y_" + projectionVarsIndex++);
//			      			solver.Add(ctx.mkGe(projVar, ctx.mkInt(0)));
//			      			containerSetsToProjVar.put(set, projVar);
//								for (IntExpr aggVar : set) {
//									aggVarsToContainerSets.get(aggVar).add(set);
//								}
//							}
//			      		
//			      		for (IntExpr aggVar : aggVars) {
//			      			List<IntExpr> projVars = new ArrayList<>();
//			      			for (List<IntExpr> containerSet : aggVarsToContainerSets.get(aggVar)) {
//			      				projVars.add(containerSetsToProjVar.get(containerSet));
//								}
//			      			ArithExpr sumOfProjVars = ctx.mkAdd(projVars.toArray(new ArithExpr[projVars.size()]));
//								solver.Add(ctx.mkGe(aggVar, sumOfProjVars));		// Type 3
//								solver.Add(ctx.mkGe(ctx.mkMul(sumOfProjVars, ctx.mkInt(relationCardinality)), aggVar));	// Type 4
//							}
//						}
//					}
//			      
//			      for (int i = 0; i < applicableConditions.size(); ++i) {
//			      	Pair<Integer, Boolean> applicableConditionInfo = applicableConditions.get(i);
//			      	if (applicableConditionInfo.getSecond()) {	// Is the condition applied with projection or without projection 
//			      		FormalCondition formalCondition = conditions.get(applicableConditionInfo.getFirst());
//							FormalConditionAggregate formalConditionAggregate = (FormalConditionAggregate) formalCondition;
//							List<String> completeGroupKey = formalConditionAggregate.getGroupKey();
//							if (completeGroupKey.size() != 1) {
//								ctx.close();
//								throw new RuntimeException("Only 1-D projection supported!");
//							}
//							String groupKey = completeGroupKey.get(0);
//							ProjectionStuffInColumn projectionStuffInColumn = columnWiseProjectionStuff.get(groupKey);
//							HashMap<IntExpr, List<IntExpr>> aggVarsToProjVars = projectionStuffInColumn.getAggVarsToProjVars();
//							Set<IntExpr> addList = new HashSet<>();
//							for (IntExpr var : solverConstraints.get(i)) {
//								addList.addAll(aggVarsToProjVars.get(var));
//							}
//							ArithExpr sumOfProjVars = ctx.mkAdd(addList.toArray(new ArithExpr[addList.size()]));
//							if (formalConditionAggregate.isTop())
//								solver.Add(ctx.mkEq(sumOfProjVars, ctx.mkInt(formalConditionAggregate.getProjectionCardinality())));			// Type 2
//							else
//							{
//								DirtyCode.throwError("Currently working on sinlge table!");
//			//					solver.Add(ctx.mkGe(sumOfProjVars, ctx.mkInt(formalConditionAggregate.getProjectionCardinality())));			// Type 2
//							}
//						}
//			      }
//			      cliqueWColumnWProjectionStuff.add(columnWiseProjectionStuff);
			///////////////// End dk
			  }
			
			///////////////// Start dk
//			  DebugHelper.printInfo("variablesRequiredForProjection: " + projectionVarsIndex);
			  
			  // Consistency
			  List<ProjectionConsistencyInfoPair> projectionConsistencyInfoPairs = new LinkedList<>();
			  int countConsistencyConstraint = 0;
			  if(cliqueCount>1) {
				  if (consistencyMakerType == ConsistencyMakerType.CONSISTENCYFILTERS) {
				      
				  	if(consistencyConstraints.length != 0 || aggregateColumnsToSingleSplitPointRegions.size() != 0) {
					        for (int c1index = 0; c1index < cliqueCount; c1index++) {
								HashMap<Integer, Integer> c1indexKeeper = mappedIndexOfConsistencyConstraint.get(c1index);
								if(c1indexKeeper.isEmpty())
									continue;
								List<List<IntExpr>> c1solverConstraints = solverConstraintsRequiredForConsistency.get(c1index);
								for (int c2index = c1index + 1; c2index < cliqueCount; c2index++) {
									HashMap<Integer, Integer> c2indexKeeper = mappedIndexOfConsistencyConstraint.get(c2index);
									if(c2indexKeeper.isEmpty())
										continue;
									List<List<IntExpr>> c2solverConstraints = solverConstraintsRequiredForConsistency.get(c2index);
									Set<Integer> applicableConsistencyConstraints = new HashSet<>(c1indexKeeper.keySet());
									applicableConsistencyConstraints.retainAll(c2indexKeeper.keySet());
									if(applicableConsistencyConstraints.isEmpty())
										continue;
									List<List<IntExpr>> c1ApplicableSolverConstraints = new ArrayList<>();
									List<List<IntExpr>> c2ApplicableSolverConstraints = new ArrayList<>();
									for (Integer originalConsistencyConstraintIndex : applicableConsistencyConstraints) {
										c1ApplicableSolverConstraints.add(c1solverConstraints.get(c1indexKeeper.get(originalConsistencyConstraintIndex)));
										c2ApplicableSolverConstraints.add(c2solverConstraints.get(c2indexKeeper.get(originalConsistencyConstraintIndex)));
									}
									
									c1ApplicableSolverConstraints.add(c1solverConstraints.get(c1solverConstraints.size() - 1));
									c2ApplicableSolverConstraints.add(c2solverConstraints.get(c2solverConstraints.size() - 1));
				
									HashMap<IntList, MutablePair<List<IntExpr>, List<IntExpr>>> conditionListToPairOfVarList = new HashMap<>();
									addTo_ConditionListToPairOfVarList(c1ApplicableSolverConstraints, conditionListToPairOfVarList);
									addTo_ConditionListToPairOfVarList(c2ApplicableSolverConstraints, conditionListToPairOfVarList);
									
									Set<String> commonCols = new HashSet<>(arasuCliques.get(c1index));
									commonCols.retainAll(arasuCliques.get(c2index));
									
									for (MutablePair<List<IntExpr>, List<IntExpr>> pair : conditionListToPairOfVarList.values()) {
										List<IntExpr> c1Set = pair.getFirst();
										List<IntExpr> c2Set = pair.getSecond();
										ArithExpr set1expr = ctx.mkAdd(c1Set.toArray(new ArithExpr[c1Set.size()]));
										ArithExpr set2expr = ctx.mkAdd(c2Set.toArray(new ArithExpr[c2Set.size()]));
										solver.Add(ctx.mkEq(set1expr, set2expr));
						                countConsistencyConstraint++;
				
						                // 1-D projection
						                collectProjectionConsistencyData(solver,ctx, c1Set, c2Set, c1index, c2index, commonCols, projectionConsistencyInfoPairs, cliqueWColumnWProjectionStuff);
									}
						        }
					        }
				      }
				  }
				///////////////// End dk
				
				  else if (consistencyMakerType == ConsistencyMakerType.BRUTEFORCE) {
				      for (CliqueIntersectionInfo cliqueIntersectionInfo : cliqueIntersectionInfos) {		// Create lp variables for consistency constraints
				
				          int i = cliqueIntersectionInfo.i;
				          int j = cliqueIntersectionInfo.j;
				          IntList intersectingColIndices = cliqueIntersectionInfo.intersectingColIndices;
				
				          Partition partitionI = cliqueIdxtoPList.get(i);
				          Partition partitionJ = cliqueIdxtoPList.get(j);
				
				          //Recomputing SplitRegions for every pair of intersecting cliques
				          //as the SplitRegions might have become more granular due to
				          //some other pair of intersecting cliques having its intersectingColIndices
				          //overlapping with this pair's intersectingColIndices
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
				
				              //Adding consistency constraints
				              solver.Add(ctx.mkEq(ctx.mkAdd(consistencyLHS.toArray(new ArithExpr[consistencyLHS.size()])),
				                      ctx.mkAdd(consistencyRHS.toArray(new ArithExpr[consistencyRHS.size()]))));
				              solverStats.solverConstraintCount++;
				              countConsistencyConstraint++;
				
				              // 1-D projection
				              collectProjectionConsistencyData(solver,ctx, consistencyLHS, consistencyRHS, i, j, commonCols, projectionConsistencyInfoPairs, cliqueWColumnWProjectionStuff);
				          }
				      }
				  }
				  else {
				  	ctx.close();
				  	throw new RuntimeException("Unknown consistency maker " + consistencyMakerType);
				  }
			  }
			  DebugHelper.printInfo("countConsistencyConstraint for " + viewname + " = " + countConsistencyConstraint);
			  
			  //commented by shadab
			/////////////// Start dk
			  // Consistency for projection: Phase 1
//			  Set<IntExpr> allSlackVars = new HashSet<>();
//			  for (int cliqueIndex = 0; cliqueIndex < cliqueWColumnWProjectionStuff.size(); ++cliqueIndex) {
//			  	HashMap<String, ProjectionStuffInColumn> columnWProjectionStuff = cliqueWColumnWProjectionStuff.get(cliqueIndex);
//			  	for (Entry<String, ProjectionStuffInColumn> entry : columnWProjectionStuff.entrySet()) {
//			  		String columnname = entry.getKey();
//			  		ProjectionStuffInColumn projectionStuff = entry.getValue();
//			  		// All AggVars in an atomic set satisfy the same set of consistency constraints
//						List<Set<IntExpr>> listOfAtomicSetOfNonAggVars = projectionStuff.doPartition_ConsistencyConstraintSetWiseNonAggVars();
//						for (int j = 0; j < listOfAtomicSetOfNonAggVars.size(); ++j) {
//							Set<IntExpr> atomicSetOfNonAggVars = listOfAtomicSetOfNonAggVars.get(j);
//							IntExpr slackVar = ctx.mkIntConst(getSlackVarStringName(cliqueIndex, columnname, j));		// SlackVar value is the number of unique points from Atomic set vars
//							solver.Add(ctx.mkGe(slackVar, ctx.mkInt(0)));
//							solver.Add(ctx.mkGe(ctx.mkAdd(atomicSetOfNonAggVars.toArray(new ArithExpr[atomicSetOfNonAggVars.size()])), slackVar));
//							
//							allSlackVars.add(slackVar);
//						}
//					}
//				}
//			  
//			  for (ProjectionConsistencyInfoPair projectionConsistencyInfoPair : projectionConsistencyInfoPairs) {
//					String columnname = projectionConsistencyInfoPair.columnname;
//					ProjectionConsistencyInfo c1ProjectionConsistencyInfo = projectionConsistencyInfoPair.getFirst();
//					ProjectionConsistencyInfo c2ProjectionConsistencyInfo = projectionConsistencyInfoPair.getSecond();
//					ProjectionStuffInColumn c1ProjectionStuff = cliqueWColumnWProjectionStuff.get(c1ProjectionConsistencyInfo.cindex).get(columnname);
//					ProjectionStuffInColumn c2ProjectionStuff = cliqueWColumnWProjectionStuff.get(c2ProjectionConsistencyInfo.cindex).get(columnname);
//					List<Integer> c1SlackVarsIndexs = c1ProjectionStuff.getSlackVarsIndexsContainedInNonAggVars(c1ProjectionConsistencyInfo.nonAggVars);
//					List<Integer> c2SlackVarsIndexs = c2ProjectionStuff.getSlackVarsIndexsContainedInNonAggVars(c2ProjectionConsistencyInfo.nonAggVars);
//					
//					projectionConsistencyInfoPair.setSlackVarsIndexes(c1SlackVarsIndexs, c2SlackVarsIndexs);
//					
//					List<IntExpr> c1ProjVarsUnionSlackVar = new LinkedList<>(c1ProjectionConsistencyInfo.projVars);
//					for (Integer index : c1SlackVarsIndexs) {
//						c1ProjVarsUnionSlackVar.add(ctx.mkIntConst(getSlackVarStringName(c1ProjectionConsistencyInfo.cindex, columnname, index)));
//					}
//					List<IntExpr> c2ProjVarsUnionSlackVar = new LinkedList<>(c2ProjectionConsistencyInfo.projVars);
//					for (Integer index : c2SlackVarsIndexs) {
//						c2ProjVarsUnionSlackVar.add(ctx.mkIntConst(getSlackVarStringName(c2ProjectionConsistencyInfo.cindex, columnname, index)));
//					}
//					
//					solver.Add(ctx.mkEq(ctx.mkAdd(c1ProjVarsUnionSlackVar.toArray(new ArithExpr[c1ProjVarsUnionSlackVar.size()])), ctx.mkAdd(c2ProjVarsUnionSlackVar.toArray(new ArithExpr[c2ProjVarsUnionSlackVar.size()]))));
//				}
//			  
//			  // 1-D projection: This is not in report. The dVars maximize the difference between nonAggVars and ProjVars so that more slack is available
//			  // While, at the same time, the sum of all slackVars is minimized to prevent generating extra unique points which were originally not required: proj1 + slack1 = proj2 + slack2; 1+1=2+0 is better then 1+2=2+1
//			  int dVarIndex = 0;
//			  List<IntExpr> allDVars = new ArrayList<>();
//			  
//			  for (ProjectionConsistencyInfoPair projectionConsistencyInfoPair : projectionConsistencyInfoPairs) {
//					ProjectionConsistencyInfo c1ProjectionConsistencyInfo = projectionConsistencyInfoPair.getFirst();
//					Set<IntExpr> projVars = c1ProjectionConsistencyInfo.projVars;
//					if (projVars.size() != 0 && c1ProjectionConsistencyInfo.nonAggVars.size() != 0) {
//						IntExpr dVar = ctx.mkIntConst("d_var" + dVarIndex++);
//						allDVars.add(dVar);
//						Set<IntExpr> nonAggVars = new HashSet<>(c1ProjectionConsistencyInfo.nonAggVars);
//						ArithExpr nonAggVarsSum = ctx.mkAdd(nonAggVars.toArray(new ArithExpr[nonAggVars.size()]));
//						ctx.mkEq(dVar, ctx.mkSub(nonAggVarsSum, ctx.mkAdd(projVars.toArray(new ArithExpr[projVars.size()]))));
//					}
//					
//					ProjectionConsistencyInfo c2ProjectionConsistencyInfo = projectionConsistencyInfoPair.getFirst();
//					projVars = c2ProjectionConsistencyInfo.projVars;
//					if (projVars.size() != 0 && c2ProjectionConsistencyInfo.nonAggVars.size() != 0) {
//						IntExpr dVar = ctx.mkIntConst("d_var" + dVarIndex++);
//						allDVars.add(dVar);
//						Set<IntExpr> nonAggVars = new HashSet<>(c2ProjectionConsistencyInfo.nonAggVars);
//						ArithExpr nonAggVarsSum = ctx.mkAdd(nonAggVars.toArray(new ArithExpr[nonAggVars.size()]));
//						ctx.mkEq(dVar, ctx.mkSub(nonAggVarsSum, ctx.mkAdd(projVars.toArray(new ArithExpr[projVars.size()]))));
//					}
//			  }
//			  
//			  ArithExpr objective = null;
//			  if (allDVars.size() > 0) {
//			  	objective = ctx.mkMul(ctx.mkInt(-1), ctx.mkAdd(allDVars.toArray(new ArithExpr[allDVars.size()])));
//			  	if (allSlackVars.size() > 0)
//			  		objective = ctx.mkAdd(objective, ctx.mkAdd(allSlackVars.toArray(new ArithExpr[allSlackVars.size()])));
//			  }
//			  
//			  if (objective != null)
//			  	solver.MkMinimize(objective);
			  
			///////////////// End dk
			  
			  // Adding an optimization function to the solver -- Anupam
			  //start
			  //solver.MkMinimize(exp_final);
			  //stop
			  
			  
			  
			  onlyFormationSW.displayTimeAndDispose();
			  
			  //Dumping LP into a file -- Anupam
			  //start
			  FileWriter fw;
				try {
					fw = new FileWriter("lpfile-"+viewname+".txt");
					fw.write(solver.toString());
			      fw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			  //System.err.println(solver.toString());
			  //stop
			  
			  StopWatch onlySolvingSW = new StopWatch("LP-OnlySolving" + viewname);
			  
			  Status solverStatus = solver.Check();
			  DebugHelper.printInfo("Solver Status: " + solverStatus);
			
			  if (!Status.SATISFIABLE.equals(solverStatus)) {
			  	ctx.close();
			      throw new RuntimeException("solverStatus is not SATISFIABLE");
			  }
			
			  Model model = solver.getModel();
			
			  List<LinkedList<VariableValuePair>> cliqueIdxToVarValuesList = new ArrayList<>(cliqueCount);
			  for (int i = 0; i < cliqueCount; i++) {
			
			      IntList colIndxs = arasuCliquesAsColIndxs.get(i);
			      Partition partition = cliqueIdxtoPList.get(i);
			      LinkedList<VariableValuePair> varValuePairs = new LinkedList<>();
			      for (int j = 0; j < partition.size(); j++) {
			          String varname = "var" + i + "_" + j;
			          
			          //Variable to column indices mapping -- Anupam
			          //start
			          FileWriter fw1;
			  		try {
			  			fw1 = new FileWriter(viewname+"-var-to-colindices.txt", true);
			  			fw1.write(varname+" "+colIndxs.toString()+ "\n");
			  	        fw1.close();
			  		} catch (IOException e) {
			  			// TODO Auto-generated catch block
			  			e.printStackTrace();
			  		} 
			  		//stop
			          //System.err.println(varname+colIndxs.toString());
			          long rowcount = Long.parseLong(model.evaluate(ctx.mkIntConst(varname), true).toString());
			        //Variable to value mapping -- Anupam
			          //start
			          FileWriter fw2;
			  		try {
			  			fw2 = new FileWriter(viewname+"-var-to-value.txt", true);
			  			fw2.write(varname+" "+rowcount+ "\n");
			  	        fw2.close();
			  		} catch (IOException e) {
			  			// TODO Auto-generated catch block
			  			e.printStackTrace();
			  		} 
			  		//stop
			          if (rowcount != 0) {
			              Region variable = getTruncatedRegion(partition.at(j), colIndxs);
			              VariableValuePair varValuePair = new VariableValuePair(variable, rowcount);
			              varValuePairs.add(varValuePair);
			          }
			      }
			      cliqueIdxToVarValuesList.add(varValuePairs);
			  }
			  
			  onlySolvingSW.displayTimeAndDispose();
			/*        
			  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			  // DIRTY START DIRTY START DIRTY START DIRTY START DIRTY START DIRTY START DIRTY START DIRTY START DIRTY START DIRTY START DIRTY START 
			  
			  StopWatch onlyPhase2SW = new StopWatch("onlyPhase2SW " + viewname);
			  
			  HashMap<String, HashMap<Region, List<ProjectionConsistencyInfoPair>>> columnWSSPRegionToAllProjectionConsistencyInfoPairs = new HashMap<>();
			  for (ProjectionConsistencyInfoPair projectionConsistencyInfoPair : projectionConsistencyInfoPairs) {
			  	HashMap<Region, List<ProjectionConsistencyInfoPair>> sspRegionToAllProjectionConsistencyInfoPairs = columnWSSPRegionToAllProjectionConsistencyInfoPairs.get(projectionConsistencyInfoPair.columnname);
			  	if (sspRegionToAllProjectionConsistencyInfoPairs == null) {
			  		sspRegionToAllProjectionConsistencyInfoPairs = new HashMap<>();
			  		columnWSSPRegionToAllProjectionConsistencyInfoPairs.put(projectionConsistencyInfoPair.columnname, sspRegionToAllProjectionConsistencyInfoPairs);
			  	}
			  	IntExpr aVar = null;
			  	ProjectionConsistencyInfo c1ProjectionConsistencyInfo = projectionConsistencyInfoPair.getFirst();
			  	if (c1ProjectionConsistencyInfo.aggVars.size() != 0) {
			  		aVar = c1ProjectionConsistencyInfo.aggVars.iterator().next();
			  	} else if (c1ProjectionConsistencyInfo.nonAggVars.size() != 0) {
			  		aVar = c1ProjectionConsistencyInfo.nonAggVars.iterator().next();
			  	} else
			  		DirtyCode.throwError("Should not happen!");
			
			  	HashMap<Region, ProjectionStuffInSSPRegion> sspRegionToProjectionStuff = cliqueWColumnWProjectionStuff.get(projectionConsistencyInfoPair.getFirst().cindex).get(projectionConsistencyInfoPair.columnname).getMapSSPRegionToProjectionStuff();
			  	List<ProjectionConsistencyInfoPair> listOfProjectionConsistencyInfoPairs = getCorrespondingRegionList(sspRegionToAllProjectionConsistencyInfoPairs, sspRegionToProjectionStuff, aVar);
					listOfProjectionConsistencyInfoPairs.add(projectionConsistencyInfoPair);
				}
			  
			  for (int i = 0; i < cliqueCount; i++) {
			  	HashMap<String, ProjectionStuffInColumn> columnWiseProjectionStuff = cliqueWColumnWProjectionStuff.get(i);
					for (ProjectionStuffInColumn projectionStuffInColumn : columnWiseProjectionStuff.values()) {
						projectionStuffInColumn.updateAggVarToProjVarOfProjectionStuffInSSPRegion();
					}
			  }
			
				// Assigning intervals to projVars and finding MaxUniqePoints per sspRegion
			  List<HashMap<IntExpr, DirtyValueInterval>> cliqueWiseProjVarToInterval = new ArrayList<>();
			  HashMap<String, HashMap<Region, Long>> columnWSSPRegionToMaxUniqePoints = new HashMap<>();
			  
			  if (false)
			  // heuristic
			  	createProjVarToIntervalAndSSPRegionToMaxUniquePoints_hueristic(model, ctx, cliqueWiseProjVarToInterval, columnWSSPRegionToAllProjectionConsistencyInfoPairs, cliqueWColumnWProjectionStuff, columnWSSPRegionToMaxUniqePoints);
			  // sequential
			  else
			  	createProjVarToIntervalAndSSPRegionToMaxUniquePoints_sequential(model, ctx, cliqueWiseProjVarToInterval, cliqueWColumnWProjectionStuff, columnWSSPRegionToMaxUniqePoints);
			  
			  List<HashMap<IntExpr, ProjectionValues>> cliqueWAggAndNonAggVarsToProjectionValues= new ArrayList<>(cliqueCount);
			  for (int i = 0; i < cliqueCount; ++i)
			  	cliqueWAggAndNonAggVarsToProjectionValues.add(new HashMap<>());
			  
			  // Projection consistency Phase 2
			  for (Entry<String, HashMap<Region, List<ProjectionConsistencyInfoPair>>> outerEntry : columnWSSPRegionToAllProjectionConsistencyInfoPairs.entrySet()) {
					String columnname = outerEntry.getKey();
					HashMap<Region, List<ProjectionConsistencyInfoPair>> sspRegionToAllProjectionConsistencyInfoPairs = outerEntry.getValue();
					HashMap<Region, Long> sspRegionToMaxUniqePoints = columnWSSPRegionToMaxUniqePoints.get(columnname);
			  	int colIndx = sortedViewColumns.indexOf(columnname);
					for (Entry<Region, List<ProjectionConsistencyInfoPair>> innerEntry : sspRegionToAllProjectionConsistencyInfoPairs.entrySet()) {
						Region region = innerEntry.getKey();
						Long maxUniqePoints = sspRegionToMaxUniqePoints.get(region);
						if (maxUniqePoints == 0)
							continue;
						
						// TODO: Problem: LongIndexNeeded : Need to change this code because using long array indexes!
						long maxint = Integer.MAX_VALUE;
						if (maxUniqePoints.longValue() >= maxint)
							DirtyCode.throwError("problem");
						
						List<ProjectionConsistencyInfoPair> listProjectionConsistencyInfoPairs = innerEntry.getValue();
						
						Map<String, String> contextmapTemp = new HashMap<>();
				        contextmapTemp.put("model", "true");
				        contextmapTemp.put("unsat_core", "true");
				        Context ctxPhase2 = new Context(contextmapTemp);
				        Solver solverPhase2 = ctxPhase2.mkSolver();
				        
				        IntNum zero = ctxPhase2.mkInt(0);
				        
				        List<List<IntExpr>> c1PointWListOfNewVars = new ArrayList<>((int)maxUniqePoints.longValue());			// value of point - list of vars
				        List<List<IntExpr>> c2PointWListOfNewVars = new ArrayList<>((int)maxUniqePoints.longValue());			// value of point - list of vars
						for(int i = 0; i < (int)maxUniqePoints.longValue(); ++i) {
							c1PointWListOfNewVars.add(new ArrayList<>());
							c2PointWListOfNewVars.add(new ArrayList<>());
						}
						
				        for (ProjectionConsistencyInfoPair pInfoPair : listProjectionConsistencyInfoPairs) {
				        	ProjectionConsistencyInfo c1ProjectionConsistencyInfo = pInfoPair.getFirst();
				        	ProjectionConsistencyInfo c2ProjectionConsistencyInfo = pInfoPair.getSecond();
				        	createPointWiseConstraints(model, solverPhase2, ctxPhase2, c1ProjectionConsistencyInfo, cliqueWColumnWProjectionStuff, cliqueWiseProjVarToInterval, c1PointWListOfNewVars, columnname, maxUniqePoints);
				        	createPointWiseConstraints(model, solverPhase2, ctxPhase2, c2ProjectionConsistencyInfo, cliqueWColumnWProjectionStuff, cliqueWiseProjVarToInterval, c2PointWListOfNewVars, columnname, maxUniqePoints);
			
				    		for(int i = 0; i < (int)maxUniqePoints.longValue(); ++i) {
				    			List<IntExpr> c1NewVars = c1PointWListOfNewVars.get(i);
				    			List<IntExpr> c2NewVars = c2PointWListOfNewVars.get(i);
				    			
				    			if (c1NewVars.isEmpty() && c2NewVars.isEmpty())
				    				continue;
				    			if (c1NewVars.isEmpty() || c2NewVars.isEmpty()) {
				    				if (c1NewVars.isEmpty() && containsOnlySlacks(c2NewVars))
				    					c1NewVars.add(zero);
				    				else if (c2NewVars.isEmpty() && containsOnlySlacks(c1NewVars))
				    					c2NewVars.add(zero);
				    				else
				    					DirtyCode.throwError("Phase 2: No space to multiply points!");
				    			}
				    			
				    			solverPhase2.add(ctxPhase2.mkEq(ctxPhase2.mkAdd(c1NewVars.toArray(new ArithExpr[c1NewVars.size()])), ctxPhase2.mkAdd(c2NewVars.toArray(new ArithExpr[c2NewVars.size()]))));
				    		}
				        }
				        
				        /////
				        long numberOfVars = 0;
				        for (List<IntExpr> list : c1PointWListOfNewVars) {
							numberOfVars += list.size();
						}
				        for (List<IntExpr> list : c2PointWListOfNewVars) {
							numberOfVars += list.size();
						}
				        System.out.println("Number of variables for phase 2: " + numberOfVars + " , maxUniqePoints: " + maxUniqePoints);
				        /////
				        
						
						if (solverPhase2.check() != Status.SATISFIABLE)
							DirtyCode.throwError("Phase 2 Unsatisfiable!!");
						Model modelTemp = solverPhase2.getModel();
				        
						// Assigning projection intervals with number of occurrences (count) to regions (vars)
						// To AggVars
						for (int cindex = 0; cindex < cliqueCount; ++cindex) {
							ProjectionStuffInColumn projectionStuffInColumn = cliqueWColumnWProjectionStuff.get(cindex).get(columnname);
							if (projectionStuffInColumn == null)
								continue;
							ProjectionStuffInSSPRegion projectionStuffInSSPRegion = projectionStuffInColumn.getProjectionStuffInSSPRegion(region);
							HashMap<IntExpr, DirtyValueInterval> projVarToInterval = cliqueWiseProjVarToInterval.get(cindex);
							HashMap<IntExpr, ProjectionValues> varsToProjectionValues = cliqueWAggAndNonAggVarsToProjectionValues.get(cindex);
				        	createAggVarsToProjectionValues(model, ctxPhase2, modelTemp, projectionStuffInSSPRegion, projVarToInterval, columnname, maxUniqePoints, varsToProjectionValues, colIndx, cindex);
						}
						
						List<Set<Integer>> cliqueWSlackVarsIndexes = new ArrayList<>(cliqueCount);
						for (int i = 0; i < cliqueCount; ++i)
							cliqueWSlackVarsIndexes.add(new HashSet<>());
						
						for (ProjectionConsistencyInfoPair pInfoPair : listProjectionConsistencyInfoPairs) {
				        	ProjectionConsistencyInfo c1ProjectionConsistencyInfo = pInfoPair.getFirst();
				        	cliqueWSlackVarsIndexes.get(c1ProjectionConsistencyInfo.cindex).addAll(c1ProjectionConsistencyInfo.slackVarsIndexes);
				        	ProjectionConsistencyInfo c2ProjectionConsistencyInfo = pInfoPair.getSecond();
				        	cliqueWSlackVarsIndexes.get(c2ProjectionConsistencyInfo.cindex).addAll(c2ProjectionConsistencyInfo.slackVarsIndexes);
						}
						
						// To NonAggVars
						for (int cindex = 0; cindex < cliqueCount; ++cindex) {
							ProjectionStuffInColumn projectionStuffInColumn = cliqueWColumnWProjectionStuff.get(cindex).get(columnname);
							if (projectionStuffInColumn == null)
								continue;
							HashMap<IntExpr, ProjectionValues> varsToProjectionValues = cliqueWAggAndNonAggVarsToProjectionValues.get(cindex);
							Set<Integer> slackVarsIndexes = cliqueWSlackVarsIndexes.get(cindex);
							createNonAggVarsToProjectionValues(model, ctxPhase2, modelTemp, projectionStuffInColumn, slackVarsIndexes, columnname, maxUniqePoints, varsToProjectionValues, colIndx, cindex);
						}
					}
				}
			  
			  onlyPhase2SW.displayTimeAndDispose();
			  
			  // Assigning intervals to aggVars of those columns which have not taken part in any of the consistency constraints
			  Set<String> projColsTakingPartInConsistency = columnWSSPRegionToAllProjectionConsistencyInfoPairs.keySet();
			  Set<String> projColsNotTakingPartInConsistency = columnWSSPRegionToMaxUniqePoints.keySet();
			  projColsNotTakingPartInConsistency.removeAll(projColsTakingPartInConsistency);
			  for (int cIndex = 0; cIndex < cliqueCount; ++cIndex) {
			  	HashMap<String, ProjectionStuffInColumn> columnWProjectionStuff = cliqueWColumnWProjectionStuff.get(cIndex);
			  	HashMap<IntExpr, ProjectionValues> varsToProjectionValues = cliqueWAggAndNonAggVarsToProjectionValues.get(cIndex);
			  	HashMap<IntExpr, DirtyValueInterval> projVarToInterval = cliqueWiseProjVarToInterval.get(cIndex);
			  	for (String columnname : projColsNotTakingPartInConsistency) {
			  		ProjectionStuffInColumn projectionStuffInColumn = columnWProjectionStuff.get(columnname);
			  		if (projectionStuffInColumn == null)
			  			continue;
			  		int colIndx = sortedViewColumns.indexOf(columnname);
			  		for (Entry<IntExpr, List<IntExpr>> entry : projectionStuffInColumn.getAggVarsToProjVars().entrySet()) {
							IntExpr aggVar = entry.getKey();
			  			long rowcount = Long.parseLong(model.evaluate(aggVar, true).toString());
			  			if (rowcount > 0) {
			  				long leftover = rowcount;
			  				List<IntExpr> projVars = entry.getValue();
			  				ProjectionValues projectionValues = getOrAdd(varsToProjectionValues, aggVar);
			  				Iterator<IntExpr> it = projVars.iterator();
			  				DirtyValueInterval interval = null;
			  				do {
			  					interval = projVarToInterval.get(it.next());
			  				} while (interval == null);
			  				long firstStart = interval.start;
			  				long intervalSizeExceptFirstElement = interval.end - (interval.start + 1);		// Will repeat first element
			  				if (intervalSizeExceptFirstElement > 0) {
			  					projectionValues.addToList(colIndx, interval.start + 1, interval.end, 1);
			  					leftover -= intervalSizeExceptFirstElement;
			  				}
			  				while (it.hasNext()) {
			  					interval = projVarToInterval.get(it.next());
			  					if (interval != null) {
			  						projectionValues.addToList(colIndx, interval.start, interval.end, 1);
			  						leftover -= interval.end - interval.start;
			  					}
			  				}
			  				projectionValues.addToList(colIndx, firstStart, firstStart + 1, leftover);	// Repeating first element leftover times
			  			}
						}
					}
			  }
			  
			  // Creating region to Value list
			  List<LinkedList<DirtyVariableValuePairWithProjectionValues>> dirtyCliqueIdxToVarValuesList = new ArrayList<>(cliqueCount);
			  for (int i = 0; i < cliqueCount; i++) {
			      IntList colIndxs = arasuCliquesAsColIndxs.get(i);
			      Partition partition = cliqueIdxtoPList.get(i);
			  	HashMap<IntExpr, ProjectionValues> aggAndNonAggVarsToProjectionValues = cliqueWAggAndNonAggVarsToProjectionValues.get(i);
					
			      LinkedList<DirtyVariableValuePairWithProjectionValues> varValuePairs = new LinkedList<>();
			      
			      for (int j = 0; j < partition.size(); j++) {
			          String varname = "var" + i + "_" + j;
						IntExpr var = ctx.mkIntConst(varname);
			          long rowcount = Long.parseLong(model.evaluate(var, true).toString());
			          if (rowcount != 0) {
			          	ProjectionValues projectionValues = aggAndNonAggVarsToProjectionValues.get(var);	// Could be null because of nonAggVars
			              Region region = getTruncatedRegion(partition.at(j), colIndxs);
			              DirtyVariableValuePairWithProjectionValues varValuePair = new DirtyVariableValuePairWithProjectionValues(region, rowcount, projectionValues);
			              varValuePair.doSanityCheck(colIndxs);
			              varValuePairs.add(varValuePair);
			          }
			      }
			      dirtyCliqueIdxToVarValuesList.add(varValuePairs);
			  }
			
			  IntList dirtyMergedColIndxs = new IntArrayList();
			  List<DirtyValueCombinationWithProjectionValues> dirtyMergedValueCombinations = new ArrayList<>();
			  dirtyMergedColIndxs.addAll(arasuCliquesAsColIndxs.get(0));
			
			  //Instantiating variables of first clique
			  for (DirtyVariableValuePairWithProjectionValues varValuePair : dirtyCliqueIdxToVarValuesList.get(0)) {
			      IntList columnValues = getFloorInstantiation(varValuePair.variable);
			      DirtyValueCombinationWithProjectionValues valueCombination = new DirtyValueCombinationWithProjectionValues(columnValues, varValuePair.rowcount, varValuePair.getProjectionValues());
			      dirtyMergedValueCombinations.add(valueCombination);
			  }
			  
			  // merging with other cliques
			  for (int i = 1; i < cliqueCount; i++) {
			      dirtyMergeWithAlignment(dirtyMergedColIndxs, dirtyMergedValueCombinations, arasuCliquesAsColIndxs.get(i), dirtyCliqueIdxToVarValuesList.get(i));
			  }
			  
			  
			  for (ListIterator<DirtyValueCombinationWithProjectionValues> iter = dirtyMergedValueCombinations.listIterator(); iter.hasNext();) {
			  	DirtyValueCombinationWithProjectionValues dirtyValueCombination = iter.next();
			  	dirtyValueCombination = new DirtyValueCombinationWithProjectionValues(getReverseMappedValueCombination(dirtyValueCombination), dirtyValueCombination.getProjectionValues());
			  	dirtyValueCombination = new DirtyValueCombinationWithProjectionValues(getExpandedValueCombination(dirtyValueCombination), dirtyValueCombination.getProjectionValues());
			      iter.set(dirtyValueCombination);
			  }
			  
			  // Verifying count per agg condition
			  List<FormalCondition> allAggConditions = new ArrayList<>();
				for (FormalCondition formalCondition : conditions) {
					if (formalCondition instanceof FormalConditionAggregate)
						allAggConditions.add(formalCondition);
				}
				
				DirtyCode.debugSolvingErrorPerConditionForProjections(allAggConditions, dirtyMergedValueCombinations, sortedViewColumns);
				
			  /*
			  DirtyViewSolution viewSolution = new DirtyViewSolution(mergedValueCombinations);
			  DirtyDatabaseSummary.getInstance().addSummary(viewname, viewSolution);
			  /*
			  
			  // DIRTY END DIRTY END DIRTY END DIRTY END DIRTY END DIRTY END DIRTY END DIRTY END DIRTY END DIRTY END DIRTY END DIRTY END DIRTY END 
			  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			*/
			  ctx.close();
			  return cliqueIdxToVarValuesList;
		
	}
	
	//codeS
	//**********Helper functions for projection by shadab start******************//
	
	public boolean isAggregateRegion(Region currRegion, List<Region> conditionAggregateRegions) {
		// TODO Auto-generated method stub
		for (int i =0;i<conditionAggregateRegions.size();i++) {
			Region conditionRegion=conditionAggregateRegions.get(i);
			if(currRegion.minus(conditionRegion).isEmpty())
				return true;
		}
		return false;
	}


	private List<ProjectionVariable> getProjectionRegions(List<Integer> aggregateRegionIdx,List<String> projectionColumns,int cliqueIdx) {
		
		List<ProjectionVariable> projectionVariableList=new ArrayList<>(); 
		List<ProjectionVariable> prevProjectionVariableList=new ArrayList<>();
		List<Integer> projectionColumnsIdx=new ArrayList<>();
		for(String projectionColumn:projectionColumns) {
			projectionColumnsIdx.add(sortedViewColumns.indexOf(projectionColumn));
		}
		for (Integer regionId:aggregateRegionIdx) {
			projectionVariableList.add(new ProjectionVariable(regionId));
			prevProjectionVariableList.add(new ProjectionVariable(regionId));
		}
		int maxLength=aggregateRegionIdx.size();
		for(int k=1;k<=maxLength-1;k++) {
			List<ProjectionVariable> currProjectionVariableList=new ArrayList<>(); 
			
			for(int i=0;i<prevProjectionVariableList.size();i++) {
				for (int j=i+1;j<prevProjectionVariableList.size();j++) {
					ProjectionVariable newVariable=areCompatible(prevProjectionVariableList.get(i),prevProjectionVariableList.get(j),projectionColumnsIdx,k,cliqueIdx);
					if(newVariable!=null) {
						currProjectionVariableList.add(newVariable);
					}
					
				}
				
			}
			prevProjectionVariableList.clear();
			prevProjectionVariableList.addAll(currProjectionVariableList);
			projectionVariableList.addAll(currProjectionVariableList);
			
			if(currProjectionVariableList.size()==0)
				break;
		}
		return projectionVariableList;
		
	}

	private ProjectionVariable areCompatible(ProjectionVariable projectionVariable1, ProjectionVariable projectionVariable2,
			List<Integer> projectionColumnsIdx,  int len,int cliqueIdx) {
		// TODO Auto-generated method stub
		List<Integer>regionList1=projectionVariable1.regionList;
		List<Integer>regionList2=projectionVariable2.regionList;
		
		for (int i =0;i<len-1;i++) {
			if(!(regionList1.get(i)==regionList2.get(i)))
			{
//				flag=false;
//				break;
				return null;
			}
		}
		if(doOverlap(projectionVariable1.regionList.get(len-1),projectionVariable2.regionList.get(len-1),projectionColumnsIdx,cliqueIdx)) {
		
			ProjectionVariable newProjectionVariable=new ProjectionVariable();
			newProjectionVariable.regionList.addAll(regionList1);
			newProjectionVariable.regionList.remove(len-1);
			//System.out.println(regionList1.get(len-1)+regionList2.get(len-1) );
			newProjectionVariable.regionList.add(Math.min(regionList1.get(len-1),regionList2.get(len-1) ));
			newProjectionVariable.regionList.add(Math.max(regionList1.get(len-1),regionList2.get(len-1) ));
			return newProjectionVariable;
		}
		return null;
		
	}

	private boolean doOverlap(Integer regionIdx1, Integer regionIdx2, List<Integer> projectionColumnsIdx, int cliqueIdx) {
		// TODO Auto-generated method stub
		Region projectionRegion1=new Region();
		Region projectionRegion2=new Region();
		Region region1=cliqueIdxtoPList.get(cliqueIdx).getAll().get(regionIdx1);
		Region region2=cliqueIdxtoPList.get(cliqueIdx).getAll().get(regionIdx2);
		
		projectionRegion1=projectRegion(region1,projectionColumnsIdx);
		projectionRegion2=projectRegion(region2,projectionColumnsIdx);
		if(projectionRegion1.intersection(projectionRegion2).size()!=0)
			return true;
		return false;
	}

	private Region projectRegion(Region region1, List<Integer> projectionColumnsIdx) {
		// TODO Auto-generated method stub
		Region projectedRegion=new Region();
		for(BucketStructure bs: region1.getAll()) {
			BucketStructure bsNew=new BucketStructure();
			for(int i=0;i<bs.size();i++) {
				if(projectionColumnsIdx.contains(i))
					bsNew.add(bs.at(i));
			}
			projectedRegion.add(bsNew);
		}
		return projectedRegion;
	}
	
	 public Map<Integer,List<Integer>> getAggregateToRegionMap(List<Region> aggregateConditionRegions,List<Region>regionList){
	    	Map<Integer,List<Integer>> aggregateConditionToRegions=new HashMap<>();
	    	for (int j=0;j<aggregateConditionRegions.size();j++) {
				Region cRegion=aggregateConditionRegions.get(j);
				List<Integer> regionIdTemp=new ArrayList<>();
				//for (Region region:regionList) {
				for(int k=0;k<regionList.size();k++) {
					if(regionList.get(k).minus(cRegion).isEmpty())//checksif region is contained in cRegion
					{
						
						regionIdTemp.add(k);
					}
				}
				if(regionIdTemp.size()!=0) {
					aggregateConditionToRegions.put(j, regionIdTemp);
				}	
	    	}
	    	return aggregateConditionToRegions;
	    }
	
	//**********Helper functions for projection by shadab end******************//

	private void createProjVarToIntervalAndSSPRegionToMaxUniquePoints_sequential(Model model, Context ctx, List<HashMap<IntExpr, DirtyValueInterval>> cliqueWiseProjVarToInterval, 
			List<HashMap<String, ProjectionStuffInColumn>> cliqueWColumnWProjectionStuff, HashMap<String, HashMap<Region, Long>> columnWSSPRegionToMaxUniqePoints) {
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
				for (Entry<Region, ProjectionStuffInSSPRegion> regionAndProjectionStuff : projectionStuffInColumn.getMapSSPRegionToProjectionStuff().entrySet()) {
					Region region = regionAndProjectionStuff.getKey();
					ProjectionStuffInSSPRegion projectionStuff = regionAndProjectionStuff.getValue();
					Set<IntExpr> projVarsInSSPRegion = projectionStuff.getAllProjVars();
					long start = 0;
					long end = 0;	// excluding end
					
					for (IntExpr projVar : projVarsInSSPRegion) {
						long count = Long.parseLong(model.evaluate(projVar, true).toString());
						if (count != 0) {
							end += count;
							projVarToInterval.put(projVar, new DirtyValueInterval(start, end));
							start = end;
						}
					}
					
					if(!sspRegionToMaxUniqePoints.containsKey(region))
						sspRegionToMaxUniqePoints.put(region, end);
					else {
						long maxEnd = sspRegionToMaxUniqePoints.get(region);
						if (end > maxEnd)
							sspRegionToMaxUniqePoints.put(region, end);
					}
					
					
					long maxint = Integer.MAX_VALUE;
					if (end >= maxint)	// Search "LongIndexNeeded" in this code
						DirtyCode.throwError("problem");
				}
			}
			cliqueWiseProjVarToInterval.add(projVarToInterval);
		}
	}

	private void createProjVarToIntervalAndSSPRegionToMaxUniquePoints_hueristic(Model model, Context ctx, List<HashMap<IntExpr, DirtyValueInterval>> cliqueWiseProjVarToInterval, HashMap<String, HashMap<Region,
			List<ProjectionConsistencyInfoPair>>> columnWSSPRegionToAllProjectionConsistencyInfoPairs, 
			List<HashMap<String, ProjectionStuffInColumn>> cliqueWColumnWProjectionStuff, HashMap<String, HashMap<Region, Long>> columnWSSPRegionToMaxUniqePoints) {
		HashMap<String, HashMap<Region, List<LinkedList<DirtyValueInterval>>>> columnWRegionWCliqueWAvailableIntervals = new HashMap<>();
		for (int i = 0; i < cliqueCount; ++i) {
			cliqueWiseProjVarToInterval.add(new HashMap<>());
		}
		
		// Assigning intervals to projVars which take part in any consistency constraint
        for (Entry<String, HashMap<Region, List<ProjectionConsistencyInfoPair>>> outerEntry : columnWSSPRegionToAllProjectionConsistencyInfoPairs.entrySet()) {
        	String columnname = outerEntry.getKey();
        	HashMap<Region, List<ProjectionConsistencyInfoPair>> sspRegionToAllProjectionConsistencyInfoPairs = outerEntry.getValue();
        	HashMap<Region, List<LinkedList<DirtyValueInterval>>> regionWCliqueWAvailableIntervals = new HashMap<>();
			for (Entry<Region, List<ProjectionConsistencyInfoPair>> innerEntry : sspRegionToAllProjectionConsistencyInfoPairs.entrySet()) {
				Region region = innerEntry.getKey();
				SimpleGraph<ProjectionConsistencyInfo, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
				for (ProjectionConsistencyInfoPair projectionConsistencyInfoPair : innerEntry.getValue()) {
					ProjectionConsistencyInfo c1ProjectionConsistencyInfo = projectionConsistencyInfoPair.getFirst();
					ProjectionConsistencyInfo c2ProjectionConsistencyInfo = projectionConsistencyInfoPair.getSecond();

					graph.addVertex(c1ProjectionConsistencyInfo);
					graph.addVertex(c2ProjectionConsistencyInfo);
					graph.addEdge(c1ProjectionConsistencyInfo, c2ProjectionConsistencyInfo);
				}
				ConnectivityInspector<ProjectionConsistencyInfo, DefaultEdge> connectivityInspector = new ConnectivityInspector<>(graph);
				List<Set<ProjectionConsistencyInfo>> listOfConnectedProjectionConsistencyInfos = connectivityInspector.connectedSets();
				
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
						HashMap<IntExpr, DirtyValueInterval> projVarToInterval = cliqueWiseProjVarToInterval.get(projectionConsistencyInfo.cindex);
						for (IntExpr projVar : projectionConsistencyInfo.projVars) {
							DirtyValueInterval interval = projVarToInterval.get(projVar);
							if (interval != null)
								DirtyCode.mergeValueInterval(existingIntervalsAcrossOtherCliques, interval);
						}
					}
					
					LinkedList<DirtyValueInterval> availableIntervalsOfInfoWithZeroSlack = cliqueWAvailableIntervals.get(infoWithZeroSlack.cindex);
					DirtyCode.intersectWithIntervals(existingIntervalsAcrossOtherCliques, availableIntervalsOfInfoWithZeroSlack, relationCardinality);

					HashMap<IntExpr, DirtyValueInterval> projVarToIntervalOfInfoWithZeroSlack = cliqueWiseProjVarToInterval.get(infoWithZeroSlack.cindex);
					for (IntExpr projVar : infoWithZeroSlack.projVars) {
						if (!projVarToIntervalOfInfoWithZeroSlack.containsKey(projVar)) {
							long count = Long.parseLong(model.evaluate(projVar, true).toString());
							if (count != 0 ) {
								DirtyValueInterval bestInterval = DirtyCode.getSuitableIntervalForProjVar(count, availableIntervalsOfInfoWithZeroSlack, existingIntervalsAcrossOtherCliques);
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
						LinkedList<DirtyValueInterval> availableIntervals = cliqueWAvailableIntervals.get(projectionConsistencyInfo.cindex);
						HashMap<IntExpr, DirtyValueInterval> projVarToInterval = cliqueWiseProjVarToInterval.get(projectionConsistencyInfo.cindex);
						LinkedList<DirtyValueInterval> existingIntervals = DirtyCode.createCopyIntervals(existingIntervalsInInfoWithZeroSlack);
						DirtyCode.intersectWithIntervals(existingIntervals, availableIntervals, relationCardinality);
						for (IntExpr projVar : projectionConsistencyInfo.projVars) {
							if (!projVarToInterval.containsKey(projVar)) {
								long count = Long.parseLong(model.evaluate(projVar, true).toString());
								if (count != 0 ) {
									DirtyValueInterval bestInterval = DirtyCode.getSuitableIntervalForProjVar(count, availableIntervals, existingIntervals);
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
        
        // Assigning intervals to projVars which do not take part in any consistency constraint
		for (int cliqueIndex = 0; cliqueIndex < cliqueCount; cliqueIndex++) {
        	HashMap<String, ProjectionStuffInColumn> columnWiseProjectionStuff = cliqueWColumnWProjectionStuff.get(cliqueIndex);
			HashMap<IntExpr, DirtyValueInterval> projVarToInterval = cliqueWiseProjVarToInterval.get(cliqueIndex);
			for (Entry<String, ProjectionStuffInColumn> entry : columnWiseProjectionStuff.entrySet()) {
				String columnname = entry.getKey();
				ProjectionStuffInColumn projectionStuffInColumn = entry.getValue();
				for (Entry<Region, ProjectionStuffInSSPRegion> regionAndProjectionStuff : projectionStuffInColumn.getMapSSPRegionToProjectionStuff().entrySet()) {
					Region region = regionAndProjectionStuff.getKey();
					ProjectionStuffInSSPRegion projectionStuff = regionAndProjectionStuff.getValue();
					Set<IntExpr> projVarsInSSPRegion = projectionStuff.getAllProjVars();

					HashMap<Region, List<LinkedList<DirtyValueInterval>>> regionWCliqueWAvailableIntervals = columnWRegionWCliqueWAvailableIntervals.get(columnname);
					if (regionWCliqueWAvailableIntervals == null) {
						regionWCliqueWAvailableIntervals = new HashMap<>();
						columnWRegionWCliqueWAvailableIntervals.put(columnname, regionWCliqueWAvailableIntervals);
					}
					List<LinkedList<DirtyValueInterval>> cliqueWAvailableIntervals = regionWCliqueWAvailableIntervals.get(region);
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
								DirtyValueInterval bestInterval = DirtyCode.getSuitableIntervalForProjVar(count, availableIntervals, null);
								DirtyCode.removeFromIntervals(bestInterval, availableIntervals);
								projVarToInterval.put(projVar, bestInterval);
							}
						}
					}
				}
			}
		}
		
		// Finding MaxUniqePoints per sspRegion
		for (Entry<String, HashMap<Region, List<LinkedList<DirtyValueInterval>>>> outerEntry : columnWRegionWCliqueWAvailableIntervals.entrySet()) {
			String columnname = outerEntry.getKey();
			HashMap<Region, List<LinkedList<DirtyValueInterval>>> regionWCliqueWAvailableIntervals = outerEntry.getValue();
			HashMap<Region, Long> sspRegionToMaxUniqePoints = new HashMap<>();
			for (Entry<Region, List<LinkedList<DirtyValueInterval>>> innerEntry : regionWCliqueWAvailableIntervals.entrySet()) {
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
				if (maxEnd >= maxint)	// Search "LongIndexNeeded" in this code
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

	private void createNonAggVarsToProjectionValues(Model model, Context ctxPhase2, Model modelPhase2, ProjectionStuffInColumn projectionStuffInColumn, Set<Integer> slackVarsIndexes, 
			String columnname, Long maxUniqePoints, HashMap<IntExpr, ProjectionValues> varsToProjectionValues, int colIndx, int cindex) {
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
					curNonAggVar = it.next();		// This should not throw error!
					curNonAggVarUsableSize = Long.parseLong(model.evaluate(curNonAggVar, true).toString());
				} while (curNonAggVarUsableSize == 0);
				ProjectionValues projectionValues = getOrAdd(varsToProjectionValues, curNonAggVar);		// projectionValues in one nonAggVar
				
				long prevOccurrence = -1;
				long startFrom = -1;
				long lastSeenI = -1;
				
				outerloop:
				for (long i = -1, curOccurrence = 0;;) {
					do {
						++i;
						if (i == maxUniqePoints)
							break;
						IntExpr intExpr = ctxPhase2.mkIntConst(slackVarName + "_" + i);
						curOccurrence = Long.parseLong(modelPhase2.evaluate(intExpr, true).toString());
					} while (curOccurrence == 0);

					if (i == maxUniqePoints)
						break;
					
					if (lastSeenI+1 != i) {		// If i's are not continuous then need to end interval
						if (prevOccurrence > 0) {
							projectionValues.addToList(colIndx, startFrom, lastSeenI+1, prevOccurrence);	// excluding i i.e. last point
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
							projectionValues.addToList(colIndx, i, i+1, curNonAggVarUsableSize);
							curOccurrence -= curNonAggVarUsableSize;
							while (true) {
								do {
									if (it.hasNext())
										curNonAggVar = it.next();
									else
										break outerloop;
									curNonAggVarUsableSize = Long.parseLong(model.evaluate(curNonAggVar, true).toString());
								} while (curNonAggVarUsableSize == 0);
								projectionValues = getOrAdd(varsToProjectionValues, curNonAggVar);
								if (curNonAggVarUsableSize >= curOccurrence) {
									curNonAggVarUsableSize -= curOccurrence;		// space used by i'th value
									prevOccurrence = curOccurrence;
									startFrom = i;
									break;
								}
								else {
									projectionValues.addToList(colIndx, i, i+1, curNonAggVarUsableSize);
									curOccurrence -= curNonAggVarUsableSize;
								}
							}
						}
					} else {
						if (prevOccurrence > 0) {
							projectionValues.addToList(colIndx, startFrom, i, prevOccurrence);	// excluding i i.e. last point
						}
						if (curNonAggVarUsableSize >= curOccurrence) {
							curNonAggVarUsableSize -= curOccurrence;
						} else {		// curNonAggVarUsableSize can't accommodate curCount
							do {
								projectionValues.addToList(colIndx, i, i+1, curNonAggVarUsableSize);
								curOccurrence -= curNonAggVarUsableSize;
								do {
									if (it.hasNext())
										curNonAggVar = it.next();
									else
										break outerloop;
									curNonAggVarUsableSize = Long.parseLong(model.evaluate(curNonAggVar, true).toString());
								} while (curNonAggVarUsableSize == 0);
								projectionValues = getOrAdd(varsToProjectionValues, curNonAggVar);
							} while (curNonAggVarUsableSize < curOccurrence);
							curNonAggVarUsableSize -= curOccurrence;		// space used by i'th value
						}
						prevOccurrence = curOccurrence;
						startFrom = i;
					}
					
					if (curNonAggVarUsableSize == 0) {
						if (prevOccurrence > 0) {
							projectionValues.addToList(colIndx, startFrom, i+1, prevOccurrence);	// excluding i i.e. last point
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

	private void createAggVarsToProjectionValues(Model model, Context ctxPhase2, Model modelPhase2, ProjectionStuffInSSPRegion projectionStuffInSSPRegion, HashMap<IntExpr, DirtyValueInterval> projVarToInterval, 
			String columnname, Long maxUniqePoints, HashMap<IntExpr, ProjectionValues> varsToProjectionValues, int colIndx, int cindex) {
		
		for (Entry<IntExpr, List<IntExpr>> entry : projectionStuffInSSPRegion.getAggVarsToProjVars().entrySet()) {
			IntExpr aggVar = entry.getKey();
			List<IntExpr> projVars = entry.getValue();
			long rowcount = Long.parseLong(model.evaluate(aggVar, true).toString());
			if (rowcount == 0)
				continue;
			ProjectionValues projectionValues = getOrAdd(varsToProjectionValues, aggVar);		// projectionValues in one aggVar
			String aggVarName = aggVar.toString();
			for (IntExpr projVar : projVars) {		// Every interval is disjoint
				DirtyValueInterval interval = projVarToInterval.get(projVar);	// If interval was null then projVar value was 0
				if (interval != null) {
					long prevCount = -1;
					long startFrom = -1;
					for(long i = interval.start; i < interval.end; ++i) {
						IntExpr intExpr = ctxPhase2.mkIntConst("n_" + aggVarName + "_" + i);
						long curCount = Long.parseLong(modelPhase2.evaluate(intExpr, true).toString());
						if (curCount != prevCount) {
							if (prevCount != -1)
								projectionValues.addToList(colIndx, startFrom, i, prevCount);	// excluding i i.e. last point
							prevCount = curCount;
							startFrom = i;
						}
					}
					projectionValues.addToList(colIndx, startFrom, interval.end, prevCount);
				}
			}
		}
	}

	private void createPointWiseConstraints(Model model, Solver solverPhase2, Context ctxPhase2, ProjectionConsistencyInfo projectionConsistencyInfo, List<HashMap<String, ProjectionStuffInColumn>> cliqueWColumnWProjectionStuff, 
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
			for (IntExpr projVar : projVars) {		// Every interval is disjoint
				DirtyValueInterval interval = projVarToInterval.get(projVar);	// If interval was null then projVar value was 0
				if (interval != null)
					for(long i = interval.start; i < interval.end; ++i) {
						IntExpr intExpr = ctxPhase2.mkIntConst("n_" + aggVarName + "_" + i);
						addList.add(intExpr);
						solverPhase2.add(ctxPhase2.mkGe(intExpr, ctxPhase2.mkInt(1)));		// Must be greater than 0
						pointWListOfNewVars.get((int)i).add(intExpr);
					}
			}
			solverPhase2.add(ctxPhase2.mkEq(ctxPhase2.mkAdd(addList.toArray(new ArithExpr[addList.size()])), ctxPhase2.mkInt(rowcount)));
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
    			for(long j = 0; j < maxUniqePoints; ++j) {
					IntExpr intExpr = ctxPhase2.mkIntConst(slackVarName + "_" + j);
					addList.add(intExpr);
					solverPhase2.add(ctxPhase2.mkGe(intExpr, ctxPhase2.mkInt(0)));
					pointWListOfNewVars.get((int)j).add(intExpr);
				}
    			solverPhase2.add(ctxPhase2.mkEq(ctxPhase2.mkAdd(addList.toArray(new ArithExpr[addList.size()])), ctxPhase2.mkInt(atomicSetRowCount)));
    		}
    	}
	}

	private String getSlackVarStringName(int cliqueIndex, String columnname, int slackVarIndex) {
		return slackVarNamePrefix + cliqueIndex + "_" + columnname + "_" + slackVarIndex;
	}

	private List<ProjectionConsistencyInfoPair> getCorrespondingRegionList(HashMap<Region, List<ProjectionConsistencyInfoPair>> sspRegionToAllProjectionConsistencyInfoPairs, HashMap<Region, ProjectionStuffInSSPRegion> sspRegionToProjectionStuff, IntExpr aVar) {
		for (Entry<Region, ProjectionStuffInSSPRegion> entry : sspRegionToProjectionStuff.entrySet()) {
			Region region = entry.getKey();
			ProjectionStuffInSSPRegion projectionStuffInSSPRegion = entry.getValue();
			HashSet<IntExpr> unionAggAndNonAggVars = new HashSet<>(projectionStuffInSSPRegion.getAggVars());
			unionAggAndNonAggVars.addAll(projectionStuffInSSPRegion.getNonAggVars());
			if (unionAggAndNonAggVars.contains(aVar)) {
				List<ProjectionConsistencyInfoPair> listOfProjectionConsistencyInfoPairs = sspRegionToAllProjectionConsistencyInfoPairs.get(region);
				if (listOfProjectionConsistencyInfoPairs == null) {
					listOfProjectionConsistencyInfoPairs = new ArrayList<>();
					sspRegionToAllProjectionConsistencyInfoPairs.put(region, listOfProjectionConsistencyInfoPairs);
				}
				return listOfProjectionConsistencyInfoPairs;
			}
		}
		throw new RuntimeException("Corresponding SSP region of aVar not found!");
	}

	private void collectProjectionConsistencyData(Optimize solver, Context ctx, List<IntExpr> c1Vars, List<IntExpr> c2Vars,
			int c1index, int c2index, Set<String> commonCols, List<ProjectionConsistencyInfoPair> projectionConsistencyInfoPairs, List<HashMap<String, ProjectionStuffInColumn>> cliqueWColumnWProjectionStuff) {
		HashMap<String, ProjectionStuffInColumn> c1ColumnWProjectionStuffInColumn = cliqueWColumnWProjectionStuff.get(c1index);
		HashMap<String, ProjectionStuffInColumn> c2ColumnWProjectionStuffInColumn = cliqueWColumnWProjectionStuff.get(c2index);
		
		for (String columnname : commonCols) {
			ProjectionStuffInColumn c1ProjectionStuffInColumn = c1ColumnWProjectionStuffInColumn.get(columnname);
			ProjectionStuffInColumn c2ProjectionStuffInColumn = c2ColumnWProjectionStuffInColumn.get(columnname);
			if (c1ProjectionStuffInColumn == null && c2ProjectionStuffInColumn == null)		// No projection on this column
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
			
			ProjectionConsistencyInfoPair projectionConsistencyInfoPair = new ProjectionConsistencyInfoPair(c1index, c2index, c1ProjVars, c1AggVars, c1NonAggVars, c2ProjVars, c2AggVars, c2NonAggVars, columnname);
			projectionConsistencyInfoPairs.add(projectionConsistencyInfoPair);
			c1ColumnWProjectionStuffInColumn.get(columnname).getConsistencyConstraintSetWNonAggVars().add(c1NonAggVars);
			c2ColumnWProjectionStuffInColumn.get(columnname).getConsistencyConstraintSetWNonAggVars().add(c2NonAggVars);
		}
	}
	
	private void getProjectionRelatedVars(List<IntExpr> vars, ProjectionStuffInColumn projectionStuffInColumn, Set<IntExpr> aggVars, Set<IntExpr> nonAggVars, Set<IntExpr> projVars) {
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

	private void dirtyMergeWithAlignment(IntList lhsColIndxs, List<DirtyValueCombinationWithProjectionValues> lhsValueCombinations, IntList rhsColIndxs, LinkedList<DirtyVariableValuePairWithProjectionValues> rhsVarValuePairs) {
		
		// Raghav didn't used retainAll in original mergeWithAlignment maybe because ordering was getting changed!
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

            for (ListIterator<DirtyVariableValuePairWithProjectionValues> rhsIter = rhsVarValuePairs.listIterator(); rhsIter.hasNext();) {
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
                    
                    if (lhsProjectionValues == null && rhsProjectionValues == null) {	// No projection on any of columns of any clique
                    } else {
                    	if (lhsProjectionValues == null || rhsProjectionValues == null) {	// No shared projection column
                        	mergedProjectionvalues = new ProjectionValues();
                    		if (lhsProjectionValues != null)
                    			mergedProjectionvalues.takeFrom(lhsProjectionValues, minMergable, null);
                    		else
                    			mergedProjectionvalues.takeFrom(rhsProjectionValues, minMergable, null);
                    	} else {
                    		Set<Integer> sharedProjectionCols = new HashSet<>(lhsProjectionValues.keySet());
                    		sharedProjectionCols.retainAll(rhsProjectionValues.keySet());
                    		if (sharedProjectionCols.size() == 0) {	// No shared projection column
                            	mergedProjectionvalues = new ProjectionValues();
                    			if (lhsProjectionValues != null)
                        			mergedProjectionvalues.takeFrom(lhsProjectionValues, minMergable, null);
                    			if (rhsProjectionValues != null)
                        			mergedProjectionvalues.takeFrom(rhsProjectionValues, minMergable, null);
                    		} else {	// Shared projection columns present
                    			mergedProjectionvalues = DirtyCode.getIntersectingProjectionValues(sharedColIndxs, lhsProjectionValues, rhsProjectionValues);	// Returns intersecting projection values for shared indexes and remove those values from lhsProjectionValues and rhsProjectionValues
                    			minMergable = mergedProjectionvalues.getTotalCount();
                    			if (minMergable > 0) {
                    				mergedProjectionvalues.takeFrom(lhsProjectionValues, minMergable, sharedProjectionCols);	// Getting projection values from lhsProjectionValues for those columns which were not present in sharedProjectionCols
                    				mergedProjectionvalues.takeFrom(rhsProjectionValues, minMergable, sharedProjectionCols);	// Getting projection values from rhsProjectionValues for those columns which were not present in sharedProjectionCols
                    			}
                    		}
                    	}
                    }
                    if (minMergable > 0) {
                    	DirtyValueCombinationWithProjectionValues mergedValueCombination = new DirtyValueCombinationWithProjectionValues(mergedColValues, minMergable, mergedProjectionvalues);
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
        
        //if (DebugHelper.sanityChecksNeeded()) {
        for (DirtyValueCombinationWithProjectionValues lhsValueCombination : lhsValueCombinations) {
            if (lhsValueCombination.getRowcount() != 0) {
            	if (!acceptErrorsWhenRegionMismatch)
            		throw new RuntimeException("Found while merge ValueCombination " + lhsValueCombination + " in LHS with unmet rowcount");
            	Pair<DirtyVariableValuePairWithProjectionValues, BucketStructure> bestMatchPair = DirtyCode.findBestMatch(lhsValueCombination, rhsVarValuePairs, posInLHS, posInRHS);
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
                mergedProjectionvalues.takeFrom(bestMatchRHSVariableValuePair.getProjectionValues(), rowCount, mergedProjectionvalues.keySet());
                
                
                DirtyValueCombinationWithProjectionValues mergedValueCombination = new DirtyValueCombinationWithProjectionValues(mergedColValues, rowCount, mergedProjectionvalues);
                mergedValueCombinations.add(mergedValueCombination);
            	lhsValueCombination.reduceRowcount(rowCount);
            	rhsVarValuePairs.remove(bestMatchRHSVariableValuePair);
            }
        }
        if (!rhsVarValuePairs.isEmpty())
            throw new RuntimeException("Found while merge RHS variables not getting exhausted");

        //Updating targetColIndxs
        lhsColIndxs.clear();
        lhsColIndxs.addAll(mergedColIndxsList);

        //Updating targetValueCombinations
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
        //Instantiating variables of first clique
        for (VariableValuePair varValuePair : cliqueIdxToVarValuesList.get(0)) {
            IntList columnValues = getFloorInstantiation(varValuePair.variable);
            ValueCombination valueCombination = new ValueCombination(columnValues, varValuePair.rowcount);
            mergedValueCombinations.add(valueCombination);
        }

        for (int i = 1; i < cliqueCount; i++) {		// merging with other cliques
            mergeWithAlignment(mergedColIndxs, mergedValueCombinations, arasuCliquesAsColIndxs.get(i), cliqueIdxToVarValuesList.get(i));
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
        //choose BS with min bucket floors
        BucketStructure leadingBS = variable.getLeadingBS();
        IntList columnValues = new IntArrayList(leadingBS.getAll().size());
        for (Bucket b : leadingBS.getAll()) {
            columnValues.add(b.min());
        }
        return columnValues;
    }

    /**
     * lhs has instantiated ValueCombinations. Each lhs ValueCombination is extended by widthwise appending instantiated tuples from appropriate BucketStructure of compatible rhs variable(s).
     *     lhsColIndxs and lhsValueCombinations get side effects.
     *     rhsColIndxs gets NO side effects.
     *     rhsVarValuePairs gets exhausted and becomes empty.
     * TODO: Could have been some smart alignment which prevents extra tuples to be added to primary view
     * @param lhsColIndxs
     * @param lhsValueCombinations
     * @param rhsColIndxs
     * @param sourceValueCombinations
     */
    private void mergeWithAlignment(IntList lhsColIndxs, List<ValueCombination> lhsValueCombinations, IntList rhsColIndxs,
            LinkedList<VariableValuePair> rhsVarValuePairs) {

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

        //if (DebugHelper.sanityChecksNeeded()) {
        for (ValueCombination lhsValueCombination : lhsValueCombinations) {
            if (lhsValueCombination.getRowcount() != 0)
                throw new RuntimeException("Found while merge ValueCombination " + lhsValueCombination + " in LHS with unmet rowcount");
        }
        if (!rhsVarValuePairs.isEmpty())
            throw new RuntimeException("Found while merge RHS variables not getting exhausted");

        //Updating targetColIndxs
        lhsColIndxs.clear();
        lhsColIndxs.addAll(mergedColIndxsList);

        //Updating targetValueCombinations
        lhsValueCombinations.clear();
        lhsValueCombinations.addAll(mergedValueCombinations);
    }

    private static BucketStructure getCompatibleBS(IntList posInLHS, IntList lhsColValues, IntList posInRHS, Region var) {
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
    
    private HashMap<String, ProjectionStuffInColumn> getColumnWiseAggAndNonAggVarsInSingleSplitPointRegion(Set<String> clique, List<List<IntExpr>> solverConstraints, HashMap<String, 
    		Set<IntExpr>> columnWiseVarsInAllAggregateConditions, Map<String, List<Region>> aggregateColumnsToSingleSplitPointRegions, int offsetForSingleSplitPointRegions, ArrayList<String> sortedAggCols) {
    	HashMap<String, ProjectionStuffInColumn> result = new HashMap<>();
        int tempIndex = 0;
        for (String columnname : sortedAggCols) {
			if (clique.contains(columnname)) {
				ProjectionStuffInColumn projectionStuffInColumn = new ProjectionStuffInColumn();
				
				Set<IntExpr> blocksInAllAggregateConditions = columnWiseVarsInAllAggregateConditions.getOrDefault(columnname, new HashSet<>()); // This clique might not have any aggregate condition on this column, hence getOrDefault
				List<Region> splitPointRegions = aggregateColumnsToSingleSplitPointRegions.get(columnname);
				for (int splitPointIndex = 0; splitPointIndex < splitPointRegions.size(); ++splitPointIndex) {
					// offsetForSingleSplitPointRegions + tempIndex is correct index because of consistent
					// ordering during multiple iterations of aggregateColumnsToSingleSplitPointRegions
					Set<IntExpr> aggBlocks = new HashSet<>(solverConstraints.get(offsetForSingleSplitPointRegions + tempIndex));
					Set<IntExpr> nonAggBlocks = new HashSet<>(aggBlocks);
					
					aggBlocks.retainAll(blocksInAllAggregateConditions);	// Only retain those blocks which are also in aggregate conditions
					nonAggBlocks.removeAll(aggBlocks);	// Remove those blocks which are in aggregate conditions
					
					projectionStuffInColumn.putProjectionStuffInSSPRegion(splitPointRegions.get(splitPointIndex), new ProjectionStuffInSSPRegion(aggBlocks, nonAggBlocks));
					tempIndex++;
				}
				result.put(columnname, projectionStuffInColumn);
			}
		}
		return result;
	}

	private HashMap<String, Set<IntExpr>> getColumnWiseAggVarsInAllAggregateConditions(List<FormalCondition> conditions, List<List<IntExpr>> solverConstraints, List<Pair<Integer, Boolean>> applicableConditions) {
		
    	HashMap<String, Set<IntExpr>> result = new HashMap<>();
        for (int i = 0; i < applicableConditions.size(); ++i) {
        	Pair<Integer, Boolean> applicableConditionInfo = applicableConditions.get(i);
        	if (applicableConditionInfo.getSecond()) {	// Is the condition applied with projection or without projection 
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
			throw new RuntimeException("one << 63 will overflow and won't work correctly because long is used. Change type of 'one' to BigInt or something like that!");
		long one = 1;
        for (int i = 0; i < (one << size); i++)
        {
        	List<IntExpr> newSet = new ArrayList<>();		// using list to save on memory because set properties are not required
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
        powerSet.remove(new ArrayList<>());		// removing empty set
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
     * cliqueIndextoPList stores current partition (set of variables) of every clique.
     * This method takes two (intersecting) cliques i and j as input and replaces
     *     cliqueIndextoPList[i] with a newer partition (a fresh set of variables)
     *     cliqueIndextoPList[j] with a newer partition (a fresh set of variables)
     * such that these newer variables of the two cliques can be used to frame consistency conditions later.
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
        Partition freshPartitionI = getFreshVariables(parentOldVarIdsI, partitionI, splitRegions, intersectingColIndices);
        cliqueIdxtoPList.set(i, freshPartitionI);
        List<IntList> sourceListI = cliqueIdxtoPSimplifiedList.get(i);
        List<IntList> freshListI = new ArrayList<>(freshPartitionI.size());
        for (int a = 0; a < freshPartitionI.size(); a++) {
            freshListI.add(sourceListI.get(parentOldVarIdsI.getInt(a)));
        }
        cliqueIdxtoPSimplifiedList.set(i, freshListI);

        IntArrayList parentOldVarIdsJ = new IntArrayList();
        Partition freshPartitionJ = getFreshVariables(parentOldVarIdsJ, partitionJ, splitRegions, intersectingColIndices);
        cliqueIdxtoPList.set(j, freshPartitionJ);
        List<IntList> sourceListJ = cliqueIdxtoPSimplifiedList.get(j);
        List<IntList> freshListJ = new ArrayList<>(freshPartitionJ.size());
        for (int a = 0; a < freshPartitionJ.size(); a++) {
            freshListJ.add(sourceListJ.get(parentOldVarIdsJ.getInt(a)));
        }
        cliqueIdxtoPSimplifiedList.set(j, freshListJ);

        CliqueIntersectionInfo cliqueIntersectionInfo = new CliqueIntersectionInfo(i, j, intersectingColIndices);
        //cliqueIntersectionInfo.splitRegions = splitRegions;
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
     * Returns a Partition with freshVariables and also populated parentOldVarIds of same length storing which oldVarId it came from
     * @param parentOldVarIds
     * @param oldPartition
     * @param splitRegions
     * @param intersectingColIndices
     * @return
     */
    private Partition getFreshVariables(IntList parentOldVarIds, Partition oldPartition, List<Region> splitRegions, IntList intersectingColIndices) {

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

                Region freshRegion = new Region(); //stores list of untruncated bs which come from intersection of oldRegion and splitRegion
                Region remainRegion = new Region(); //stores list of untruncated bs which come from oldRegion minus splitRegion
                for (BucketStructure oldBS : oldRegion.getAll()) { //need to do bs by bs so that untruncing is possible
                    Region oldSingleBSRegion = new Region();
                    oldSingleBSRegion.add(oldBS);
                    Region truncOldSingleBSRegion = getTruncatedRegion(oldSingleBSRegion, intersectingColIndices);
                    Region truncOverlap = truncOldSingleBSRegion.intersection(splitRegion);
                    if (truncOverlap.isEmpty()) {
                        remainRegion.add(oldBS);
                    } else {
                        Region untruncOverlap = getUntruncatedRegion(truncOverlap, oldSingleBSRegion, intersectingColIndices);
                        freshRegion.addAll(untruncOverlap.getAll());

                        Region remainTruncRegion = truncOldSingleBSRegion.minus(truncOverlap);
                        if (!remainTruncRegion.isEmpty()) {
                            Region remainUntruncRegion = getUntruncatedRegion(remainTruncRegion, oldSingleBSRegion, intersectingColIndices);
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
            throw new RuntimeException("Expected oldRegions to be empty here. oldRegions " + oldRegions.size() + " and oldRegionIds " + oldRegionIds.size());

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

    private Region getTruncatedRegion(Region region, IntList intersectingColIndices) {
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
            throw new RuntimeException("Can only untruncate Regions with single BucketStructure in donor but found " + donorRegion.getAll().size());

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
    
    private void addTo_ConditionListToPairOfVarList(List<List<IntExpr>> applicableSolverConstraints, HashMap<IntList, MutablePair<List<IntExpr>, List<IntExpr>>> conditionListToSetOfVarList) {
    	HashMap<IntExpr, IntList> varToConditionList = new HashMap<>();
		for (int i = 0; i < applicableSolverConstraints.size(); i++) {
			for (IntExpr var : applicableSolverConstraints.get(i)) {	// For every region which satisfy i'th condition
				if(!varToConditionList.containsKey(var))
					varToConditionList.put(var, new IntArrayList());
				varToConditionList.get(var).add(i);
			}
		}
		HashMap<IntList, List<IntExpr>> conditionListToVarList = new HashMap<>();
        for (Entry<IntExpr, IntList> entry : varToConditionList.entrySet()) {
        	IntExpr var = entry.getKey();
        	IntList conditionsList = entry.getValue();
			if(!conditionListToVarList.containsKey(conditionsList)) {
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

class CliqueIntersectionInfo {
    final int     i;
    final int     j;
    final IntList intersectingColIndices;
    //List<Region> splitRegions;

    public CliqueIntersectionInfo(int i, int j, IntList intersectingColIndices) {
        this.i = i;
        this.j = j;
        this.intersectingColIndices = intersectingColIndices;
    }
}



class ProjectionConsistencyInfoPair {
	final Pair<ProjectionConsistencyInfo, ProjectionConsistencyInfo> pair;
	final String columnname;
	public ProjectionConsistencyInfoPair(int c1index, int c2index, Set<IntExpr> c1ProjVars, Set<IntExpr> c1AggVars, Set<IntExpr> c1NonAggVars, Set<IntExpr> c2ProjVars, Set<IntExpr> c2AggVars, Set<IntExpr> c2NonAggVars, String columnname) {
		ProjectionConsistencyInfo first = new ProjectionConsistencyInfo(c1index, c1ProjVars, c1AggVars, c1NonAggVars);
		ProjectionConsistencyInfo second = new ProjectionConsistencyInfo(c2index, c2ProjVars, c2AggVars, c2NonAggVars);
		this.columnname = columnname;
		pair = new Pair<ProjectionConsistencyInfo, ProjectionConsistencyInfo>(first, second);
	}
	
	public ProjectionConsistencyInfo getFirst() {
		return pair.getFirst();
	}
	
	public ProjectionConsistencyInfo getSecond() {
		return pair.getSecond();
	}
	
	public void setSlackVarsIndexes(List<Integer> c1SlackVarsIndexes, List<Integer> c2SlackVarsIndexes) {
		pair.getFirst().setSlackVarsIndexes(c1SlackVarsIndexes);
		pair.getSecond().setSlackVarsIndexes(c2SlackVarsIndexes);
	}
}

class ProjectionConsistencyInfo {
	final int cindex;
	final Set<IntExpr> projVars;
	final Set<IntExpr> aggVars;
	final Set<IntExpr> nonAggVars;
	List<Integer> slackVarsIndexes;
	public ProjectionConsistencyInfo(int cindex, Set<IntExpr> projVars, Set<IntExpr> aggVars, Set<IntExpr> nonAggVars) {
		this.cindex = cindex;
		this.projVars = projVars;
		this.aggVars = aggVars;
		this.nonAggVars = nonAggVars;
	}
	
	public void setSlackVarsIndexes(List<Integer> slackVarsIndexes) {
		this.slackVarsIndexes = slackVarsIndexes;
	}
	
	@Override
    public int hashCode() {
        final int prime = 31;
        int result = cindex;
        result = prime * result + aggVars.hashCode();
        result = prime * result + nonAggVars.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
    	if (this == obj) return true;
    	if (getClass() != obj.getClass())
    		return false;
    	@SuppressWarnings("unchecked")
    	ProjectionConsistencyInfo that = (ProjectionConsistencyInfo)obj;
    	if (!aggVars.equals(that.aggVars))
    		return false;
    	if (!nonAggVars.equals(that.nonAggVars))
    		return false;
    	
    	return true;
    }
}
package in.ac.iisc.cds.dsl.cdgvendor.solver;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import in.ac.iisc.cds.dsl.cdgvendor.constants.PostgresVConfig;
import in.ac.iisc.cds.dsl.cdgvendor.constants.PostgresVConfig.LPSolvingType;
import in.ac.iisc.cds.dsl.cdgvendor.model.ValueCombination;
import in.ac.iisc.cds.dsl.cdgvendor.model.VariableValuePair;
import in.ac.iisc.cds.dsl.cdgvendor.model.ViewInfo;
import in.ac.iisc.cds.dsl.cdgvendor.model.ViewSolution;
import in.ac.iisc.cds.dsl.cdgvendor.model.ViewSolutionInMemory;
import in.ac.iisc.cds.dsl.cdgvendor.model.ViewSolutionRegion;
import in.ac.iisc.cds.dsl.cdgvendor.model.ViewSolutionWithSolverStats;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalCondition;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionAggregate;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionAnd;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionBaseAggregate;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionComposite;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionOr;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionSOP;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionSimpleInt;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.Symbol;
import in.ac.iisc.cds.dsl.cdgvendor.reducer.Bucket;
import in.ac.iisc.cds.dsl.cdgvendor.reducer.BucketStructure;
import in.ac.iisc.cds.dsl.cdgvendor.reducer.Region;
import in.ac.iisc.cds.dsl.cdgvendor.utils.ConditionsEvaluator;
import in.ac.iisc.cds.dsl.cdgvendor.utils.DebugHelper;
import in.ac.iisc.cds.dsl.cdgvendor.utils.StopWatch;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
//TODO add sanity check. Clique merging should be done at region level ?
public class Z3Solver {

    private static final DecimalFormat DF = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
    static {
        DF.setMaximumFractionDigits(340);
    }

    public enum SolverType {
        ARASU,
        DOUBLE;
    }

    public enum SpillType {
        INMEMORY,
        //        MAPDBBACKED,
        //        FILEBACKED,
        FILEBACKED_FKeyedBased;
    }
    
    public enum ConsistencyMakerType {
    	BRUTEFORCE,
    	CONSISTENCYFILTERS
    }

    private final SolverType solverType;
    private final ConsistencyMakerType consistencyMakerType;
    /**
     * Applicable in case of ARASU
     */
    private final SpillType  spillType;

    public Z3Solver(SolverType solverType, SpillType spillType) {
        this.solverType = solverType;
        this.spillType = spillType;
        this.consistencyMakerType = ConsistencyMakerType.CONSISTENCYFILTERS;
    }

    public ViewSolution getTrivialSolution(ViewInfo viewInfo) {
        IntList colValues = new IntArrayList(viewInfo.getViewNonkeys().size());
        for (int i = viewInfo.getViewNonkeys().size() - 1; i >= 0; i--) {
            colValues.add(0);
        }
        ViewSolutionInMemory viewSolution = new ViewSolutionInMemory(1);
        viewSolution.addValueCombination(new ValueCombination(colValues, viewInfo.getRowcount()));
        return viewSolution;
    }
    
    public ViewSolutionRegion solveView(List<FormalCondition> conditions, ViewInfo viewInfo, String viewname, long scaleFactor) {
        StopWatch preprocessViewSW = new StopWatch("Pre-Processing-" + viewname);
    
        //removes non projection conditions and non-top CCs
        for(ListIterator<FormalCondition>iter=conditions.listIterator();iter.hasNext();) {
        	FormalCondition fc=iter.next();
        	if(!(fc instanceof FormalConditionAggregate))
        		iter.remove();
        	else if(((FormalConditionAggregate)fc).isTop()==false)
        		iter.remove();
        }
        if(conditions.size()==0)
        	return null;
        
        //--------------------------------------
        
        Map<String, IntList> bucketFloorsByColumns = DomainDecomposer.getBucketFloors(conditions, viewInfo);

        final List<String> sortedViewColumns = new ArrayList<>(bucketFloorsByColumns.keySet());
        Collections.sort(sortedViewColumns);

        //Marking buckets for each column as true
        double varcountUR = 1;
        final List<boolean[]> allTrueBS = new ArrayList<>();
        for (String column : sortedViewColumns) {
            int length = bucketFloorsByColumns.get(column).size();
            boolean[] arr = new boolean[length];
            varcountUR *= length;

            Arrays.fill(arr, true);
            allTrueBS.add(arr);
        }
        
        DebugHelper.printInfo("Number of variables without reduction is " + DF.format(varcountUR));

        if (solverType == SolverType.DOUBLE) {
            //NOTE: Scaling only to extent when cardinality value stays within permissible range of long
            //NOTE: Scaling such that rowcount of any PK table goes beyond range of int will introduce negative values in DatabaseSummary.
            //TODO : This is not yet fixed.

            //long scaleFactor = (long) 1e10;
            DebugHelper.printInfo("SCALING by " + scaleFactor);
            viewInfo.scaleRowCount(scaleFactor);
            for (FormalCondition condition : conditions) {
                condition.scaleRowCount(scaleFactor);
            }
        }

        CliqueFinder cliqueReducer = new CliqueFinder(viewInfo, allTrueBS);
        List<Set<String>> arasuCliques = cliqueReducer.getOrderedNonTrivialCliques(conditions);
        int cliqueCount=arasuCliques.size();
        
        //gets clique to groupkey list
        List<Set<List<String>>>cliqueToGroupkeys=new ArrayList<>();
        for (int i = 0; i < cliqueCount; i++) {
        	Set<List<String>>groupkeys=new HashSet<>();
        	Set<String> clique = arasuCliques.get(i);   // set of columns

        	for(FormalCondition condition:conditions) {
        		if(!(condition instanceof FormalConditionAggregate))
        			continue;
        		Set<String> appearingCols = new HashSet<>();
        		getAppearingCols(appearingCols, condition);   // appearing columns will have columns appeared in current condition
        		if (clique.containsAll(appearingCols)) {
            	  List<String>groupkey=((FormalConditionAggregate)(condition)).getGroupKey();
            	  groupkeys.add(groupkey);
              }
        	}
        	cliqueToGroupkeys.add(groupkeys);
        }
        
        mergeCliquesForProjection(arasuCliques,cliqueToGroupkeys);
		
        //TODO add sanity check to see if done right
        noConflictSanityCheck(arasuCliques,cliqueToGroupkeys);
///////////////// Start dk
        /*Disable arasu clique optimisation
        Set<String> jumboClique = new HashSet<>();
        for (Set<String> clique : arasuCliques) {
        	jumboClique.addAll(clique);
        }
        arasuCliques.clear();
        arasuCliques.add(jumboClique);/**/
///////////////// End dk
        
        long varcountAR = cliqueReducer.getReducedVariableCount(arasuCliques);
        DebugHelper.printInfo("Number of variables using Arasu's clique based reduction " + varcountAR);

        // TODO: Enable this
//        if (varcountAR > varcountUR)
//            throw new RuntimeException("Arasu's reduction is increasing variables over unreduced");

///////////////// Start dk
        int totalAggregateConditions = 0;
        List<List<FormalCondition>> conditionsInSOPForm = new ArrayList<>();
        
        //Shadab changed.
        //codeS
        for (FormalCondition condition : conditions) {
        	                       
        	List<FormalCondition> conditionInSOPForm = getSOPSubconditions(condition);	// Note : outputCardinality and projection information is lost
        	conditionsInSOPForm.add(conditionInSOPForm);
        	
		}
        //codeS
        
        //TODO add sort to groupkey while storing
        //Creating a groupkey to condition map
//        Set<Set<String>> groupKeysSet=new HashSet<>();
//        Map<Set<String>,List<FormalCondition>> groupKeyToConditionIdx=new HashMap<>();
//        Map<Set<String>,List<Region>>groupKeyTocRegion=new HashMap<>();
//        for (FormalCondition condition : conditions) {
//        	if (condition instanceof FormalConditionAggregate) {
//        		Set<String>groupKey=new HashSet<>(((FormalConditionAggregate)(condition)).getGroupKey());
//        		groupKeysSet.add(groupKey);
//        		if (!groupKeyToConditionIdx.containsKey(groupKey)) { 
//        			groupKeyToConditionIdx.put(groupKey, new ArrayList<>());
//        			groupKeyTocRegion.put(groupKey,new ArrayList<>());
//        		}
//        		groupKeyToConditionIdx.get(groupKey).add(condition);
//        		groupKeyTocRegion.get(groupKey).add(getConditionRegionsSingle(condition, allTrueBS, sortedViewColumns, bucketFloorsByColumns));
//        		conditionsAggregate.add(condition);
//        		aggregateConditionsInSOPForm.add(getSOPSubconditions(condition));
//        		totalAggregateConditions++;
//        	}
//        }
//        List<Set<String>> groupKeysList=new ArrayList<>(groupKeysSet);
//       
//        Map<Set<String>,Set<FormalConditionOr>> groupKeyToConsistencyFilter=new HashMap<>();
//        Map<Set<String>,List<Region>> groupKeyToConsistencyFilterRegions=new HashMap<>();
//        for (Set<String>groupKey:groupKeyToConditionIdx.keySet()) {
//        	groupKeyToConsistencyFilter.put(groupKey,getConsistencyFiltersProjection(groupKey, groupKeyToConditionIdx.get(groupKey), allTrueBS, bucketFloorsByColumns, sortedViewColumns));
//        	groupKeyToConsistencyFilterRegions.put(groupKey,getConditionRegionsTemp(new ArrayList(getConsistencyFiltersProjection(groupKey, groupKeyToConditionIdx.get(groupKey), allTrueBS, bucketFloorsByColumns, sortedViewColumns)),allTrueBS, sortedViewColumns, bucketFloorsByColumns));
//        	DebugHelper.printInfo("Merge Happened");
//        	//groupKeyToConsistencyFilter.put(groupKey,getgetConsistencyFiltersProjection(groupKey, groupKeyToConditionIdx.get(groupKey), allTrueBS, bucketFloorsByColumns, sortedViewColumns));
//        }
        
        
        
        
        
        
        //Map<String,List<Integer>> groupKeyToConditionIdx=new HashMap<>();
        //for
        //codeS temp
//        List<List<FormalCondition>>tempCondition=new ArrayList<>();
//        Set<String>temp1=new HashSet<String>();
//        temp1.add("t11_c008");
//        Set<FormalConditionOr> newConsistencyConstraints1 = getConsistencyFilters(temp1, conditionsInSOPForm, allTrueBS, bucketFloorsByColumns, sortedViewColumns);
//        for (FormalConditionOr formalCondition : newConsistencyConstraints1) {
//        	tempCondition.add(formalCondition.getConditionList());
//		}
        
      //  List<Region> projectedConditionRegions1 = getConditionRegions(tempCondition, allTrueBS, sortedViewColumns, bucketFloorsByColumns);
        
        //end
        FormalConditionOr consistencyConstraints[] = new FormalConditionOr[0];
        
        if (consistencyMakerType == ConsistencyMakerType.CONSISTENCYFILTERS) {
	        StopWatch getProjectedConditionsSW = new StopWatch("getTruncatedConditions for Consistency Filters");
	        
	        HashMap<FormalConditionOr, Integer> consistencyConstraintsToIndex = new HashMap<>();
	        int newCCIndex = 0;
	        
	        Set<Set<String>> processedCommonAttribs = new HashSet<>();
	        for (int i = 0; i < arasuCliques.size(); i++) {
	        	Set<String> c1 = arasuCliques.get(i);
	        	for (int j = i + 1; j < arasuCliques.size(); j++) {
	        		Set<String> c2 = arasuCliques.get(j);
	        		Set<String> commonAttribsSet = new HashSet<>(c1);
	        		commonAttribsSet.retainAll(c2);
	        		if(!commonAttribsSet.isEmpty()) {
	        			if(processedCommonAttribs.contains(commonAttribsSet))
	        				continue;
	        			else
	        				processedCommonAttribs.add(commonAttribsSet);
	        			Set<FormalConditionOr> newConsistencyConstraints = getConsistencyFilters(commonAttribsSet, conditionsInSOPForm, allTrueBS, bucketFloorsByColumns, sortedViewColumns);
	        			for (FormalConditionOr formalConditionOr : newConsistencyConstraints) {
	        				if(!consistencyConstraintsToIndex.containsKey(formalConditionOr)) {		 // TODO: hashCode and equals of FormalConditionComposite are used and it's not good because it uses Condition list instead of Condition set
	        					consistencyConstraintsToIndex.put(formalConditionOr, newCCIndex);
	        					newCCIndex++;
	        				}
						}
	        		}
				}
			}
	//        Converting to list to maintain ordering
	        consistencyConstraints = new FormalConditionOr[consistencyConstraintsToIndex.size()];
	        for (Entry<FormalConditionOr, Integer> entry : consistencyConstraintsToIndex.entrySet()) {
				consistencyConstraints[entry.getValue()] = entry.getKey();
			}
	        
	        for (FormalConditionOr formalCondition : consistencyConstraints) {
	        	conditionsInSOPForm.add(formalCondition.getConditionList());
			}
	
	        getProjectedConditionsSW.displayTimeAndDispose();
        }
        //codeS
        // 1-D projection handling -- creating constraints (regions) which will split subviews along the split points of group key
       // Set<List<String>>list=getAllColumnsHavingAggregateConditionsNew(conditions); 
        //Set<String> allColumnsHavingAggregateCondition = getAllColumnsHavingAggregateConditions(conditions);
       // Map<String, List<Region>> aggregateColumnsToSingleSplitPointRegions = getSingleSplitPointRegions(sortedViewColumns, bucketFloorsByColumns, allColumnsHavingAggregateCondition);
///////////////// End dk
        
        List<Region> conditionRegions = getConditionRegions(conditionsInSOPForm, allTrueBS, sortedViewColumns, bucketFloorsByColumns);
       // List<Region> conditionAggregateRegions = getConditionRegions(aggregateConditionsInSOPForm, allTrueBS, sortedViewColumns, bucketFloorsByColumns);
        
        
        
        DebugHelper.printInfo("Number of cardinality constraints " + (conditionRegions.size() - consistencyConstraints.length + 1)); //All 1's which is added later is included in the count here
        DebugHelper.printInfo("Number of projection constraints " + totalAggregateConditions);

        ViewSolution fullViewSolution = null;
        //Shadab
        ViewSolutionRegion fullViewSolutionRegion=null;
        ViewSolutionWithSolverStats temp=null;
       
        switch (solverType) {
            case ARASU:
                ArasuReductionBasedViewSolver arasuSolver =
                        new ArasuReductionBasedViewSolver(viewname, viewInfo, allTrueBS, arasuCliques, bucketFloorsByColumns, spillType);
                preprocessViewSW.displayTimeAndDispose();
                //shadab commented this
                //fullViewSolution = arasuSolver.solveView(conditions, new ArrayList<>(conditionRegions), null, null, null);
                fullViewSolutionRegion=new ViewSolutionRegion(fullViewSolution,null);
                break;

            case DOUBLE:
//                DoubleReductionBasedViewSolver doubleSolver =
//                        new DoubleReductionBasedViewSolver(viewname, viewInfo, allTrueBS, arasuCliques, bucketFloorsByColumns);
            	DoubleReductionTemp doubleSolver =
                new DoubleReductionTemp(viewname, viewInfo, allTrueBS, arasuCliques, bucketFloorsByColumns);
                preprocessViewSW.displayTimeAndDispose();
                //Shadab
                //fullViewSolution = doubleSolver.solveView(conditions, new ArrayList<>(conditionRegions), consistencyConstraints, consistencyMakerType, aggregateColumnsToSingleSplitPointRegions);
//                temp=doubleSolver.solveView(conditions, new ArrayList<>(conditionRegions), consistencyConstraints, consistencyMakerType, aggregateColumnsToSingleSplitPointRegions,conditionsAggregate, conditionAggregateRegions,projectedConditionRegions1,groupKeyToConsistencyFilterRegions,groupKeyTocRegion);//changed by shadab
                temp=doubleSolver.solveView(conditions, new ArrayList<>(conditionRegions), consistencyConstraints, consistencyMakerType);//changed by shadab
                //debugSolvingErrorPerCondition(conditions, fullViewSolution, sortedViewColumns);
                //fullViewSolutionRegion=temp.viewSolutionRegion;
                break;

            default:
                throw new RuntimeException("Unsupported SolverType " + solverType);
        }
        //return fullViewSolution;
        //return fullViewSolutionRegion;
        return null;
        
    }
    
    private void mergeCliquesForProjection(List<Set<String>> arasuCliques, List<Set<List<String>>> cliqueToGroupkeys) {
    	boolean check=false;
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

						//mergedClique.addAll(clique1);
						mergedClique.addAll(clique2);
						mergedGroupkeys.addAll(groupkeys2);
						mergeHappened = true;
						check=true;
						toRemove.add(j);

					}
				}
				Collections.sort(toRemove);
				arasuCliques.set(i, mergedClique);
				cliqueToGroupkeys.set(i, mergedGroupkeys);
				if (toRemove.size() != 0) {
					for (int idx = toRemove.size() - 1; idx >= 0; idx--) {
						arasuCliques.remove((int)toRemove.get(idx));
						cliqueToGroupkeys.remove((int)toRemove.get(idx));
					}
				}

			}
		}
		if(check)
			DebugHelper.printInfo("Merge Happened");
		else
			DebugHelper.printInfo("No Merge Happened");
	}

	private void noConflictSanityCheck(List<Set<String>> arasuCliques, List<Set<List<String>>> cliqueToGroupkeys) {
    	for (int i = 0; i < arasuCliques.size(); i++) {

			//Set<String> clique1 = arasuCliques.get(i); // set of columns
			Set<List<String>> groupkeys1 = cliqueToGroupkeys.get(i);
			
			
			for (int j = i + 1; j < arasuCliques.size(); j++) {
				//Set<String> clique2 = arasuCliques.get(j); // set of columns
				Set<List<String>> groupkeys2 = cliqueToGroupkeys.get(j);
				// check intersection more thoroughly

				if (haveConflict(groupkeys1, groupkeys2)) {

					throw new RuntimeException("clique merge conflict. Inter clique inconsistent groupkeys");
				}
			}
    	}
		
	}

	//shadab
    //codeS
    private boolean haveConflict(Set<List<String>> groupkeys1, Set<List<String>> groupkeys2) {
		// checks whether groupkeys from differenct cliques have conflict. a condlict will arise under the assumptions of non overlapping groupkeys when two groupkeys of diff cliques have asubset superset relation.
    	//Hence, its enough that the groupkeys have a common attribute.
    	for(List<String>groupkey1:groupkeys1) {
    		for(List<String>groupkey2:groupkeys2) {
    			List<String>common=new ArrayList<>();
    			common.addAll(groupkey1);
    			common.retainAll(groupkey2);
    			if(common.size()!=0)
    				return true;
    		}
    	}
		return false;
	}

	//added by shadab
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
        
        if (condition instanceof FormalConditionAggregate) {	// Adding those attributes which are part of group key
        	attributesFound.addAll(((FormalConditionAggregate) condition).getGroupKey());
        }
    }
    
    private Map<String, List<Region>> getSingleSplitPointRegions(List<String> sortedColumns, Map<String, IntList> bucketFloorsByColumns, Set<String> allColumnsHavingAggregateCondition) {
    	Map<String, List<Region>> aggregateColumnsToSingleSplitPointRegions = new HashMap<>();
    	
        BucketStructure completeBucketStructure = new BucketStructure();
        for (String columnname : sortedColumns) {
        	Bucket bucket = getBucketInRange(bucketFloorsByColumns.get(columnname).size());
        	completeBucketStructure.add(bucket);
		}
        
        for (String columnname : allColumnsHavingAggregateCondition) {
        	List<Region> singleSplitPointRegions = new ArrayList<>();
            int columnIndex = sortedColumns.indexOf(columnname);
			for (int splitPointIndex = 0; splitPointIndex < bucketFloorsByColumns.get(columnname).size(); ++splitPointIndex) {
				BucketStructure bucketStructure = new BucketStructure(completeBucketStructure);
                Bucket bucketCorrespondingToColumn = bucketStructure.at(columnIndex);
                bucketCorrespondingToColumn.clear();
                bucketCorrespondingToColumn.add(splitPointIndex);
                Region region = new Region();
                region.add(bucketStructure);
                singleSplitPointRegions.add(region);
			}
			aggregateColumnsToSingleSplitPointRegions.put(columnname, singleSplitPointRegions);
		}
        return aggregateColumnsToSingleSplitPointRegions;
    }
    
    private Bucket getBucketInRange(int size) {
    	Bucket bucket = new Bucket();
    	for (int i = 0; i < size; ++i)
    		bucket.add(i);
		return bucket;
	}

    private Set<String> getAllColumnsHavingAggregateConditions(List<FormalCondition> conditions) {
    	Set<String> result = new HashSet<>();
        for (int i = 0; i < conditions.size(); ++i) {
        	FormalCondition condition = conditions.get(i);
			if (condition instanceof FormalConditionAggregate) {
				List<String> completeGroupKey = ((FormalConditionAggregate) condition).getGroupKey();
//				if (completeGroupKey.size() != 1)
//					throw new RuntimeException("Only 1-D projection supported!");
				String groupKey = completeGroupKey.get(0);
				result.add(groupKey);
			}
		}
        return result;
    }
    
    private Set<List<String>> getAllColumnsHavingAggregateConditionsNew(List<FormalCondition> conditions) {
    	Set<List<String>> result = new HashSet<>();
        for (int i = 0; i < conditions.size(); ++i) {
        	FormalCondition condition = conditions.get(i);
			if (condition instanceof FormalConditionAggregate) {
				List<String> groupKey = ((FormalConditionAggregate) condition).getGroupKey();
				Collections.sort(groupKey);
//				if (completeGroupKey.size() != 1)
//					throw new RuntimeException("Only 1-D projection supported!");
				//String groupKey = completeGroupKey.get(0);
				result.add(groupKey);
			}
		}
        return result;
    }
    
    private Set<FormalConditionOr> getConsistencyFilters(Set<String> commonAttribsSet, List<List<FormalCondition>> conditionsInSOPForm, List<boolean[]> allTrueBS, Map<String, IntList> bucketFloorsByColumns, List<String> sortedViewColumns) {
    	List<boolean[]> allTrueBSofCommonAttribs = new ArrayList<>();
    	List<boolean[]> allTrueBSofNotCommonAttribs = new ArrayList<>();
		Map<String, IntList> bucketFloorsByColumnsOfCommonAttribs = new HashMap<>();
		Map<String, IntList> bucketFloorsByColumnsOfNotCommonAttribs = new HashMap<>();
		
		List<String> commonAttribs = new ArrayList<>(commonAttribsSet);
		Collections.sort(commonAttribs);
		
		List<String> notCommonAttribs = new ArrayList<>(sortedViewColumns);
		notCommonAttribs.removeAll(commonAttribsSet);
		Collections.sort(notCommonAttribs);
		
		for (int k = 0; k < sortedViewColumns.size(); ++k) {
			String column = sortedViewColumns.get(k);
			if(commonAttribsSet.contains(column)) {
				allTrueBSofCommonAttribs.add(allTrueBS.get(k));
				bucketFloorsByColumnsOfCommonAttribs.put(column, bucketFloorsByColumns.get(column));
			}
			else {
				allTrueBSofNotCommonAttribs.add(allTrueBS.get(k));
				bucketFloorsByColumnsOfNotCommonAttribs.put(column, bucketFloorsByColumns.get(column));
			}
		}
    	
    	Set<FormalConditionOr> newConsistencyConstraints = new HashSet<>();
    	//getSOPSubconditions(condition)
    	for (List<FormalCondition> condition : conditionsInSOPForm) {	// For each condition
    		HashMap<FormalConditionAnd, FormalConditionOr> conditionMap = new HashMap<>();
    		for (FormalCondition subcondition : condition) {
    			FormalConditionAnd newConditionKept = new FormalConditionAnd();	// part of subcondition on common attribs
    			FormalConditionAnd newConditionDropped = new FormalConditionAnd();	// part of subcondition not on common attribs
    			if (subcondition instanceof FormalConditionAnd) {
    				for (FormalCondition simpleCondition : ((FormalConditionAnd)subcondition).getConditionList()) {
    					if(commonAttribsSet.contains(((FormalConditionSimpleInt)simpleCondition).getColumnname())) {
    						FormalCondition temp = simpleCondition.getDeepCopy();
    						temp.setOutputCardinality(-1);		// Make hashing consistent
    						newConditionKept.addCondition(temp);
    					}
    					else {
    						FormalCondition temp = simpleCondition.getDeepCopy();
    						temp.setOutputCardinality(-1);
    						newConditionDropped.addCondition(temp);
    					}
    		        }
                }
    			else if (subcondition instanceof FormalConditionSimpleInt) {
    				if(commonAttribsSet.contains(((FormalConditionSimpleInt)subcondition).getColumnname())) {
    					FormalCondition temp = subcondition.getDeepCopy();
						temp.setOutputCardinality(-1);		// Make hashing consistent
						newConditionKept.addCondition(temp);
					}
                }
    			else throw new RuntimeException("Unsupported FormalCondition of type" + subcondition.getClass() + " " + subcondition);
    			if(newConditionKept.size() != 0) {
    				if(!conditionMap.containsKey(newConditionKept))
    					conditionMap.put(newConditionKept, new FormalConditionOr());
	    			conditionMap.get(newConditionKept).addCondition(newConditionDropped);
    			}
			}
    		
    		HashMap<CustomBoolean, List<FormalConditionAnd>> areAllTrueToListKeptCondition = new HashMap<>();
    		for (Entry<FormalConditionAnd, FormalConditionOr> entry : conditionMap.entrySet()) {
    			FormalConditionAnd keptPart = entry.getKey();
				FormalConditionOr droppedPart = entry.getValue();
				
				for (FormalCondition subDroppedPart : droppedPart.getConditionList()) {
					List<boolean[]> bsCopyDroppedPart = deepCopyBS(allTrueBSofNotCommonAttribs);
			        setFalseAppropriateBuckets(subDroppedPart, notCommonAttribs, bsCopyDroppedPart, bucketFloorsByColumnsOfNotCommonAttribs);
			        CustomBoolean areAllTrue = new CustomBoolean(notCommonAttribs.size());
					for (int i = 0; i < bsCopyDroppedPart.size(); i++) {
						areAllTrue.set(i, getAreAllTrue(bsCopyDroppedPart.get(i)));
					}
					if(!areAllTrueToListKeptCondition.containsKey(areAllTrue))
						areAllTrueToListKeptCondition.put(areAllTrue, new ArrayList<>());
					areAllTrueToListKeptCondition.get(areAllTrue).add(keptPart);
				}		        
			}
    		for (Entry<CustomBoolean, List<FormalConditionAnd>> entry : areAllTrueToListKeptCondition.entrySet()) {
				List<FormalConditionAnd> keptPartList = entry.getValue();
				FormalConditionOr conditionToAdd = new FormalConditionOr();
				for (FormalConditionAnd formalConditionAnd : keptPartList) {
					conditionToAdd.addCondition(formalConditionAnd);
				}
				newConsistencyConstraints.add(conditionToAdd);
			}
    	}
        return newConsistencyConstraints;
    }

    private Set<FormalConditionOr> getConsistencyFiltersProjection(Set<String> commonAttribsSet, List<FormalCondition> conditionsInSOPForm, List<boolean[]> allTrueBS, Map<String, IntList> bucketFloorsByColumns, List<String> sortedViewColumns) {
    	List<boolean[]> allTrueBSofCommonAttribs = new ArrayList<>();
    	List<boolean[]> allTrueBSofNotCommonAttribs = new ArrayList<>();
		Map<String, IntList> bucketFloorsByColumnsOfCommonAttribs = new HashMap<>();
		Map<String, IntList> bucketFloorsByColumnsOfNotCommonAttribs = new HashMap<>();
		
		List<String> commonAttribs = new ArrayList<>(commonAttribsSet);
		Collections.sort(commonAttribs);
		
		List<String> notCommonAttribs = new ArrayList<>(sortedViewColumns);
		notCommonAttribs.removeAll(commonAttribsSet);
		Collections.sort(notCommonAttribs);
		
		for (int k = 0; k < sortedViewColumns.size(); ++k) {
			String column = sortedViewColumns.get(k);
			if(commonAttribsSet.contains(column)) {
				allTrueBSofCommonAttribs.add(allTrueBS.get(k));
				bucketFloorsByColumnsOfCommonAttribs.put(column, bucketFloorsByColumns.get(column));
			}
			else {
				allTrueBSofNotCommonAttribs.add(allTrueBS.get(k));
				bucketFloorsByColumnsOfNotCommonAttribs.put(column, bucketFloorsByColumns.get(column));
			}
		}
    	
    	Set<FormalConditionOr> newConsistencyConstraints = new HashSet<>();
    	//getSOPSubconditions(condition)
    	for (FormalCondition conditionIter : conditionsInSOPForm) {	// For each condition
    		List<FormalCondition>condition=getSOPSubconditions(conditionIter);
    		HashMap<FormalConditionAnd, FormalConditionOr> conditionMap = new HashMap<>();
    		for (FormalCondition subcondition : condition) {
    			FormalConditionAnd newConditionKept = new FormalConditionAnd();	// part of subcondition on common attribs
    			FormalConditionAnd newConditionDropped = new FormalConditionAnd();	// part of subcondition not on common attribs
    			if (subcondition instanceof FormalConditionAnd) {
    				for (FormalCondition simpleCondition : ((FormalConditionAnd)subcondition).getConditionList()) {
    					if(commonAttribsSet.contains(((FormalConditionSimpleInt)simpleCondition).getColumnname())) {
    						FormalCondition temp = simpleCondition.getDeepCopy();
    						temp.setOutputCardinality(-1);		// Make hashing consistent
    						newConditionKept.addCondition(temp);
    					}
    					else {
    						FormalCondition temp = simpleCondition.getDeepCopy();
    						temp.setOutputCardinality(-1);
    						newConditionDropped.addCondition(temp);
    					}
    		        }
                }
    			else if (subcondition instanceof FormalConditionSimpleInt) {
    				if(commonAttribsSet.contains(((FormalConditionSimpleInt)subcondition).getColumnname())) {
    					FormalCondition temp = subcondition.getDeepCopy();
						temp.setOutputCardinality(-1);		// Make hashing consistent
						newConditionKept.addCondition(temp);
					}
                }
    			else throw new RuntimeException("Unsupported FormalCondition of type" + subcondition.getClass() + " " + subcondition);
    			if(newConditionKept.size() != 0) {
    				if(!conditionMap.containsKey(newConditionKept))
    					conditionMap.put(newConditionKept, new FormalConditionOr());
	    			conditionMap.get(newConditionKept).addCondition(newConditionDropped);
    			}
			}
    		
    		HashMap<CustomBoolean, List<FormalConditionAnd>> areAllTrueToListKeptCondition = new HashMap<>();
    		for (Entry<FormalConditionAnd, FormalConditionOr> entry : conditionMap.entrySet()) {
    			FormalConditionAnd keptPart = entry.getKey();
				FormalConditionOr droppedPart = entry.getValue();
				
				for (FormalCondition subDroppedPart : droppedPart.getConditionList()) {
					List<boolean[]> bsCopyDroppedPart = deepCopyBS(allTrueBSofNotCommonAttribs);
			        setFalseAppropriateBuckets(subDroppedPart, notCommonAttribs, bsCopyDroppedPart, bucketFloorsByColumnsOfNotCommonAttribs);
			        CustomBoolean areAllTrue = new CustomBoolean(notCommonAttribs.size());
					for (int i = 0; i < bsCopyDroppedPart.size(); i++) {
						areAllTrue.set(i, getAreAllTrue(bsCopyDroppedPart.get(i)));
					}
					if(!areAllTrueToListKeptCondition.containsKey(areAllTrue))
						areAllTrueToListKeptCondition.put(areAllTrue, new ArrayList<>());
					areAllTrueToListKeptCondition.get(areAllTrue).add(keptPart);
				}		        
			}
    		for (Entry<CustomBoolean, List<FormalConditionAnd>> entry : areAllTrueToListKeptCondition.entrySet()) {
				List<FormalConditionAnd> keptPartList = entry.getValue();
				FormalConditionOr conditionToAdd = new FormalConditionOr();
				for (FormalConditionAnd formalConditionAnd : keptPartList) {
					conditionToAdd.addCondition(formalConditionAnd);
				}
				newConsistencyConstraints.add(conditionToAdd);
			}
    	}
        return newConsistencyConstraints;
    }

    private boolean getAreAllTrue(boolean[] bs) {
		for (boolean b : bs) {
			if(!b)
				return false;
		}
		return true;
	}

    private static void debugSolvingErrorPerCondition(List<FormalCondition> conditions, ViewSolution viewSolution, List<String> sortedColumns) {
        if (!DebugHelper.solvingErrorCheckNeeded())
            return;

        DebugHelper.printDebug("Evaluating sampling/merging errors per condition");
        ConditionsEvaluator.debugErrorPerConditionWithException(conditions, viewSolution, sortedColumns);
    }

    //    private void exportToFile(String viewname, Set<String> nonKeys, CliqueSolutionInMemory cliqueSolution) {
    //
    //        try {
    //            List<String> sortedColumnnames = new ArrayList<>(nonKeys);
    //            Collections.sort(sortedColumnnames);
    //
    //            List<String> chosenColumns = new ArrayList<>();
    //            for (int i : cliqueSolution.getColIndexes()) {
    //                chosenColumns.add(sortedColumnnames.get(i));
    //            }
    //
    //            FileWriter fw = new FileWriter(new File("/home/dsladmin/CODD/RaghavSood/intermediateViewSolution", viewname));
    //            String header = "COUNT," + StringUtils.join(chosenColumns, ",") + "\n";
    //            fw.write(header);
    //
    //            for (ValueCombination valueCombination : cliqueSolution.getValueCombinations()) {
    //                fw.write(valueCombination.toStringFileDump() + "\n");
    //            }
    //
    //            fw.close();
    //        } catch (IOException ex) {
    //            throw new RuntimeException("File writing error", ex);
    //        }
    //
    //    }

    private List<Region> getConditionRegions(List<List<FormalCondition>> conditionsInSOPForm, List<boolean[]> allTrueBS, List<String> sortedColumns, Map<String, IntList> bucketFloorsByColumns) {
        List<Region> conditionRegions = new ArrayList<>();
        for (int i = 0; i < conditionsInSOPForm.size(); i++) {
//            FormalCondition condition = conditions.get(i);
//            List<FormalCondition> subconditions = getSOPSubconditions(condition);
        	List<FormalCondition> subconditions = conditionsInSOPForm.get(i);
            Region conditionRegion = new Region();
            
//            if (subconditions.size() == 0) {	// Base condition
//            	List<boolean[]> bsCopy = deepCopyBS(allTrueBS);
//            }
            
            for (FormalCondition subcondition : subconditions) {	// which buckets follow this particular subcondition
                List<boolean[]> bsCopy = deepCopyBS(allTrueBS);

                //Unmarking false buckets
                setFalseAppropriateBuckets(subcondition, sortedColumns, bsCopy, bucketFloorsByColumns);//assert: every element of bucketStructures has at least one true entry

                BucketStructure subConditionBS = new BucketStructure();
                for (int j = 0; j < bsCopy.size(); j++) {
                    Bucket bucket = new Bucket();
                    for (int k = 0; k < bsCopy.get(j).length; k++) {
                        if (bsCopy.get(j)[k]) {
                            bucket.add(k);	// What split points of this dimension follow this sub constraint (Important Note: index of split points in bucketFloorsByColumns is being added in bucket instead of split point value)
                        }
                    }
                    subConditionBS.add(bucket);	// For particular dimension
                }
                conditionRegion.add(subConditionBS);	// For every subcondition in condition
            }
            conditionRegions.add(conditionRegion);	// For every condition
        }
        return conditionRegions;
    }
    private List<Region> getConditionRegionsTemp(List<FormalCondition> conditions, List<boolean[]> allTrueBS, List<String> sortedColumns, Map<String, IntList> bucketFloorsByColumns) {
        List<Region> conditionRegions = new ArrayList<>();
        for (int i = 0; i < conditions.size(); i++) {
            FormalCondition condition = conditions.get(i);
            List<FormalCondition> subconditions = getSOPSubconditions(condition);
//        	List<FormalCondition> subconditions = conditions.get(i);
            Region conditionRegion = new Region();
            
//            if (subconditions.size() == 0) {	// Base condition
//            	List<boolean[]> bsCopy = deepCopyBS(allTrueBS);
//            }
            
            for (FormalCondition subcondition : subconditions) {	// which buckets follow this particular subcondition
                List<boolean[]> bsCopy = deepCopyBS(allTrueBS);

                //Unmarking false buckets
                setFalseAppropriateBuckets(subcondition, sortedColumns, bsCopy, bucketFloorsByColumns);//assert: every element of bucketStructures has at least one true entry

                BucketStructure subConditionBS = new BucketStructure();
                for (int j = 0; j < bsCopy.size(); j++) {
                    Bucket bucket = new Bucket();
                    for (int k = 0; k < bsCopy.get(j).length; k++) {
                        if (bsCopy.get(j)[k]) {
                            bucket.add(k);	// What split points of this dimension follow this sub constraint (Important Note: index of split points in bucketFloorsByColumns is being added in bucket instead of split point value)
                        }
                    }
                    subConditionBS.add(bucket);	// For particular dimension
                }
                conditionRegion.add(subConditionBS);	// For every subcondition in condition
            }
            conditionRegions.add(conditionRegion);	// For every condition
        }
        return conditionRegions;
    }
    
    private Region getConditionRegionsSingle(FormalCondition condition, List<boolean[]> allTrueBS, List<String> sortedColumns, Map<String, IntList> bucketFloorsByColumns) {
        
        
      
        	List<FormalCondition> subconditions = getSOPSubconditions(condition);
            Region conditionRegion = new Region();
            
            for (FormalCondition subcondition : subconditions) {	// which buckets follow this particular subcondition
                List<boolean[]> bsCopy = deepCopyBS(allTrueBS);

                //Unmarking false buckets
                setFalseAppropriateBuckets(subcondition, sortedColumns, bsCopy, bucketFloorsByColumns);//assert: every element of bucketStructures has at least one true entry

                BucketStructure subConditionBS = new BucketStructure();
                for (int j = 0; j < bsCopy.size(); j++) {
                    Bucket bucket = new Bucket();
                    for (int k = 0; k < bsCopy.get(j).length; k++) {
                        if (bsCopy.get(j)[k]) {
                            bucket.add(k);	// What split points of this dimension follow this sub constraint (Important Note: index of split points in bucketFloorsByColumns is being added in bucket instead of split point value)
                        }
                    }
                    subConditionBS.add(bucket);	// For particular dimension
                }
                conditionRegion.add(subConditionBS);	// For every subcondition in condition
            }
            return conditionRegion;
    }

    private List<FormalCondition> getSOPSubconditions(FormalCondition condition) {
        FormalConditionSOP sopCondition = new FormalConditionSOP(condition);
        return sopCondition.getConditionList();
    }

    private static void setFalseAppropriateBuckets(FormalCondition condition, List<String> sortedColumns, List<boolean[]> bucketStructures,
            Map<String, IntList> bucketFloorsByColumns) {

        if (condition instanceof FormalConditionAnd) {
            setFalseAppropriateBuckets((FormalConditionAnd) condition, sortedColumns, bucketStructures, bucketFloorsByColumns);
            return;
        }
        if (condition instanceof FormalConditionSimpleInt) {
            setFalseAppropriateBuckets((FormalConditionSimpleInt) condition, sortedColumns, bucketStructures, bucketFloorsByColumns);
            return;
        }
        throw new RuntimeException("Unsupported FormalCondition of type" + condition.getClass() + " " + condition);
    }

    private static void setFalseAppropriateBuckets(FormalConditionAnd andCondition, List<String> sortedColumns, List<boolean[]> bucketStructures,
            Map<String, IntList> bucketFloorsByColumns) {
        for (FormalCondition condition : andCondition.getConditionList()) {
            setFalseAppropriateBuckets(condition, sortedColumns, bucketStructures, bucketFloorsByColumns);
        }
    }

    private static void setFalseAppropriateBuckets(FormalConditionSimpleInt simpleCondition, List<String> sortedColumns, List<boolean[]> bucketStructures,
            Map<String, IntList> bucketFloorsByColumns) {

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

    
    /*  
     * 	  Added by Tarun
     *    Solves all views stage by stage
     * 
     */
	public Map<String, ViewSolution> solveAllView(List<ViewInfo> viewInfoList, List<List<FormalCondition>> conditionsList,
			List<String> sortedViewNames, int scaleFactor) {
		
		//StopWatch preprocessViewSW = new StopWatch("Pre-Processing All view");
		
		// Stage 1 : Create BucketIntervals and allTrueBS for all views 
        List<Map<String,IntList>> bucketFloorsByColumnsViewList = new ArrayList<>();
        List<List<boolean[]>> allTrueBSViewList = new ArrayList<>();
        List<List<String>> sortedViewColumnsList = new ArrayList<>();
        double TotalVarcountUR = 0;
        for(int i=0; i< sortedViewNames.size(); i++)
        {
        	Map<String, IntList> bucketFloorsByColumns = DomainDecomposer.getBucketFloors(conditionsList.get(i), viewInfoList.get(i));
        	final List<String> sortedViewColumns = new ArrayList<>(bucketFloorsByColumns.keySet());
            Collections.sort(sortedViewColumns);
            
            //Marking buckets for each column as true
            double varcountUR = 1;
            final List<boolean[]> allTrueBS = new ArrayList<>();
            for (String column : sortedViewColumns) {
                int length = bucketFloorsByColumns.get(column).size();
                boolean[] arr = new boolean[length];
                varcountUR *= length;

                Arrays.fill(arr, true);
                allTrueBS.add(arr);
            }
            
            TotalVarcountUR += varcountUR; 
            
        	bucketFloorsByColumnsViewList.add(bucketFloorsByColumns);
        	allTrueBSViewList.add(allTrueBS);
        	sortedViewColumnsList.add(sortedViewColumns);
        	        	
        }
        
        DebugHelper.printInfo("Number of variables without reduction for all views is " + TotalVarcountUR);
        
        
        
        
        if (solverType == SolverType.DOUBLE) {
            //NOTE: Scaling only to extent when cardinality value stays within permissible range of long
            //NOTE: Scaling such that rowcount of any PK table goes beyond range of int will introduce negative values in DatabaseSummary.
            //TODO : This is not yet fixed.

            //long scaleFactor = (long) 1e10;
            DebugHelper.printInfo("SCALING by " + scaleFactor);
            for(int i=0; i< sortedViewNames.size(); i++) {
            	viewInfoList.get(i).scaleRowCount(scaleFactor);
            	List<FormalCondition> conditions = conditionsList.get(i);
            	for (FormalCondition condition : conditions) {
                     condition.scaleRowCount(scaleFactor);
            	
            	}      
           
            }
        }
        
        
        
        
        // Stage 2 :  Clique formation and it's reduction
        List<List<Set<String>>> arasuCliquesViewList = new ArrayList<>();
        List<CliqueFinder> cliqueReducerList = new ArrayList<>();
        long totalVarCountAR = 0;
        for(int i=0; i< sortedViewNames.size(); i++) {
        	 	
        	CliqueFinder cliqueReducer = new CliqueFinder(viewInfoList.get(i), allTrueBSViewList.get(i));
		    	
        }
        
		
		
		DebugHelper.printInfo("Total Number of variables for all views using Arasu's clique based reduction " + totalVarCountAR);
		
		List<Map<String, List<Region>>> aggregateColumnsToSingleSplitPointRegionsViewList = new ArrayList<>();
		List<List<List<FormalCondition>>> conditionsInSOPFormViewList =  new ArrayList<>();
		List<FormalConditionOr[]> consistencyConstraintsViewList = new ArrayList<>();
		List<Integer> totalAggregateConditionsViewList = new ArrayList<>();
		for(int v=0; v< sortedViewNames.size(); v++) {
			
			List<String> sortedViewColumns = sortedViewColumnsList.get(v);
			Map<String, IntList> bucketFloorsByColumns = bucketFloorsByColumnsViewList.get(v);
			List<boolean[]> allTrueBS = allTrueBSViewList.get(v);
			
			int totalAggregateConditions = 0;
			List<List<FormalCondition>> conditionsInSOPForm = new ArrayList<>();
			
			
			List<FormalCondition> conditions = conditionsList.get(v);
			for (FormalCondition condition : conditions) {
				if (condition instanceof FormalConditionAggregate) {
					totalAggregateConditions++;
				}
				List<FormalCondition> conditionInSOPForm = getSOPSubconditions(condition);	// Note : outputCardinality and projection information is lost
				conditionsInSOPForm.add(conditionInSOPForm);
			}
			
			FormalConditionOr consistencyConstraints[] = new FormalConditionOr[0];
			
			
			if (consistencyMakerType == ConsistencyMakerType.CONSISTENCYFILTERS) {
			    StopWatch getProjectedConditionsSW = new StopWatch("getTruncatedConditions for Consistency Filters");
			    
			    HashMap<FormalConditionOr, Integer> consistencyConstraintsToIndex = new HashMap<>();
			    int newCCIndex = 0;
			    
			    Set<Set<String>> processedCommonAttribs = new HashSet<>();
			    List<Set<String>> arasuCliques = arasuCliquesViewList.get(v);
			    
			    for (int i = 0; i < arasuCliques.size(); i++) {
			    	Set<String> c1 = arasuCliques.get(i);
			    	for (int j = i + 1; j < arasuCliques.size(); j++) {
			    		Set<String> c2 = arasuCliques.get(j);
			    		Set<String> commonAttribsSet = new HashSet<>(c1);
			    		commonAttribsSet.retainAll(c2);
			    		if(!commonAttribsSet.isEmpty()) {
			    			if(processedCommonAttribs.contains(commonAttribsSet))
			    				continue;
			    			else
			    				processedCommonAttribs.add(commonAttribsSet);
			    			Set<FormalConditionOr> newConsistencyConstraints = getConsistencyFilters(commonAttribsSet, conditionsInSOPForm, allTrueBS, bucketFloorsByColumns, sortedViewColumns);
			    			for (FormalConditionOr formalConditionOr : newConsistencyConstraints) {
			    				if(!consistencyConstraintsToIndex.containsKey(formalConditionOr)) {		 // TODO: hashCode and equals of FormalConditionComposite are used and it's not good because it uses Condition list instead of Condition set
			    					consistencyConstraintsToIndex.put(formalConditionOr, newCCIndex);
			    					newCCIndex++;
			    				}
							}
			    		}
					}
				}
			//        Converting to list to maintain ordering
			    consistencyConstraints = new FormalConditionOr[consistencyConstraintsToIndex.size()];
			    for (Entry<FormalConditionOr, Integer> entry : consistencyConstraintsToIndex.entrySet()) {
					consistencyConstraints[entry.getValue()] = entry.getKey();
				}
			    
			    for (FormalConditionOr formalCondition : consistencyConstraints) {
			    	conditionsInSOPForm.add(formalCondition.getConditionList());
				}
			
			    getProjectedConditionsSW.displayTimeAndDispose();
			}
			
			conditionsInSOPFormViewList.add(conditionsInSOPForm);
			totalAggregateConditionsViewList.add(totalAggregateConditions);
			consistencyConstraintsViewList.add(consistencyConstraints);
			
			// 1-D projection handling -- creating constraints (regions) which will split subviews along the split points of group key
			Set<String> allColumnsHavingAggregateCondition = getAllColumnsHavingAggregateConditions(conditions);

			Map<String, List<Region>> aggregateColumnsToSingleSplitPointRegions = getSingleSplitPointRegions(sortedViewColumns, bucketFloorsByColumns, allColumnsHavingAggregateCondition);
			aggregateColumnsToSingleSplitPointRegionsViewList.add(aggregateColumnsToSingleSplitPointRegions);
		}	

		/*
		 *
		 *  Dharmendra's code of consistency filters + regions ends here
		 *  
		 */
		
		
		/*
		 *    Stage 4 : Condition Region Creation
		 * 
		 */
		
		 List<List<Region>> conditionRegionsViewList = new ArrayList<>();
		 for(int i=0; i < sortedViewNames.size();i++) {
			 
			List<String> sortedViewColumns = sortedViewColumnsList.get(i);
			Map<String, IntList> bucketFloorsByColumns = bucketFloorsByColumnsViewList.get(i);
			List<boolean[]> allTrueBS = allTrueBSViewList.get(i);
				
			List<List<FormalCondition>> conditionsInSOPForm = conditionsInSOPFormViewList.get(i);
			FormalConditionOr[] consistencyConstraints = consistencyConstraintsViewList.get(i);
			Integer totalAggregateConditions = totalAggregateConditionsViewList.get(i);
			
			List<Region> conditionRegions = getConditionRegions(conditionsInSOPForm, allTrueBS, sortedViewColumns, bucketFloorsByColumns);
			conditionRegionsViewList.add(conditionRegions);
			DebugHelper.printInfo("For view :  " + sortedViewNames.get(i));
			DebugHelper.printInfo("Number of cardinality constraints " + (conditionRegions.size() - consistencyConstraints.length + 1)); //All 1's which is added later is included in the count here
		    DebugHelper.printInfo("Number of projection constraints " + totalAggregateConditions);		
		    
			 
		 }
		 
		 
		 /*
		  *   Stage 5 : Create Partitions 
		  *   
		  * 
		  */
		 
		 
		 List<DoubleReductionBasedViewSolver> solverObjViewList = new ArrayList<>();		 
		 for(int i=0; i < sortedViewNames.size();i++)
		 {
			 
			 String viewname = sortedViewNames.get(i);
			 ViewInfo viewInfo = viewInfoList.get(i);
			 List<boolean[]> allTrueBS = allTrueBSViewList.get(i);
			 List<Set<String>> arasuCliques = arasuCliquesViewList.get(i);
			 Map<String, IntList> bucketFloorsByColumns = bucketFloorsByColumnsViewList.get(i);			 
			 List<FormalCondition> conditions = conditionsList.get(i);
			 List<Region> conditionRegions = conditionRegionsViewList.get(i);
			 List<String> sortedViewColumns = sortedViewColumnsList.get(i);
			 FormalConditionOr[] consistencyConstraints = consistencyConstraintsViewList.get(i);
			 Map<String, List<Region>> aggregateColumnsToSingleSplitPointRegions = aggregateColumnsToSingleSplitPointRegionsViewList.get(i);
			 
		     switch (solverType) {
		            case ARASU:
		                ArasuReductionBasedViewSolver arasuSolver =
		                        new ArasuReductionBasedViewSolver(viewname, viewInfo, allTrueBS, arasuCliques, bucketFloorsByColumns, spillType);
		                //preprocessViewSW.displayTimeAndDispose();
		                //fullViewSolution = arasuSolver.solveView(conditions, new ArrayList<>(conditionRegions), null, null, null);
		                break;

		            case DOUBLE:
		                DoubleReductionBasedViewSolver doubleSolver =
		                        new DoubleReductionBasedViewSolver(viewname, viewInfo, allTrueBS, arasuCliques, bucketFloorsByColumns);
		                //preprocessViewSW.displayTimeAndDispose();
		                //fullViewSolution = doubleSolver.solveView(conditions, new ArrayList<>(conditionRegions), consistencyConstraints, consistencyMakerType, aggregateColumnsToSingleSplitPointRegions);
		                // fetch partition add to some partition list
		                // generates LPs to be done in two way
		                // Merge solution 
		                doubleSolver.solveView1(conditions, new ArrayList<>(conditionRegions), consistencyConstraints, consistencyMakerType, aggregateColumnsToSingleSplitPointRegions);
		                solverObjViewList.add(doubleSolver);
		                //doubleSolver.mergeCliques(cliqueIdxToVarValuesList);
		                //debugSolvingErrorPerCondition(conditions, fullViewSolution, sortedViewColumns);
		                break;

		            default:
		                throw new RuntimeException("Unsupported SolverType " + solverType);
		      }
		      
		     
		      
		 }
		 
		 
		 /*
		  * 
		  *   Stage 6 : Form and Solve LPs for all views
		  * 
		  */
		 
		 /*
		 List<List<LinkedList<VariableValuePair>>> cliqueIdxToVarValuesListViewList = new ArrayList<>();  
		 for(int i=0; i < sortedViewNames.size();i++) {
			 
			 DoubleReductionBasedViewSolver doubleSolver = solverObjViewList.get(i);
			 FormalConditionOr[] consistencyConstraints = consistencyConstraintsViewList.get(i);
			 List<FormalCondition> conditions = conditionsList.get(i);
			 Map<String, List<Region>> aggregateColumnsToSingleSplitPointRegions = aggregateColumnsToSingleSplitPointRegionsViewList.get(i);
			 
			 
			 switch (solverType) {
	            case ARASU:
	                
	            	// Not doing for ARASU
	            	
	            	//ArasuReductionBasedViewSolver arasuSolver =
	                 //       new ArasuReductionBasedViewSolver(viewname, viewInfo, allTrueBS, arasuCliques, bucketFloorsByColumns, spillType);
	                //preprocessViewSW.displayTimeAndDispose();
	                //fullViewSolution = arasuSolver.solveView(conditions, new ArrayList<>(conditionRegions), null, null, null);
	                break;

	            case DOUBLE:
	                
	                //preprocessViewSW.displayTimeAndDispose();
	                //fullViewSolution = doubleSolver.solveView(conditions, new ArrayList<>(conditionRegions), consistencyConstraints, consistencyMakerType, aggregateColumnsToSingleSplitPointRegions);
	                // fetch partition add to some partition list
	                // generates LPs to be done in two way
	                // Merge solution 
	                
	                List<LinkedList<VariableValuePair>> cliqueIdxToVarValuesList = doubleSolver.formAndSolveLP(consistencyMakerType, consistencyConstraints, conditions, aggregateColumnsToSingleSplitPointRegions);
	                cliqueIdxToVarValuesListViewList.add(cliqueIdxToVarValuesList);
	                //doubleSolver.mergeCliques(cliqueIdxToVarValuesList);
	                //debugSolvingErrorPerCondition(conditions, fullViewSolution, sortedViewColumns);
	                break;

	            default:
	                throw new RuntimeException("Unsupported SolverType " + solverType);
	      }
			 
		 }
		 */
		 List<List<LinkedList<VariableValuePair>>> cliqueIdxToVarValuesListViewList = new ArrayList<>(); 
		 
		 LPSolvingType lpSolvingStyle = LPSolvingType.SINGLE; // for single view LP solving
		 //LPSolvingType lpSolvingStyle = LPSolvingType.ALL; // for all view LP solving
		 switch (solverType) {
         case ARASU:
             
         	// Not doing for ARASU
         	
         	//ArasuReductionBasedViewSolver arasuSolver =
              //       new ArasuReductionBasedViewSolver(viewname, viewInfo, allTrueBS, arasuCliques, bucketFloorsByColumns, spillType);
             //preprocessViewSW.displayTimeAndDispose();
             //fullViewSolution = arasuSolver.solveView(conditions, new ArrayList<>(conditionRegions), null, null, null);
             break;

         case DOUBLE:
             
             //preprocessViewSW.displayTimeAndDispose();
             //fullViewSolution = doubleSolver.solveView(conditions, new ArrayList<>(conditionRegions), consistencyConstraints, consistencyMakerType, aggregateColumnsToSingleSplitPointRegions);
             // fetch partition add to some partition list
             // generates LPs to be done in two way
             // Merge solution 
        	 
        	 
        	 
        	 if(lpSolvingStyle == LPSolvingType.SINGLE)
        	 {
        		 for(int i=0; i < sortedViewNames.size();i++) {
        			 
        			 DoubleReductionBasedViewSolver doubleSolver = solverObjViewList.get(i);
        			 FormalConditionOr[] consistencyConstraints = consistencyConstraintsViewList.get(i);
        			 List<FormalCondition> conditions = conditionsList.get(i);
        			 Map<String, List<Region>> aggregateColumnsToSingleSplitPointRegions = aggregateColumnsToSingleSplitPointRegionsViewList.get(i);
        			 
    	             
//    	             List<LinkedList<VariableValuePair>> cliqueIdxToVarValuesList = doubleSolver.formAndSolveLP(consistencyMakerType, consistencyConstraints, conditions, aggregateColumnsToSingleSplitPointRegions);
//    	             cliqueIdxToVarValuesListViewList.add(cliqueIdxToVarValuesList);
                 
        		 }
        	 }
        	 else if(lpSolvingStyle == LPSolvingType.ALL)
        	 {
        		 // function to be made 
        	 }
        	  
    		 
             //doubleSolver.mergeCliques(cliqueIdxToVarValuesList);
             //debugSolvingErrorPerCondition(conditions, fullViewSolution, sortedViewColumns);
             break;

         default:
             throw new RuntimeException("Unsupported SolverType " + solverType);
		 }

		 
		 
		 
		 
		 /*
		  *   Stage 7 : Aligning and Merging Subviews(Cliques) + making map for viewname ---> viewSolution	  * 
		  * 
		  */
		 Map<String, ViewSolution> viewSolutionsMap = new HashMap<>();
		 for(int i=0; i < sortedViewNames.size();i++) {
			 
			 ViewSolution fullViewSolution = null;
			 DoubleReductionBasedViewSolver doubleSolver = solverObjViewList.get(i);
			 List<LinkedList<VariableValuePair>> cliqueIdxToVarValuesList = cliqueIdxToVarValuesListViewList.get(i);
			 
			 fullViewSolution = doubleSolver.mergeCliques(cliqueIdxToVarValuesList);
			 viewSolutionsMap.put(sortedViewNames.get(i),fullViewSolution); 
			 
		 }
		 			 
		 return viewSolutionsMap;       
		
	}

    
    

	
}

class CustomBoolean {
	public static final int SEED = 173;
	public static final int PRIME = 37;
	
	boolean booleanArray[];
	
	public void set(int i, boolean data) {
		booleanArray[i] = data;
	}
	
	public boolean get(int i) {
		return booleanArray[i];
	}
	
	public CustomBoolean(int size) {
		booleanArray = new boolean[size];
	}
	
	public int hash(int seed, boolean aBoolean) {
		return (PRIME * seed) + (aBoolean ? 1231 : 1237);
	}

	public int hash(int seed) {
		if (booleanArray == null) {
			return 0;
		}
		for (boolean aBoolean : booleanArray) {
			seed = hash(seed, aBoolean);
	    }
	    return seed;
	}
	  
	@Override
	public boolean equals(Object obj) {
	    if (obj == null) {
	        return false;
	    }
	    if (!CustomBoolean.class.isAssignableFrom(obj.getClass())) {
	        return false;
	    }
	    final CustomBoolean other = (CustomBoolean) obj;
	    if(this.hashCode() == other.hashCode())
	    	return true;
	    else
	    	return false;
	}

	@Override
	public int hashCode() {
		return hash(SEED);
	}
}
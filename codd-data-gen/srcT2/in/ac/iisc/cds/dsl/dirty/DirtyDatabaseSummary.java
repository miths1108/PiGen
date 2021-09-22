package in.ac.iisc.cds.dsl.dirty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.ac.iisc.cds.dsl.cdgvendor.constants.PostgresVConfig;
import in.ac.iisc.cds.dsl.cdgvendor.model.ValueCombination;
import in.ac.iisc.cds.dsl.cdgvendor.model.ViewInfo;
import in.ac.iisc.cds.dsl.cdgvendor.model.ViewSolution;
import in.ac.iisc.cds.dsl.cdgvendor.model.ViewSolutionInMemory;
import in.ac.iisc.cds.dsl.cdgvendor.model.ViewSolutionWithSolverStats;
import in.ac.iisc.cds.dsl.cdgvendor.utils.DebugHelper;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;

@Deprecated
public class DirtyDatabaseSummary {
	
	private final Map<String, DirtyViewSolution> viewSolutions;
	private boolean isConsistent;
	
	private static DirtyDatabaseSummary instance;
	static {
		instance = new DirtyDatabaseSummary();
	}

	public static DirtyDatabaseSummary getInstance() {
		return instance;
	}
	
	public DirtyDatabaseSummary() {
		viewSolutions = new HashMap<>();;
		isConsistent = false;
	}
	
	public void addSummary(String viewname, DirtyViewSolution viewSolution) {
		viewSolutions.put(viewname, viewSolution);
	}
	
	public void makeFKeyConsistency() {
		if (isConsistent) {
            DebugHelper.printError("Received extra call to makeViewConsistency while it is already made consistent");
            return;
        }

        for (DirtyViewSolution viewSolution : viewSolutions.values()) {
        	viewSolution.prepareForSearch();
//            if (viewSolution instanceof ViewSolutionInMemory) {
//                ((ViewSolutionInMemory) viewSolution).prepareForSearch();
//            } else if (viewSolution instanceof ViewSolutionWithSolverStats) {
//                ((ViewSolutionWithSolverStats) viewSolution).prepareForSearch();
//            }
        }

        Map<String, ViewInfo> viewInfos = PostgresVConfig.ANONYMIZED_VIEWINFOs;
        List<String> topoViewnames = PostgresVConfig.VIEWNAMES_TOPO;

        //Checking Views in topological order and adding extra tuple to fkeyViews whenever needed
        for (int i = 0; i < topoViewnames.size() - 1; i++) { //Last one won't require addition of any extra tuple to its children because it doesn't have any!
            String fkViewname = topoViewnames.get(i);
            ViewInfo fkViewInfo = viewInfos.get(fkViewname);
            List<String> fkSortedColumns = new ArrayList<>(fkViewInfo.getViewNonkeys());
            Collections.sort(fkSortedColumns);
            DirtyViewSolution fkViewSolution = viewSolutions.get(fkViewname);

            DebugHelper.printDebug("Ensuring view consistency for pkviews of " + fkViewname);

            for (String pkViewname : fkViewInfo.getFkeyViews()) {
                ViewInfo pkViewInfo = viewInfos.get(pkViewname);
                List<String> sortedCommonColumns = new ArrayList<>(pkViewInfo.getViewNonkeys()); //All columns of pkViewInfo should infact be the common columns between it and viewInfo
                Collections.sort(sortedCommonColumns);

                //DebugHelper.printDebug("\tEnsuring view consistency for pkview " + pkViewname);

                IntList posOfCommonColumns = new IntArrayList(sortedCommonColumns.size());
                for (int j = 0; j < fkSortedColumns.size(); j++) {
                    if (sortedCommonColumns.contains(fkSortedColumns.get(j))) {
                        posOfCommonColumns.add(j);
                    }
                }

                DirtyViewSolution pkViewSolution = viewSolutions.get(pkViewname);

                /*
                for (DirtyValueCombination fkValueCombination : fkViewSolution) {
                    IntList seekedValuesInCombination = new IntArrayList(posOfCommonColumns.size());
                    for (IntIterator iter = posOfCommonColumns.iterator(); iter.hasNext();) {
                        int pos = iter.nextInt();
                        seekedValuesInCombination.add(fkValueCombination.getColValues().getInt(pos));
                    }

                    if (!pkViewSolution.contains(seekedValuesInCombination)) {
//                    	DirtyValueCombination toaddValueCombination = new DirtyValueCombination(seekedValuesInCombination, 1); //Adding extra valueCombination with count 1
//                        pkViewSolution.addValueCombination(toaddValueCombination);
                    }
                    else {
                    	DirtyValueCombination pkValueCombination = pkViewSolution.getValueCombination(seekedValuesInCombination);
                    	System.out.println();
                    }
                }*/
            }
        }
        isConsistent = true;

//        if (DebugHelper.viewConsistencyErrorCheckNeeded()) {
//            DebugHelper.debugSummaryRowcounts(viewSolutions);
//        }
	}
}

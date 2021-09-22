package in.ac.iisc.cds.dsl.cdgvendor.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import in.ac.iisc.cds.dsl.cdgvendor.constants.Constants;
import in.ac.iisc.cds.dsl.cdgvendor.constants.PostgresVConfig;
import in.ac.iisc.cds.dsl.cdgvendor.model.Alqp;
import in.ac.iisc.cds.dsl.cdgvendor.model.CliqueSolutionInMemory;
import in.ac.iisc.cds.dsl.cdgvendor.model.Condition;
import in.ac.iisc.cds.dsl.cdgvendor.model.Constraint;
import in.ac.iisc.cds.dsl.cdgvendor.model.RelnameAlias;
import in.ac.iisc.cds.dsl.cdgvendor.model.SchemaInfo;
import in.ac.iisc.cds.dsl.cdgvendor.model.TableInfo;
import in.ac.iisc.cds.dsl.cdgvendor.model.ValueCombination;
import in.ac.iisc.cds.dsl.cdgvendor.model.ViewInfo;
import in.ac.iisc.cds.dsl.cdgvendor.model.ViewSolution;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalCondition;

public class DebugHelper {

    private static final boolean PERFORM_SANITY_CHECKS               = false;
    private static final boolean PERFORM_SOLVING_ERROR_CHECK         = true;
    private static final boolean PERFORM_VIEWCONSISTENCY_ERROR_CHECK = true;
    private static final boolean PERFORM_TOTAL_ERROR_CHECK           = true;

    public static boolean sanityChecksNeeded() {
        return PERFORM_SANITY_CHECKS;
    }

    public static boolean solvingErrorCheckNeeded() {
        return PERFORM_SOLVING_ERROR_CHECK;
    }

    public static boolean viewConsistencyErrorCheckNeeded() {
        return PERFORM_VIEWCONSISTENCY_ERROR_CHECK;
    }

    public static boolean totalErrorCheckNeeded() {
        return PERFORM_TOTAL_ERROR_CHECK;
    }

    public static void printDebug(String message) {
        System.out.println(message);
        System.out.flush();
    }

    public static void printInfo(String message) {
        System.out.println(message);
    }

    public static void printError(String message) {
        System.out.println(message);
    }

    public static void printConditions(List<Condition> conditions) {
        printDebug("Conditions: ");
        for (Condition condition : conditions) {
            printDebug("\t" + condition);
        }
    }

    public static void printFormalConditions(List<FormalCondition> formalConditions) {
        printDebug("Formal Conditions: ");
        List<FormalCondition> sortedFormalConditions = new ArrayList<>(formalConditions);
        Collections.sort(sortedFormalConditions);
        for (FormalCondition formalCondition : sortedFormalConditions) {
            printDebug("\t" + formalCondition);
        }
    }

    public static void printAllRelations(Map<String, Alqp> alqps) {
    	throw new UnsupportedOperationException("Need anonymizer!");

//        Set<List<RelnameAlias>> set = new HashSet<>();
//        for (Alqp alqp : alqps.values()) {
//            for (Condition condition : alqp.getAllConditions()) {
//                set.add(condition.getRelnames());
//            }
//        }
//
//        printInfo("Relations: ");
//        StringBuilder sb;
//        for (List<RelnameAlias> relnames : set) {
//            sb = new StringBuilder();
//            for (RelnameAlias relname : relnames) {
//                sb.append(relname.toString() + Constants.JOIN_OPERATOR);
//            }
//            String relstr = StringUtils.removeEnd(sb.toString(), Constants.JOIN_OPERATOR);
//            printInfo(relstr);
//        }

    }

    public static void printAllPredicates(Map<String, Alqp> alqps) {
    	throw new UnsupportedOperationException("Need anonymizer!");

//        Set<List<String>> set = new HashSet<>();
//        for (Alqp alqp : alqps.values()) {
//            for (Condition condition : alqp.getAllConditions()) {
//                set.add(condition.getPredicates());
//            }
//        }
//
//        printInfo("Predicates: ");
//        StringBuilder sb;
//        for (List<String> predicates : set) {
//            sb = new StringBuilder();
//            for (String predicate : predicates) {
//                sb.append(predicate + Constants.AND_OPERATOR);
//            }
//            String predstr = StringUtils.removeEnd(sb.toString(), Constants.AND_OPERATOR);
//            printInfo(predstr);
//        }

    }

    public static void printAllViews(Map<String, ViewInfo> viewInfos) {

        printInfo("Generated Views: ");
        for (Entry<String, ViewInfo> entry : viewInfos.entrySet()) {
            String tablename = entry.getKey();
            ViewInfo viewInfo = entry.getValue();
            printInfo("\t" + tablename + " " + viewInfo);
        }
    }

    public static void printAllConstraints(List<Constraint> constraints) {

        printInfo("Constraints: ");
        for (Constraint constraint : constraints) {
            printInfo("\t" + constraint);
        }
    }

    //    public static void printAllSolutions(Map<String, ViewSolution> solutionPerView) {
    //
    //        printInfo("View Solutions: ");
    //        for (Entry<String, ViewSolution> entry : solutionPerView.entrySet()) {
    //            String viewname = entry.getKey();
    //            ViewSolution viewSolution = entry.getValue();
    //            if (!ViewSolution.TRIVIAL_SOLUTION.equals(viewSolution)) {
    //                printInfo("\t" + viewname + " " + viewSolution);
    //            }
    //        }
    //    }

    public static void printAllTableInfos(Map<String, TableInfo> tableInfoMap) {

        printDebug("Table Infos: ");
        JSONObject result = new JSONObject();
        for (Entry<String, TableInfo> entry : tableInfoMap.entrySet()) {
            String tablename = entry.getKey();
            TableInfo tinfo = entry.getValue();
            result.put(tablename, new JSONObject(tinfo));
        }
        printDebug(result.toString());
    }

    public static void printSchemaInfo(SchemaInfo SCHEMA_INFO) {

        printInfo("Received SchemaInfo: ");
        printInfo(SCHEMA_INFO.toString());
    }

    public static void printDatabaseSummary(Map<String, List<ValueCombination>> summaryByViews) {

        List<String> topoViewnames = PostgresVConfig.VIEWNAMES_TOPO;
        Map<String, ViewInfo> viewInfos = PostgresVConfig.ANONYMIZED_VIEWINFOs;
        printInfo("Received DatabaseSummary: ");
        for (String viewname : topoViewnames) {

            List<ValueCombination> valueCombinations = summaryByViews.get(viewname);

            printInfo(viewname);
            List<String> sortedTableNonkeyColumns = new ArrayList<>(viewInfos.get(viewname).getTableNonkeys());
            Collections.sort(sortedTableNonkeyColumns);
            List<String> sortedFkeyColumns = new ArrayList<>(viewInfos.get(viewname).getViewNonkeys());
            sortedFkeyColumns.removeAll(sortedTableNonkeyColumns);
            Collections.sort(sortedFkeyColumns);

            StringBuilder sb = new StringBuilder("[");
            for (String fkeyColumn : sortedFkeyColumns) {
                sb.append(fkeyColumn + Constants.COMMA);
            }
            sb = new StringBuilder(StringUtils.removeEnd(sb.toString(), Constants.COMMA) + " | ");
            for (String tableNonkeyColumn : sortedTableNonkeyColumns) {
                sb.append(tableNonkeyColumn + Constants.COMMA);
            }
            String keysStr = StringUtils.removeEnd(sb.toString(), Constants.COMMA) + "]";
            printInfo("\t" + keysStr + ": ");

            if (valueCombinations.size() == 1) {
                printInfo("\t rowcount: " + valueCombinations.get(0).getRowcount());
            } else {
                for (ValueCombination valueCombination : valueCombinations) {
                    printInfo("\t" + valueCombination);
                }
            }
        }
    }

    public static void debugCliqueSolution(CliqueSolutionInMemory cliqueSolution) {
        printDebug("ColIndxs: " + cliqueSolution.getColIndexes());
        printDebug("Summary: ");
        for (ValueCombination valueCombination : cliqueSolution.getValueCombinations()) {
            printDebug("\t" + valueCombination);
        }
    }

    public static void debugSummaryRowcounts(Map<String, ViewSolution> summaryByView) {
        printDebug("Summary Stats (view name, required rowcount, got rowcount): ");
        List<String> sortedViewnames = new ArrayList<>(summaryByView.keySet());
        Collections.sort(sortedViewnames);

        for (String viewname : sortedViewnames) {
            int count = 0;
            for (ValueCombination valueCombination : summaryByView.get(viewname)) {
                count += valueCombination.getRowcount();
            }
            printDebug("\t" + viewname + "," + PostgresVConfig.ANONYMIZED_VIEWINFOs.get(viewname).getRowcount() + "," + count);
        }
    }
}

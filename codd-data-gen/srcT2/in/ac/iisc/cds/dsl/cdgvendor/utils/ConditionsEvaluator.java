package in.ac.iisc.cds.dsl.cdgvendor.utils;

import java.util.List;

import in.ac.iisc.cds.dsl.cdgvendor.constants.PostgresVConfig;
import in.ac.iisc.cds.dsl.cdgvendor.model.ValueCombination;
import in.ac.iisc.cds.dsl.cdgvendor.model.ViewSolution;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalCondition;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionAnd;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionOr;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionSimpleInt;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.Symbol;

public class ConditionsEvaluator {

    public static void debugErrorPerCondition(List<FormalCondition> conditions, ViewSolution viewSolution, List<String> sortedColumns) {

        long[] valuesGot = getCardinalities(conditions, viewSolution, sortedColumns);
        DebugHelper.printDebug("REQUIRED,GOT");
        for (int i = 0; i < conditions.size(); i++) {
            DebugHelper.printDebug(conditions.get(i).getOutputCardinality() + "," + valuesGot[i]);
        }
    }

    public static void debugErrorPerConditionWithException(List<FormalCondition> conditions, ViewSolution viewSolution, List<String> sortedColumns) {

        long[] valuesGot = getCardinalities(conditions, viewSolution, sortedColumns);
        //DebugHelper.printDebug("REQUIRED,GOT");
        for (int i = 0; i < conditions.size(); i++) {
            //DebugHelper.printDebug(conditions.get(i).getOutputCardinality() + "," + valuesGot[i]);
            if (valuesGot[i] != conditions.get(i).getOutputCardinality())
                throw new RuntimeException("Not met" + conditions.get(i));
        }
    }

    private static long[] getCardinalities(List<FormalCondition> conditions, ViewSolution viewSolution, List<String> sortedColumns) {

        long[] valuesGot = new long[conditions.size()];
        for (ValueCombination valueCombination : viewSolution) {
            for (int i = 0; i < conditions.size(); i++) {
                FormalCondition condition = conditions.get(i);
                if (condition instanceof FormalConditionSimpleInt && meetsSimple(valueCombination, (FormalConditionSimpleInt) condition, sortedColumns)) {
                    valuesGot[i] += valueCombination.getRowcount();
                } else if (condition instanceof FormalConditionAnd && meetsAnd(valueCombination, (FormalConditionAnd) condition, sortedColumns)) {
                    valuesGot[i] += valueCombination.getRowcount();
                } else if (condition instanceof FormalConditionOr && meetsOr(valueCombination, (FormalConditionOr) condition, sortedColumns)) {
                    valuesGot[i] += valueCombination.getRowcount();
                } else if (!(condition instanceof FormalConditionSimpleInt || condition instanceof FormalConditionAnd
                        || condition instanceof FormalConditionOr))
                    throw new RuntimeException("Unrecognized condition " + condition + " of type " + condition.getClass());
            }
        }
        return valuesGot;
    }

    public static long getCardinality(FormalCondition condition, ViewSolution viewSolution, List<String> sortedColumns) {

        long valueGot = 0;
        for (ValueCombination valueCombination : viewSolution) {
            if (condition instanceof FormalConditionSimpleInt && meetsSimple(valueCombination, (FormalConditionSimpleInt) condition, sortedColumns)) {
                valueGot += valueCombination.getRowcount();
            } else if (condition instanceof FormalConditionAnd && meetsAnd(valueCombination, (FormalConditionAnd) condition, sortedColumns)) {
                valueGot += valueCombination.getRowcount();
            } else if (condition instanceof FormalConditionOr && meetsOr(valueCombination, (FormalConditionOr) condition, sortedColumns)) {
                valueGot += valueCombination.getRowcount();
            } else if (!(condition instanceof FormalConditionSimpleInt || condition instanceof FormalConditionAnd || condition instanceof FormalConditionOr))
                throw new RuntimeException("Unrecognized condition " + condition + " of type " + condition.getClass());
        }
        return valueGot;
    }

    private static boolean meetsSimple(ValueCombination valueCombination, FormalConditionSimpleInt simpleCondition, List<String> sortedColumns) {
        String colname = simpleCondition.getColumnname();
        //String colname = PostgresVConfig.COLUMN_ID_MAP.get(columnId).getColname();
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

}

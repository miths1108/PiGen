package in.ac.iisc.cds.dsl.cdgvendor.solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import in.ac.iisc.cds.dsl.cdgvendor.constants.PostgresVConfig;
import in.ac.iisc.cds.dsl.cdgvendor.model.ViewInfo;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalCondition;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionBaseAggregate;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionComposite;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionSimpleInt;
import in.ac.iisc.cds.dsl.cdgvendor.utils.DebugHelper;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class DomainDecomposer {

    public static Map<String, List<Integer>> getTrivialBucketFloors(ViewInfo viewInfo) {

        Map<String, Set<Integer>> bucketFloorsByColumns = new HashMap<>();
        for (String columnname : viewInfo.getViewNonkeys()) {

            Set<Integer> integerSet = new HashSet<>();
            integerSet.add(0); //IMPORTANT: Adding ZERO as the floor for every column
            bucketFloorsByColumns.put(columnname, integerSet);
        }

        Map<String, List<Integer>> result = new HashMap<>();
        for (Entry<String, Set<Integer>> entry : bucketFloorsByColumns.entrySet()) {
            String columnname = entry.getKey();
            List<Integer> list = new ArrayList<>(entry.getValue());
            Collections.sort(list);
            result.put(columnname, list);
        }

        return result;
    }

    public static Map<String, IntList> getBucketFloors(List<FormalCondition> conditions, ViewInfo viewInfo) {

        Map<String, IntSet> bucketFloorSetByColumns = new HashMap<>();
        System.out.println("viewInfo.getViewNonkeys()" + viewInfo.getViewNonkeys());
        for (String columnname : viewInfo.getViewNonkeys()) {

            IntSet integerSet = new IntOpenHashSet();
            integerSet.add(0); //IMPORTANT: Adding ZERO as the floor for every column
            bucketFloorSetByColumns.put(columnname, integerSet);
        }
//        DebugHelper.printDebug("Empty bucketFloorsByColumns " + bucketFloorSetByColumns);

        collectBucketFloors(conditions, bucketFloorSetByColumns);
//        DebugHelper.printDebug("Filled bucketFloorsByColumns " + bucketFloorSetByColumns);

        Map<String, IntList> bucketFloorListByColumns = new HashMap<>();
        for (Entry<String, IntSet> entry : bucketFloorSetByColumns.entrySet()) {
            String columnname = entry.getKey();
            IntList list = new IntArrayList(entry.getValue());
            Collections.sort(list);
            bucketFloorListByColumns.put(columnname, list);
        }

        return bucketFloorListByColumns;
    }

    /************************************************************
     * Private supporting methods
     ************************************************************/

    /**
     * Looks at each of the condition and fills in the bucketCeilsByColumn map
     * @param conditions
     * @param bucketFloorsByColumns
     */
    private static void collectBucketFloors(List<FormalCondition> conditions, Map<String, IntSet> bucketFloorsByColumns) {

    	// fetching ColumnIdMap from target location    	
        for (FormalCondition condition : conditions) {

            //TODO: Should get only simple conditions here
            if (condition instanceof FormalConditionComposite) {
                collectBucketFloors(((FormalConditionComposite) condition).getConditionList(), bucketFloorsByColumns);

            } else if (condition instanceof FormalConditionSimpleInt) {
                FormalConditionSimpleInt formalConditionSimpleInt = (FormalConditionSimpleInt) condition;
                String columnName = formalConditionSimpleInt.getColumnname();      
                             
                IntSet integerSet = bucketFloorsByColumns.get(columnName);
                //IMPORTANT: integerSet already has ZERO as the floor for every column
                
                int a = formalConditionSimpleInt.getValue();
                switch (formalConditionSimpleInt.getSymbol()) {
                    case LT:
                        integerSet.add(a);
                        break;
                    case LE:
                        integerSet.add(a + 1);
                        break;
                    case GT:
                        integerSet.add(a + 1);
                        break;
                    case GE:
                        integerSet.add(a);
                        break;
                    case EQ:
                        integerSet.add(a);
                        integerSet.add(a + 1);
                        break;
                    default:
                        throw new RuntimeException("Unrecognized Symbol " + formalConditionSimpleInt.getSymbol());
                }
            } else if (condition instanceof FormalConditionBaseAggregate) {
            } else
                throw new RuntimeException("Unrecognized type of FormalCondition " + condition.getClass());
        }
    }
}

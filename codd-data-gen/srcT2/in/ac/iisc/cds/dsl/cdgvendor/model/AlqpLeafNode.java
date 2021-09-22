package in.ac.iisc.cds.dsl.cdgvendor.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class AlqpLeafNode extends AlqpNode {

    @Override
    public String asString(int tabCount) {
        String prefix = "\n" + StringUtils.repeat("  ", tabCount);
        return prefix + super.toString();
    }

    @Override
    public AlqpNode compress() {
        return this;
    }

    @Override
    public Condition getCondition() {
        return new ConditionBase(relname, alias, conditionStr, outputCardinality);
    }

    @Override
    public List<Condition> getAllConditions() {
        List<Condition> conditionList = new ArrayList<>();
        conditionList.add(getCondition());
        return conditionList;
    }
    
    @Override
    public Condition getAggregateCondition(List<String> groupKey, Set<String> groupTables, long projectionCardinality, boolean isTop, Map<String, TableInfo> tableInfoMap, Set<String> viewsProcessedForAggregation, MutableBoolean mayAggregateAhead) {
    	if (groupTables.size() == 1 && groupTables.contains(relname) && !viewsProcessedForAggregation.contains(relname))
    		return new ConditionBaseAggregate(relname, alias, conditionStr, outputCardinality, groupKey, groupTables, projectionCardinality, isTop);
    	else
    		return new ConditionBase(relname, alias, conditionStr, outputCardinality);
	}
    
    @Override
    public List<Condition> getAllAggregateConditions(List<String> groupKey, Set<String> groupTables, long projectionCardinality, boolean isTop, Map<String, TableInfo> tableInfoMap, Set<String> viewsProcessedForAggregation) {
    	List<Condition> conditionList = new ArrayList<>();
        conditionList.add(getAggregateCondition(groupKey, groupTables, projectionCardinality, isTop, tableInfoMap, viewsProcessedForAggregation, null));
        return conditionList;
	}

    @Override
    public List<String> getAllRelnames() {
        List<String> tablenames = new ArrayList<>();
        tablenames.add(relname);
        return tablenames;
    }

    @Override
    public List<String> getAllJoinConditionStrs() {
        return new ArrayList<>();
    }
}

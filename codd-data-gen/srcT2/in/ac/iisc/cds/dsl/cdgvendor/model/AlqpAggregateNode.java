package in.ac.iisc.cds.dsl.cdgvendor.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;

import in.ac.iisc.cds.dsl.cdgclient.constants.PostgresCConfig;

public class AlqpAggregateNode extends AlqpNode {
	
	protected List<String> groupKey = null;
	protected AlqpNode child;
	protected Set<String> groupTables = null;

    protected static final String DOT              = ".";
    protected static final String SPLITTER         = "\\.";
	
	public List<String> getGroupKey() {
		return groupKey;
	}

	public void setGroupKey(List<String> list) {
		//if(list.size() > 1)
		//	throw new RuntimeException("Only single column projections allowed");
		groupKey = new ArrayList<>();
		for (String columnname : list) {
			//Removing Aliasing. assuming columnnames are anyways unique
	        if (columnname.contains(DOT))
	        	groupKey.add(columnname.split(SPLITTER)[1]);
	        else
	        	groupKey.add(columnname);
		}
		
		SchemaInfo schemaInfo = PostgresCConfig.BASICSCHEMA_INFO;
		groupTables = new HashSet<String>();
		for (String childRelname : child.getAllRelnames()) { 				// Getting all tables names whose attribute is present in groupKey 
			TableInfo tableInfo = schemaInfo.getTableInfo(childRelname);
			if(containsAnyColumn(tableInfo.columnsNames(), groupKey)) {
				groupTables.add(childRelname);
			}
		}
	}
	
	public AlqpNode getChild() {
        return child;
    }

    public void setChild(AlqpNode child) {
        this.child = child;
    }
	
	@Override
	public String asString(int tabCount) {
		String prefix = "\n" + StringUtils.repeat("  ", tabCount);
        String str = prefix + super.toString() + "  " + groupKey.toString();
        str += child.asString(tabCount + 1);
        return str;
	}

	@Override
	public AlqpNode compress() {
		child = child.compress();
		return this;
	}

	@Override
	public Condition getCondition() {
		//throw new UnsupportedOperationException("Not applicable!");
		if(groupKey == null)
			throw new RuntimeException("Group Key not set!");
		
		SchemaInfo schemaInfo = PostgresCConfig.BASICSCHEMA_INFO;
		Condition childCondition = child.getCondition();
		Set<String> groupTables = new HashSet<String>();
		for (RelnameAlias relnameAlias : childCondition.getRelnames()) { 				// Getting all tables names whose attribute is present in groupKey 
			TableInfo tableInfo = schemaInfo.getTableInfo(relnameAlias.getRelname());
			//if(containsAnyColumn(tableInfo.getColumns(), groupKey)) {
			if(containsAnyColumn(tableInfo.columnsNames(), groupKey)) {
				groupTables.add(relnameAlias.getRelname());
			}
		}
		Condition aggregateCond = new ConditionAggregate(childCondition, groupKey, groupTables, outputCardinality);
		//throw new RuntimeException("Here need to create condition on every view");
        return aggregateCond;
	}
	
	@Override
	public List<Condition> getAllConditions() {
		throw new UnsupportedOperationException("Not applicable!");
//		List<Condition> conditionList = new ArrayList<>();
//        conditionList.addAll(child.getAllAggregateConditions(groupKey, groupTables, outputCardinality, true));
//        return conditionList;
	}
	
	@Override
	public Condition getAggregateCondition(List<String> groupKey, Set<String> groupTables, long projectionCardinality, boolean isTop, Map<String, TableInfo> tableInfoMap, Set<String> viewsProcessedForAggregation, MutableBoolean mayAggregateAhead) {
		throw new UnsupportedOperationException("Not applicable!");
	}
	
    @Override
    public List<Condition> getAllAggregateConditions(List<String> useless1, Set<String> useless2, long useless3, boolean useless4, Map<String, TableInfo> tableInfoMap, Set<String> useless5) {
    	List<Condition> conditionList = new ArrayList<>();
    	conditionList.addAll(child.getAllAggregateConditions(groupKey, groupTables, outputCardinality, true, tableInfoMap, new HashSet<>()));
    	return conditionList;
//    	throw new UnsupportedOperationException("Not applicable!");
	}

	@Override
	public List<String> getAllRelnames() {
		return child.getAllRelnames();
	}

	@Override
	public List<String> getAllJoinConditionStrs() {
		return child.getAllJoinConditionStrs();
	}

	private boolean containsAnyColumn(Set<String> first, List<String> second) {
		Set<Object> temp = new HashSet<>(first);
		temp.retainAll(second);
		if(temp.size() == 0)
			return false;
		else
			return true;
	}
}

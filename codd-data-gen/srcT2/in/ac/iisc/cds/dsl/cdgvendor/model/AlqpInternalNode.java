package in.ac.iisc.cds.dsl.cdgvendor.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class AlqpInternalNode extends AlqpNode {

    protected AlqpNode leftChild;
    protected AlqpNode rightChild;

    public AlqpNode getLeftChild() {
        return leftChild;
    }

    public void setLeftChild(AlqpNode leftChild) {
        this.leftChild = leftChild;
    }

    public AlqpNode getRightChild() {
        return rightChild;
    }

    public void setRightChild(AlqpNode rightChild) {
        this.rightChild = rightChild;
    }

    public List<AlqpNode> getChildren() {
        List<AlqpNode> children = new ArrayList<>();
        children.add(leftChild);
        children.add(rightChild);
        return children;
    }

    @Override
    public String asString(int tabCount) {
        String prefix = "\n" + StringUtils.repeat("  ", tabCount);
        String str = prefix + super.toString();

        str += leftChild.asString(tabCount + 1);
        if (rightChild != null) {
            str += rightChild.asString(tabCount + 1);
        }
        return str;
    }

    /**
     * Removes extra nodes by collapsing nodes with [ singlechild |  nocondition | samerows ]
     */
    @Override
    public AlqpNode compress() {
        //Descend root
        if (rightChild == null && conditionStr == null && outputCardinality == leftChild.getOutputCardinality())
            return leftChild.compress();
        if (rightChild == null && ("Hash".equals(nodetype) || "Sort".equals(nodetype))) // || "Aggregate".equals(nodetype)))
            return leftChild.compress();

        leftChild = leftChild.compress();
        if (rightChild != null) {
            rightChild = rightChild.compress();
        }
        return this;
    }

    @Override
    public Condition getCondition() {

        if (rightChild == null)		// will be null on node type "Hash" if getCondition called before compressing the tree
            //System.out.println("--rarely happens");
            //return leftChild.getCondition();
            throw new RuntimeException("Should not be reaching here");
        
        ConditionJoin join = new ConditionJoin(leftChild.getCondition(), rightChild.getCondition(), outputCardinality, conditionStr);
        return join;
    }

    /**
     * Creates fresh unbacked list from child lists and returns
     */
    @Override
    public List<Condition> getAllConditions() {

        List<Condition> conditionList = new ArrayList<>();
        conditionList.add(getCondition());
        conditionList.addAll(leftChild.getAllConditions());
        if (rightChild != null) {
            conditionList.addAll(rightChild.getAllConditions());
        }
        return conditionList;
    }
    
    @Override
    public Condition getAggregateCondition(List<String> groupKey, Set<String> groupTables, long projectionCardinality, boolean isTop, Map<String, TableInfo> tableInfoMap, Set<String> viewsProcessedForAggregation, MutableBoolean mayAggregateAhead) {
    	List<String> allRelnames = getAllRelnames();
    	if (rightChild == null)		// will be null on node type "Hash" if getCondition called before compressing the tree
            throw new RuntimeException("Should not be reaching here");
    	
    	Set<String> tempGroupTables = new HashSet<>(groupTables);
    	tempGroupTables.removeAll(allRelnames);
    	// TODO: In projections other then 1D, logic may have to be changed
    	if (tempGroupTables.size() == 0) {
    		mayAggregateAhead.setTrue();
    		String parentView = getParentView(allRelnames, tableInfoMap);
    		if (!viewsProcessedForAggregation.contains(parentView)) {
    			viewsProcessedForAggregation.add(parentView);
    			return new ConditionJoinAggregate(leftChild.getCondition(), rightChild.getCondition(), outputCardinality, groupKey, groupTables, projectionCardinality, isTop, conditionStr);
    		}
    	}
   		return new ConditionJoin(leftChild.getCondition(), rightChild.getCondition(), outputCardinality, conditionStr);
	}
    
    @Override
    public List<Condition> getAllAggregateConditions(List<String> groupKey, Set<String> groupTables, long projectionCardinality, boolean isTop, Map<String, TableInfo> tableInfoMap, Set<String> viewsProcessedForAggregation) {
    	List<Condition> conditionList = new ArrayList<>();
    	MutableBoolean mayAggregateAhead = new MutableBoolean(false);
    	Condition conditionAtThisNode = getAggregateCondition(groupKey, groupTables, projectionCardinality, isTop, tableInfoMap, viewsProcessedForAggregation, mayAggregateAhead);
        conditionList.add(conditionAtThisNode);
        if (mayAggregateAhead.isTrue()) {
	        conditionList.addAll(leftChild.getAllAggregateConditions(groupKey, groupTables, projectionCardinality, false, tableInfoMap, new HashSet<>(viewsProcessedForAggregation)));	// TODO: Can send viewsProcessedForAggregation without copy?
	        if (rightChild != null) {
	            conditionList.addAll(rightChild.getAllAggregateConditions(groupKey, groupTables, projectionCardinality, false, tableInfoMap, new HashSet<>(viewsProcessedForAggregation)));	// TODO: Can send viewsProcessedForAggregation without copy?
	        }
        }
        else {
        	conditionList.addAll(leftChild.getAllConditions());
            if (rightChild != null) {
                conditionList.addAll(rightChild.getAllConditions());
            }
        }
        return conditionList;
	}

    @Override
    public List<String> getAllRelnames() {
        List<String> tablenames = new ArrayList<>();
        tablenames.addAll(leftChild.getAllRelnames());
        if (rightChild != null) {
            tablenames.addAll(rightChild.getAllRelnames());
        }
        return tablenames;
    }

    @Override
    public List<String> getAllJoinConditionStrs() {
        List<String> joinConditionStrList = new ArrayList<>();
        joinConditionStrList.add(conditionStr);
        joinConditionStrList.addAll(leftChild.getAllJoinConditionStrs());
        if (rightChild != null) {
            joinConditionStrList.addAll(rightChild.getAllJoinConditionStrs());
        }
        return joinConditionStrList;
    }
    
    public String getParentView(List<String> relnameList, Map<String, TableInfo> tableInfoMap) {
        String parentViewname = null;
        int minSeqno = Integer.MAX_VALUE;
        for (String relname : relnameList) {
            int seqno = tableInfoMap.get(relname).getTopoSeqno();
            if (minSeqno > seqno) {
                minSeqno = seqno;
                parentViewname = relname;
            }
        }

        if (parentViewname == null)
            throw new RuntimeException("Should not be reaching here");

        return parentViewname;
    }
}

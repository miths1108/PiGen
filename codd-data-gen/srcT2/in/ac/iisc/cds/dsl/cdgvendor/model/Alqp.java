package in.ac.iisc.cds.dsl.cdgvendor.model;

import java.util.List;
import java.util.Map;

public class Alqp {
    private AlqpNode root;

    public Alqp(AlqpNode root) {
        this.root = root;
    }

    public AlqpNode getRoot() {
        return root;
    }

    public void compress() {
        root = root.compress();
    }

    @Override
    public String toString() {
        return root.asString(0);
    }

    public List<Condition> getAllConditions(Map<String, TableInfo> tableInfoMap) {
    	
    	if (root instanceof AlqpAggregateNode) {
    		return root.getAllAggregateConditions(null, null, -1, true, tableInfoMap, null);
    	}
    	else
    		return root.getAllConditions();
    }

    public List<String> getAllRelnames() {
        return root.getAllRelnames();
    }

    public List<String> getAllJoinConditionStrs() {
        return root.getAllJoinConditionStrs();
    }
}

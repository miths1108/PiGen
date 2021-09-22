package in.ac.iisc.cds.dsl.cdgvendor.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.mutable.MutableBoolean;

import in.ac.iisc.cds.dsl.cdgvendor.constants.Constants;

public abstract class AlqpNode {
    protected String nodetype;
    protected long   outputCardinality;
    protected String conditionStr;

    protected String relname;
    protected String alias;

    public String getNodetype() {
        return nodetype;
    }

    public void setNodetype(String nodetype) {
        this.nodetype = nodetype;
    }

    public long getOutputCardinality() {
        return outputCardinality;
    }

    public void setOutputCardinality(long outputCardinality) {
        this.outputCardinality = outputCardinality;
    }

    public String getConditionStr() {
        return conditionStr;
    }

    public void setConditionStr(String conditionStr) {
        this.conditionStr = conditionStr;
    }

    public String getRelname() {
        return relname;
    }

    public void setRelname(String relname) {
        this.relname = relname;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Override
    public String toString() {
        return nodetype + " " + outputCardinality + " " + conditionStr + " " + (relname == null ? "null" : relname + Constants.ALIAS_OPERATOR + alias);
    }

    /**
     * Used in toString
     * @param tabCount
     * @return
     */
    public abstract String asString(int tabCount);

    public abstract AlqpNode compress();

    public abstract Condition getCondition();

    public abstract List<Condition> getAllConditions();
    
    public abstract Condition getAggregateCondition(List<String> groupKey, Set<String> groupTables, long projectionCardinality, boolean isTop, Map<String, TableInfo> tableInfoMap, Set<String> viewsProcessedForAggregation, MutableBoolean mayAggregateAhead);
    
    public abstract List<Condition> getAllAggregateConditions(List<String> groupKey, Set<String> groupTables, long projectionCardinality, boolean isTop, Map<String, TableInfo> tableInfoMap, Set<String> viewsProcessedForAggregation);

    public abstract List<String> getAllRelnames();

    public abstract List<String> getAllJoinConditionStrs();

}

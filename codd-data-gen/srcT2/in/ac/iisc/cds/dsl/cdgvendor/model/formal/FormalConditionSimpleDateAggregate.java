package in.ac.iisc.cds.dsl.cdgvendor.model.formal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import in.ac.iisc.cds.dsl.cdgvendor.model.ConditionAggregate;

public class FormalConditionSimpleDateAggregate extends FormalConditionSimpleDate implements FormalConditionAggregate {

	private List<String> groupKey;
	private Set<String> groupTables;
	protected long projectionCardinality;
	protected boolean isTop;
	
	public FormalConditionSimpleDateAggregate(ConditionAggregate origCondition, FormalConditionSimpleDate formalConditionSimpleDate) {
		super(formalConditionSimpleDate);
    	
		groupKey = origCondition.getGroupKey();
		Collections.sort(groupKey);
		groupTables = origCondition.getGroupTables();
		projectionCardinality = origCondition.getProjectionCardinality();
		isTop = origCondition.isTop();
	}

	public FormalConditionSimpleDateAggregate(String columnname, String symbolString, Date value) {
		super(columnname, symbolString, value);
	}
	
	@Override
	public void setGroupKey(List<String> groupKey) {
		this.groupKey = groupKey;
	}

	@Override
	public List<String> getGroupKey() {
		return groupKey;
	}

	@Override
	public void setGroupTables(Set<String> groupTables) {
		this.groupTables = groupTables;
	}

	@Override
	public Set<String> getGroupTables() {
		return groupTables;
	}

	@Override
	public long getProjectionCardinality() {
		return projectionCardinality;
	}

	@Override
	public boolean isTop() {
		return isTop;
	}
	
	@Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append("\n --- GROUP BY {" + groupKey.toString() + "} ");
        if (isTop)
        	sb.append("=");
        else
        	sb.append(">=");
        sb.append(" " + getProjectionCardinality());
        return sb.toString();
    }
	
	@Override
    public String asString() {
        return super.asString();
    }

    @Override
    public String asQueryString() {
        throw new RuntimeException("Not Implemented");
    }
    
    @Override
    public int hashCode() {
        throw new RuntimeException("Not Implemented");
//        final int prime = 31;
//        int result = super.hashCode();
//        result = prime * result + groupKey.hashCode();
//        result = prime * result + groupTables.hashCode();
//        result = prime * result + (int) (projectionCardinality ^ projectionCardinality >>> 32);
//        result = prime * result + (isTop ? prime : 0);
//        return result;
    }

    @Override
    public boolean equals(Object obj) {
		throw new RuntimeException("Not implemented");
    }
    
    @Override
    public FormalConditionSimpleDateAggregate getDeepCopy() {
    	FormalConditionSimpleDateAggregate another = new FormalConditionSimpleDateAggregate(columnname, symbol.symbolString, value);
    	
        another.outputCardinality = outputCardinality;
        another.viewname = viewname;
        
        
        another.setGroupKey(new ArrayList<>(getGroupKey()));
        another.setGroupTables(new HashSet<>(getGroupTables()));
        another.projectionCardinality = projectionCardinality;
        another.isTop = isTop;
        return another;
    }
}

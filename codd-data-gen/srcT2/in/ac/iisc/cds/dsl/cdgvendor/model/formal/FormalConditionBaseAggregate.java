package in.ac.iisc.cds.dsl.cdgvendor.model.formal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import in.ac.iisc.cds.dsl.cdgvendor.model.ConditionAggregate;

public class FormalConditionBaseAggregate extends FormalCondition implements FormalConditionAggregate {
	
	private List<String> groupKey;
	private Set<String> groupTables;
	protected long projectionCardinality;
	protected boolean isTop;
	
	public FormalConditionBaseAggregate(ConditionAggregate origCondition, String viewname, long outputCardinality) {
		this.viewname = viewname;
		this.outputCardinality = outputCardinality;
		
		groupKey = origCondition.getGroupKey();
		Collections.sort(groupKey);
		groupTables = origCondition.getGroupTables();
		projectionCardinality = origCondition.getProjectionCardinality();
		isTop = origCondition.isTop();
	}

	public FormalConditionBaseAggregate(FormalConditionBaseAggregate other) {
		this.viewname = other.viewname;
		this.outputCardinality = other.outputCardinality;
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
    public int hashCode() {
    	final int prime = 31;
        int result = super.hashCode();
        result = prime * result + groupKey.hashCode();
        result = prime * result + groupTables.hashCode();
        result = prime * result + (int) (projectionCardinality ^ projectionCardinality >>> 32);
        result = prime * result + (isTop ? prime : 0);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FormalConditionBaseAggregate other = (FormalConditionBaseAggregate) obj;
        if (!groupKey.equals(other.groupKey))
    		return false;
    	if (!groupTables.equals(other.groupTables))
    		return false;
    	if (projectionCardinality != other.getProjectionCardinality())
    		return false;
    	if (isTop != other.isTop())
    		return false;
    	
    	if (!(super.equals((FormalCondition) other)))
    		return false;
        return true;
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
        return "";
	}

	@Override
	public String asQueryString() {
        throw new RuntimeException("Not implemented!");
	}

	@Override
	public FormalCondition getDeepCopy() {
		FormalConditionBaseAggregate another = new FormalConditionBaseAggregate(this);
    	
        another.setGroupKey(new ArrayList<>(getGroupKey()));
        another.setGroupTables(new HashSet<>(getGroupTables()));
        another.projectionCardinality = projectionCardinality;
        another.isTop = isTop;
        return another;
	}

}

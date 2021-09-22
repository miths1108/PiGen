package in.ac.iisc.cds.dsl.cdgvendor.model.formal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FormalConditionSimpleIntAggregate extends FormalConditionSimpleInt implements FormalConditionAggregate {

	private List<String> groupKey;
	private Set<String> groupTables;
	protected long projectionCardinality;
	protected boolean isTop;
	
	public FormalConditionSimpleIntAggregate(FormalConditionSimpleNumberAggregate another, int value) {
        super(another, value);
        
        groupKey = another.getGroupKey();
        Collections.sort(groupKey);
		groupTables = another.getGroupTables();
		projectionCardinality = another.getProjectionCardinality();
		isTop = another.isTop();
    }

    public FormalConditionSimpleIntAggregate(FormalConditionSimpleStringAggregate another, int value) {
        super(another, value);
        
        groupKey = another.getGroupKey();
		groupTables = another.getGroupTables();
		projectionCardinality = another.getProjectionCardinality();
		isTop = another.isTop();
    }
    
    public FormalConditionSimpleIntAggregate(FormalConditionSimpleIntAggregate another, int value) {
        super(another, value);

        groupKey = another.getGroupKey();
		groupTables = another.getGroupTables();
		projectionCardinality = another.getProjectionCardinality();
		isTop = another.isTop();
    }

	public FormalConditionSimpleIntAggregate(FormalConditionSimpleDateAggregate another, int value) {
		super(another, value);

        groupKey = another.getGroupKey();
		groupTables = another.getGroupTables();
		projectionCardinality = another.getProjectionCardinality();
		isTop = another.isTop();
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
    	if (this == obj) return true;
    	if (getClass() != obj.getClass())
    		return false;
    	FormalConditionSimpleIntAggregate that = (FormalConditionSimpleIntAggregate)obj;
    	if (!groupKey.equals(that.groupKey))
    		return false;
    	if (!groupTables.equals(that.groupTables))
    		return false;
    	if (projectionCardinality != that.getProjectionCardinality())
    		return false;
    	if (isTop != that.isTop())
    		return false;
    	
    	if (!(super.equals((FormalConditionSimpleInt) that)))
    		return false;
    	
    	return true;
    }
    
    @Override
    public FormalConditionSimpleIntAggregate getDeepCopy() {
    	FormalConditionSimpleIntAggregate another = new FormalConditionSimpleIntAggregate(this, value);
    	
        another.setGroupKey(new ArrayList<>(getGroupKey()));
        another.setGroupTables(new HashSet<>(getGroupTables()));
        another.projectionCardinality = projectionCardinality;
        another.isTop = isTop;
        return another;
    }
}

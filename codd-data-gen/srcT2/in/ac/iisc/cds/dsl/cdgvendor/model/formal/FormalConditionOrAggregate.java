package in.ac.iisc.cds.dsl.cdgvendor.model.formal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import in.ac.iisc.cds.dsl.cdgvendor.constants.Constants;
import in.ac.iisc.cds.dsl.cdgvendor.model.ConditionAggregate;

public class FormalConditionOrAggregate extends FormalConditionOr implements FormalConditionAggregate {
	
	private List<String> groupKey;
	private Set<String> groupTables;
	protected long projectionCardinality;
	protected boolean isTop;
	
	public FormalConditionOrAggregate() {
		super();
    }

    public FormalConditionOrAggregate(ConditionAggregate origCondition, FormalConditionOr another) {
    	super(another);
    	
		addConditionAll(another.getConditionList());
		groupKey = origCondition.getGroupKey();
		Collections.sort(groupKey);
		groupTables = origCondition.getGroupTables();
		projectionCardinality = origCondition.getProjectionCardinality();
		isTop = origCondition.isTop();
    }
    
    public FormalConditionOrAggregate(FormalConditionOrAggregate another) {
    	super(another);
    	
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
        StringBuilder sb = new StringBuilder();
        for (FormalCondition condition : conditionList) {
            sb.append("[" + condition.asString() + "]").append(Constants.OR_OPERATOR);
        }
        return StringUtils.removeEnd(sb.toString(), Constants.OR_OPERATOR);
    }

    @Override
    public String asQueryString() {
		throw new RuntimeException("Not implemented");

//        StringBuilder sb = new StringBuilder();
//        for (FormalCondition condition : conditionList) {
//            sb.append("(" + condition.asQueryString() + ")").append(" OR ");
//        }
//        return StringUtils.removeEnd(sb.toString(), " OR ");
    }

    @Override
    public FormalConditionOrAggregate getDeepCopy() {
    	FormalConditionOrAggregate another = new FormalConditionOrAggregate();
    	
        for (FormalCondition condition : conditionList) {
            another.addCondition(condition.getDeepCopy());
        }
        another.outputCardinality = outputCardinality;
        another.viewname = viewname;
        
        another.setGroupKey(new ArrayList<>(getGroupKey()));
        another.setGroupTables(new HashSet<>(getGroupTables()));
        another.projectionCardinality = projectionCardinality;
        another.isTop = isTop;
        
        return another;
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
    	return false;
//		throw new RuntimeException("Not implemented");
//        if (this == obj) {
//            return true;
//        }
//        if (!super.equals(obj)) {
//            return false;
//        }
//        if (getClass() != obj.getClass()) {
//            return false;
//        }
//        FormalConditionComposite other = (FormalConditionComposite) obj;
//        if (conditionList == null) {
//            if (other.conditionList != null) {
//                return false;
//            }
//        } else if (!conditionList.equals(other.conditionList)) {
//            return false;
//        }
//        return true;
    }
}

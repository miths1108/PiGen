package in.ac.iisc.cds.dsl.cdgvendor.model;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import in.ac.iisc.cds.dsl.cdgvendor.constants.Constants;

public class ConditionJoinAggregate extends ConditionJoin implements ConditionAggregate {
	
	private final List<String> groupKey;
	private final Set<String> groupTables;
	private final long projectionCardinality;
	private final boolean isTop;

	public ConditionJoinAggregate(Condition cond1, Condition cond2, long outputCardinality, List<String> groupKey, Set<String> groupTables, long projectionCardinality, boolean isTop, String conditionStr) {
		super(cond1, cond2, outputCardinality, conditionStr);
		this.groupKey = groupKey;
		this.groupTables = groupTables;
		this.projectionCardinality = projectionCardinality;
		this.isTop = isTop;
		
	}

	@Override
	public List<String> getGroupKey() {
		return groupKey;
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
		StringBuilder sb;

        sb = new StringBuilder();
        for (String predicate : predicates) {
            sb.append(predicate).append(Constants.AND_OPERATOR);
        }
        String predtsr = StringUtils.removeEnd(sb.toString(), Constants.AND_OPERATOR);

        sb = new StringBuilder();
        for (RelnameAlias relname : relnames) {
            sb.append(relname.toString() + Constants.JOIN_OPERATOR);
        }
        String relstr = StringUtils.removeEnd(sb.toString(), Constants.JOIN_OPERATOR);
        
        sb = new StringBuilder();
        sb.append("|" + Constants.SEL_OPERATOR + "( " + predtsr + " ) (" + relstr + ") | = " + getOutputCardinality() + "\n");
        sb.append("|" + Constants.PROJECTION_OPERATOR + groupKey.toString() + Constants.SEL_OPERATOR + "( " + predtsr + " ) (" + relstr + ") | ");
        if (isTop)
        	sb.append("=");
        else
        	sb.append(">=");
        sb.append(" " + getProjectionCardinality());
        return sb.toString();
	}
}

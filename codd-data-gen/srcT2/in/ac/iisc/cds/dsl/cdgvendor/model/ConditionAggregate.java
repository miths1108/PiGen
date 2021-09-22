package in.ac.iisc.cds.dsl.cdgvendor.model;

import java.util.List;
import java.util.Set;

public interface ConditionAggregate {
	public List<String> getGroupKey();
	public Set<String> getGroupTables();
	public long getProjectionCardinality();
	public boolean isTop();
}

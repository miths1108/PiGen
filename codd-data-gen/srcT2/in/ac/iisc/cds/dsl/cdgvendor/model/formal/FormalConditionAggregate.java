package in.ac.iisc.cds.dsl.cdgvendor.model.formal;

import java.util.List;
import java.util.Set;

public interface FormalConditionAggregate {
	public void setGroupKey(List<String> groupKey);
	public List<String> getGroupKey();

	public void setGroupTables(Set<String> groupTables);
	public Set<String> getGroupTables();

	public long getProjectionCardinality();
	public boolean isTop();
}

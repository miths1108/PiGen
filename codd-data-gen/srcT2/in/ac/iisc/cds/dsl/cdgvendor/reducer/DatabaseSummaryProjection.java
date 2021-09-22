package in.ac.iisc.cds.dsl.cdgvendor.reducer;

import java.io.Serializable;
import java.util.List;

public class DatabaseSummaryProjection implements Serializable {
	private static final long serialVersionUID = 1L;
	public List<RegionSummary>regionSummary;
	
	public DatabaseSummaryProjection(List<RegionSummary>regionSummary) {
		this.regionSummary=regionSummary;
	}
}

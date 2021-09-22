package in.ac.iisc.cds.dsl.cdgvendor.reducer;

import java.util.Comparator;

public class RegionComparator implements Comparator<BucketStructure>{
	public int compare(BucketStructure a, BucketStructure b)
    {
		return a.toString().compareTo(b.toString());
    }
}

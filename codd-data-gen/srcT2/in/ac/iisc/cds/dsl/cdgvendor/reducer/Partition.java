package in.ac.iisc.cds.dsl.cdgvendor.reducer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * List<Region> is called Partition.
 * 
 * @author dsladmin
 *
 */
public class Partition implements Serializable {

    //private final List<Region> regionList;
	//changed by shadab
	private static final long serialVersionUID = -91901467087404100L;
	public List<Region> regionList;

    public Partition() {
        regionList = new ArrayList<>();
    }

    public Partition(List<Region> regionList) {
        this.regionList = new ArrayList<>(regionList);
    }

    public void add(Region val) {
        regionList.add(val);
    }

    public Region at(int index) {
        return regionList.get(index);
    }

    public List<Region> getAll() {
        return regionList;
    }

    public int size() {
        return regionList.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("[");
        for (Region region : regionList) {
            sb.append(region + "\n");
        }
        String str = sb.toString();
        str = str.substring(0, str.length() - 1);
        return str + "]";
    }
}

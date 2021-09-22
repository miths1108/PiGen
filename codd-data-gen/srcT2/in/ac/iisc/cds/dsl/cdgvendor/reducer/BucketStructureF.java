package in.ac.iisc.cds.dsl.cdgvendor.reducer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BucketStructureF implements Serializable {
	private static final long serialVersionUID = 13L;
	private final List<BucketF>          bucketList;

    public static final BucketStructure EMPTY_BS = new BucketStructure();

    public BucketStructureF() {
        bucketList = new ArrayList<>();
    }

    /**
     * Does deep copy
     * @param another
     */
    public BucketStructureF(BucketStructureF another) {
        bucketList = new ArrayList<>();

        for (BucketF anotherBucket : another.bucketList) {
            BucketF bucket = new BucketF(anotherBucket);
            bucketList.add(bucket);
        }
    }

    public BucketStructureF(List<BucketF> bucketList) {
        this.bucketList = bucketList;
    }

    public void add(BucketF val) {
        bucketList.add(val);
    }

    public BucketF at(int index) {
        return bucketList.get(index);
    }

    public int size() {
        return bucketList.size();
    }

    public List<BucketF> getAll() {
        return bucketList;
    }
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("[");
        for (BucketF bucket : bucketList) {
            sb.append(bucket + " ");
        }
        String str = sb.toString();
        str = str.substring(0, str.length() - 1);
        return str + "]";
    }
}

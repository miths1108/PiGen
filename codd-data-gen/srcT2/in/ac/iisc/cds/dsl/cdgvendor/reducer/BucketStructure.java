package in.ac.iisc.cds.dsl.cdgvendor.reducer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import in.ac.iisc.cds.dsl.cdgvendor.utils.DebugHelper;

/**
 * List<Bucket> is called BucketStructure. Length of list equals number of
 * attributes. A BucketStructure also represents a subcondition.
 * 
 * @author dsladmin
 *
 */
public class BucketStructure implements Serializable {

	private final List<Bucket> bucketList;

	public static final BucketStructure EMPTY_BS = new BucketStructure();

	public BucketStructure() {
		bucketList = new ArrayList<>();
	}

	/**
	 * Does deep copy
	 * 
	 * @param another
	 */
	public BucketStructure(BucketStructure another) {
		bucketList = new ArrayList<>();

		for (Bucket anotherBucket : another.bucketList) {
			Bucket bucket = new Bucket(anotherBucket);
			bucketList.add(bucket);
		}
	}

	public BucketStructure(List<Bucket> bucketList) {
		this.bucketList = bucketList;
	}

	public void add(Bucket val) {
		bucketList.add(val);
	}

	public Bucket at(int index) {
		return bucketList.get(index);
	}

	public int size() {
		return bucketList.size();
	}

	public List<Bucket> getAll() {
		return bucketList;
	}

	public static BucketStructure getLeading(BucketStructure bs1, BucketStructure bs2) {
		if (DebugHelper.sanityChecksNeeded()) {
			checkCompatibility(bs1, bs2);
		}

		if (bs1.isEmpty() || bs2.isEmpty()) {
			throw new RuntimeException("Comparing empty bucketStructure on min value");
		}

		for (int i = 0; i < bs1.bucketList.size(); i++) {
			Bucket b1 = bs1.bucketList.get(i);
			Bucket b2 = bs2.bucketList.get(i);
			int res = b1.compareTo2(b2);
			if (res < 0) {
				return bs1;
			} else if (res > 0) {
				return bs2;
			}
		}
		return bs1;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("[");
		for (Bucket bucket : bucketList) {
			sb.append(bucket + " ");
		}
		String str = sb.toString();
		str = str.substring(0, str.length() - 1);
		return str + "]";
	}

	/************************************************************
	 * Some primitive operations
	 ************************************************************/
	private static void checkCompatibility(BucketStructure bs1, BucketStructure bs2) {
		if (bs1.bucketList.size() != bs2.bucketList.size()) {
			throw new RuntimeException("Uncamptible operation on bucketStructures of size " + bs1.bucketList.size()
					+ " and " + bs2.bucketList.size());
		}
	}

	public BucketStructure intersection(BucketStructure bs2) {
		if (DebugHelper.sanityChecksNeeded()) {
			checkCompatibility(this, bs2);
		}

		BucketStructure result = new BucketStructure();
		for (int i = 0; i < bucketList.size(); i++) {
			Bucket b1 = bucketList.get(i);
			Bucket b2 = bs2.bucketList.get(i);
			Bucket bres = b1.intersection(b2);
			if (bres.isEmpty()) {
				return EMPTY_BS;
			}
			result.add(bres);
		}
		return result;
	}

	public boolean isEmpty() {
		return bucketList.isEmpty();
	}

	public List<BucketStructure> minus(BucketStructure bs2) {
		if (DebugHelper.sanityChecksNeeded()) {
			checkCompatibility(this, bs2);
		}

		List<BucketStructure> result = new ArrayList<>();
		if (intersection(bs2).isEmpty()) {
			result.add(this);
			return result;
		}

		List<Bucket> minusBuckets = new ArrayList<>();
		for (int i = 0; i < bucketList.size(); i++) {
			Bucket b1 = bucketList.get(i);
			Bucket b2 = bs2.bucketList.get(i);
			Bucket minusBucket = b1.minus(b2);
			minusBuckets.add(minusBucket);
		}

		for (int i = 0; i < bucketList.size(); i++) {
			BucketStructure temp = new BucketStructure();
			int j;
			for (j = 0; j < i; j++) {
				Bucket b = bucketList.get(j);
				b = b.minus(minusBuckets.get(j));
				if (b.isEmpty()) {
					continue;
				}
				temp.add(b);
			}
			if (minusBuckets.get(i).isEmpty()) {
				continue;
			}
			temp.add(minusBuckets.get(i));
			for (j++; j < bucketList.size(); j++) {
				if (bucketList.get(j).isEmpty()) {
					continue;
				}
				temp.add(bucketList.get(j));
			}
			result.add(temp);
		}

		return result;
	}

	public BucketStructure getDeepCopy() {
		BucketStructure newBS=new BucketStructure();
		for(Bucket b:this.getAll()) {
			newBS.add(b.deepCopy());
		}
		return newBS;
	}

	public int compareTo(BucketStructure o) {
		return this.bucketList.toString().compareTo(o.toString());
	}

	@Override
	public int hashCode() {
		int result = 0;
		int exp = 0;
		for (Bucket b : this.bucketList) {
			int tot = 0;
			for (Integer i : b.getAll()) {
				tot += i;
			}
			result += tot * Math.pow(10, exp);

		}

		return result;
	}

	public boolean areEqual(BucketStructure bs) {
		
		if (this.minus(bs).isEmpty() && bs.minus(this).isEmpty())
			return true;
		return false;
	}
	@Override
	public boolean equals(Object o) {
		BucketStructure bs2 = (BucketStructure) o;
		if (this.minus(bs2).isEmpty() && bs2.minus(this).isEmpty())
			return true;
		return false;
	}
	
//	public BucketStructure union(BucketStructure bs) {
//		BucketStructure result=
//	}
	
	public BucketStructure projectBS(List<Integer> projectionColumnsIdx) {
		BucketStructure projectedBS = new BucketStructure();
		for (int i = 0; i < this.size(); i++) {
			if (projectionColumnsIdx.contains(i))
				projectedBS.add(this.at(i));
		}
		return projectedBS;
	}
	
	public void set(Bucket b, int i) {
		this.bucketList.set(i, b);
	}
}

package in.ac.iisc.cds.dsl.cdgvendor.reducer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalConditionSimpleInt;

/**
 * List<BucketStructure> is called Region. A Region in the attributes domain
 * space comprises of a list of connected regions. A Region also represents a
 * Variable. A Region also represents a Condition.
 * 
 * @author dsladmin
 *
 */
public class Region implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -919014670874041927L;

	private final List<BucketStructure> bsList;

	public static final Region EMPTY_REGION = new Region();

	public Region() {
		bsList = new ArrayList<>();
	}

	public void add(BucketStructure val) {
		bsList.add(val);
	}

//	public void set(BucketStructure)
	public void addAll(List<BucketStructure> vals) {
		bsList.addAll(vals);
	}

	public BucketStructure at(int index) {
		return bsList.get(index);
	}

	public int size() {
		return bsList.size();
	}

	public List<BucketStructure> getAll() {
		return bsList;
	}

	// Shadab
	public void sort() {
		Collections.sort(bsList, new RegionComparator());
	}

	public void remove(int ind) {
		bsList.remove(ind);
	}

	public BucketStructure getLeadingBS() {
		if (isEmpty()) {
			throw new RuntimeException("Unable to get leadingBS from empty region");
		}

		BucketStructure minBS = at(0);
		for (int i = 1; i < size(); i++) {
			minBS = BucketStructure.getLeading(minBS, at(i));
		}
		return minBS;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("[");
		for (BucketStructure bs : bsList) {
			sb.append(bs + "\n");
		}
		String str = sb.toString();
		str = str.substring(0, str.length() - 1);
		return str + "]";
	}

	/************************************************************
	 * Some primitive operations
	 ************************************************************/
	public Region intersection(Region r2) {
		Region result = new Region();
		for (BucketStructure bs1 : bsList) {
			for (BucketStructure bs2 : r2.bsList) {
				BucketStructure bsres = bs1.intersection(bs2);
				if (!bsres.isEmpty()) {
					result.add(bsres);
				}
			}
		}
		if (result.bsList.isEmpty()) {
			return EMPTY_REGION;
		}
		return result;
	}

	public Region union(Region r2) {
		return null;

	}

	public boolean isEmpty() {
		return bsList.isEmpty();
	}

	public Region minus(Region r2) {
		if (intersection(r2).isEmpty()) {
			return this;
		}
		List<BucketStructure> resList = new ArrayList<>();
		resList.addAll(bsList);
		for (BucketStructure bs2 : r2.bsList) {
			List<BucketStructure> tempList = new ArrayList<>();
			for (BucketStructure bs : resList) {
				tempList.addAll(bs.minus(bs2));
			}
			resList = tempList;
		}
		if (resList.isEmpty()) {
			return EMPTY_REGION;
		}
		Region result = new Region();
		result.bsList.addAll(resList);
		return result;
	}

	public boolean le(Region r2) {
		return minus(r2).isEmpty();
	}

	public Region getDeepCopy() {
		Region another = new Region();
		for (BucketStructure bucketStructure : bsList) {
			another.add(new BucketStructure(bucketStructure));
		}
		return another;
	}

	public boolean contains(Region r) {
		if (r.minus(this).isEmpty())
			return true;
		return false;
	}

	public boolean areEqual(Region r) {
		if ((r.minus(this)).isEmpty() && (this.minus(r)).isEmpty())
			return true;

		return false;
	}

}

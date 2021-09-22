package in.ac.iisc.cds.dsl.cdgvendor.model;

import java.util.List;

public class ExportBucket {

    int           constraintNo;
    int           attributeNo;
    List<Integer> attributeTrueBuckets;

    public ExportBucket(int constraintNo, int attributeNo, List<Integer> attributeTrueBuckets) {
        this.constraintNo = constraintNo;
        this.attributeNo = attributeNo;
        this.attributeTrueBuckets = attributeTrueBuckets;
    }

    @Override
    public String toString() {

        String str = attributeTrueBuckets.toString();
        str = str.substring(1, str.length() - 1);

        return constraintNo + " " + attributeNo + " " + str;
    }
}

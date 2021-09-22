package in.ac.iisc.cds.dsl.cdgvendor.model;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;

import in.ac.iisc.cds.dsl.cdgvendor.mmap.BigArrayReaderWriter;
import in.ac.iisc.cds.dsl.cdgvendor.utils.Converter;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;

public class ValueCombination implements Serializable {

    private static final long   serialVersionUID = 5804649605490297160L;

    private static final String COMMA            = ",";

    private final IntList       colValues;
    private long                rowcount;

    public ValueCombination(IntList colValues, long rowcount) {
        this.colValues = colValues;
        this.rowcount = rowcount;
    }

    public String toStringFileDump() {
        StringBuilder sb = new StringBuilder();
        sb.append(rowcount);
        for (IntIterator iter = colValues.iterator(); iter.hasNext();) {
            sb.append(COMMA).append(iter.nextInt());
        }
        return sb.toString();
    }

    /**
     * Only colValues appear
     * @return
     */
    public String toStringStaticDump() {
        StringBuilder sb = new StringBuilder();
        for (IntIterator iter = colValues.iterator(); iter.hasNext();) {
            sb.append(iter.nextInt()).append(COMMA);
        }
        String str = sb.toString();
        str = StringUtils.removeEnd(str, COMMA);
        return str;
    }

    public ValueCombination(String fileDumpStr) {
        String arr[] = fileDumpStr.split(COMMA);
        rowcount = Long.parseLong(arr[0]);
        colValues = new IntArrayList(arr.length - 1);
        for (int i = 1; i < arr.length; i++) {
            colValues.add(Integer.parseInt(arr[i]));
        }
    }

    public byte[] toByteArray() {
        if (rowcount > Integer.MAX_VALUE)
            throw new RuntimeException("Expected cases with int rowcounts here but found " + rowcount);

        IntList serializeList = new IntArrayList(colValues.size() + 1);
        serializeList.add((int) rowcount);
        serializeList.addAll(colValues);
        return Converter.toByteArr(serializeList.toIntArray());
    }

    public ValueCombination(byte[] barr) {
        int arr[] = Converter.toIntArr(barr);
        rowcount = arr[0];
        colValues = new IntArrayList(arr.length - 1);
        for (int i = 1; i < arr.length; i++) {
            colValues.add(arr[i]);
        }
    }

    public static class ValueCombinationSerializer implements Serializer<ValueCombination> {

        @Override
        public void serialize(DataOutput2 out, ValueCombination valueCombination) throws IOException {
            out.write(valueCombination.toByteArray());
        }

        @Override
        public ValueCombination deserialize(DataInput2 in, int available) throws IOException {
            byte[] arr = new byte[available];
            in.readFully(arr);
            return new ValueCombination(arr);
        }
    }

    public int getSizeInBytes() {
        return toByteArray().length;
    }

    public static class ValueCombinationReaderWriter implements BigArrayReaderWriter<ValueCombination> {

        private final int    messageSize;
        private final byte[] tempArr;

        public ValueCombinationReaderWriter(int messageSize) {
            this.messageSize = messageSize;
            tempArr = new byte[this.messageSize];
        }

        @Override
        public void write(ValueCombination valueCombination, ByteBuffer byteBuffer) {
            byteBuffer.put(valueCombination.toByteArray());
        }

        @Override
        public ValueCombination read(ByteBuffer buffer) {
            buffer.get(tempArr);
            return new ValueCombination(tempArr);
        }

        @Override
        public int messageSize() {
            return messageSize;
        }
    }

    public IntList getColValues() {
        return colValues;
    }

    public void reduceRowcount(long by) {
        rowcount -= by;
    }

    public long getRowcount() {
        return rowcount;
    }

    /**
     * Checks if this valueCombination represents same colValues
     * @param another
     * @return
     */
    public boolean hasSimilarValueCombination(IntList anotherColValues) {
        if (colValues.size() != anotherColValues.size())
            throw new RuntimeException("Some previous bug led to comparision of ValueCombination having diff number of colValues");

        return colValues.equals(anotherColValues);
    }

    @Override
    public String toString() {
        return rowcount + " | " + colValues;
    }

    public static void mergeSort(List<ValueCombination> list, IntList comparingIndxs, int left, int right) {
        if (left < right) {
            int mid = (left + right) / 2;
            mergeSort(list, comparingIndxs, left, mid);
            mergeSort(list, comparingIndxs, mid + 1, right);
            merge(list, comparingIndxs, left, mid, right);
        }
    }

    private static void merge(List<ValueCombination> list, IntList comparingIndxs, int left, int mid, int right) {
        int i, j;

        List<ValueCombination> resList = new ArrayList<>(right - left + 1);
        for (i = left, j = mid + 1; i <= mid && j <= right;) {
            if (list.get(i).le(list.get(j), comparingIndxs)) {
                resList.add(list.get(i++));
            } else {
                resList.add(list.get(j++));
            }
        }
        for (; i <= mid;) {
            resList.add(list.get(i++));
        }
        for (; j <= right;) {
            resList.add(list.get(j++));
        }

        for (int k = left; k <= right; k++) {
            list.remove(k);
            list.add(k, resList.get(k - left));
        }
    }

    private boolean le(ValueCombination another, IntList comparingIndxs) {

        IntList colValuesThis = colValues;
        IntList colValuesAnother = another.colValues;

        for (IntIterator iter = comparingIndxs.iterator(); iter.hasNext();) {
            int comparingIndx = iter.nextInt();
            if (colValuesThis.getInt(comparingIndx) < colValuesAnother.getInt(comparingIndx))
                return true;
            if (colValuesThis.getInt(comparingIndx) > colValuesAnother.getInt(comparingIndx))
                return false;
        }
        return true;
    }

}

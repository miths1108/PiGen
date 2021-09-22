package in.ac.iisc.cds.dsl.cdgvendor.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import it.unimi.dsi.fastutil.ints.IntList;

public class ViewSolutionDiskBased extends AbstractValueCombinationList implements ViewSolution {

	// TODO : remove hardcode
//    private static final String      DUMP_FILE_LOCATION   = "/Users/dharmendra.singh/Desktop/iisc/scratchInUse";
	private static final String      DUMP_FILE_LOCATION   = "../../scratchINUSE";
    private static final String      DUMP_FILENAME_PREFIX = "ViewSolutionDiskBased_";

    protected final File             file;
    protected final FileOutputStream fos;
    protected final int              aValueCombinationSizeInBytes;

    int                              valueCombinationCount;

    public ViewSolutionDiskBased(String viewname, int aValueCombinationSizeInBytes) {
        file = new File(DUMP_FILE_LOCATION, DUMP_FILENAME_PREFIX + viewname);
        try {
        	file.getParentFile().mkdirs();
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException ex) {
            throw new ExceptionInInitializerError(ex);
        }
        this.aValueCombinationSizeInBytes = aValueCombinationSizeInBytes;
        valueCombinationCount = 0;
    }

    @Override
    public void addValueCombination(ValueCombination valueCombination) {
        try {
            fos.write(valueCombination.toByteArray());
            valueCombinationCount++;
        } catch (IOException ex) {
            throw new RuntimeException("Unable to write ValueCombination " + valueCombination + " in file " + file.getAbsolutePath(), ex);
        }
    }

    @Override
    public int getCountOfValueCombinations() {
        return valueCombinationCount;
    }

    @Override
    public Iterator<ValueCombination> iterator() {
        try {
            fos.flush();
        } catch (IOException ex) {
            throw new RuntimeException("Unable to flush fos", ex);
        }

        Iterator<ValueCombination> it = new Iterator<ValueCombination>() {

            private int                   currentIndex;
            private final FileInputStream fis;
            private final byte[]          tempArr;

            {
                currentIndex = 0;
                try {
                    fis = new FileInputStream(file);
                } catch (FileNotFoundException ex) {
                    throw new ExceptionInInitializerError(ex);
                }
                tempArr = new byte[aValueCombinationSizeInBytes];
            }

            @Override
            public boolean hasNext() {
                return currentIndex < getCountOfValueCombinations();
            }

            @Override
            public ValueCombination next() {
                try {
                    fis.read(tempArr);
                } catch (IOException ex) {
                    throw new RuntimeException("Unable to read fis", ex);
                }
                currentIndex++;
                return new ValueCombination(tempArr);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
        return it;
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void close() {
        try {
            fos.close();
            file.delete();
        } catch (IOException ex) {
            throw new RuntimeException("Unable to close fos", ex);
        }
    }

    @Override
    public void prepareForSearch() {
        throw new UnsupportedOperationException("Unsupported being expensive");

    }

    @Override
    public boolean contains(IntList valuesInCombination) {
        throw new UnsupportedOperationException("Unsupported being expensive");
    }

    @Override
    public long getPK(IntList valuesInCombination) {
        throw new UnsupportedOperationException("Unsupported being expensive");
    }

    @Override
    public ViewSolution clone() {
        throw new UnsupportedOperationException("Unsupported being expensive");
    }
}

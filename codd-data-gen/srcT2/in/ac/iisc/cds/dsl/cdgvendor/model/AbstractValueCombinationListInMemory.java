package in.ac.iisc.cds.dsl.cdgvendor.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import in.ac.iisc.cds.dsl.cdgvendor.utils.StringUtils;
import in.ac.iisc.cds.dsl.cdgvendor.utils.StringUtils.Separator;

/**
 * In memory
 * @author rWX450917
 *
 */
public class AbstractValueCombinationListInMemory extends AbstractValueCombinationList {

    protected List<ValueCombination> valueCombinations;

    public AbstractValueCombinationListInMemory(int expectedCapacity) {
        valueCombinations = new ArrayList<>(expectedCapacity);
    }

    @Override
    public void addValueCombination(ValueCombination valueCombination) {
        valueCombinations.add(valueCombination);
    }

    @Override
    public int getCountOfValueCombinations() {
        return valueCombinations.size();
    }

    @Override
    public Iterator<ValueCombination> iterator() {
        Iterator<ValueCombination> it = new Iterator<ValueCombination>() {

            private int currentIndex = 0;

            @Override
            public boolean hasNext() {
                return currentIndex < getCountOfValueCombinations();
            }

            @Override
            public ValueCombination next() {
                return valueCombinations.get(currentIndex++);
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
        return "valueCombinations=\n" + StringUtils.join(valueCombinations, Separator.NEWLINE);
    }

    @Override
    public final void close() {
        valueCombinations = null;
    }
}

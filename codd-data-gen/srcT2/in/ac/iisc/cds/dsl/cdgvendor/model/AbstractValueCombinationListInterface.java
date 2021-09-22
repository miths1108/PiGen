package in.ac.iisc.cds.dsl.cdgvendor.model;

import java.util.List;

public interface AbstractValueCombinationListInterface extends Iterable<ValueCombination> {

    /**
     * Adds a ValueCombination
     * @param valueCombination
     */
    public abstract void addValueCombination(ValueCombination valueCombination);

    /**
     * @return count of ValueCombinations stored
     */
    public abstract int getCountOfValueCombinations();

    public abstract List<ValueCombination> getValueCombinations();

    public abstract void close();

}

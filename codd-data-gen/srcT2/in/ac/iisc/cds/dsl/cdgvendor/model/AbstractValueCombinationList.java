package in.ac.iisc.cds.dsl.cdgvendor.model;

import java.util.List;

/**
 * ViewSolution is extended i.e., its valueCombinations hold values for all nonkeys in a view
 * Acts like a List<ValueCombination> by supporting add(), get() and size() operations
 * @author rWX450917
 *
 */
public abstract class AbstractValueCombinationList implements AbstractValueCombinationListInterface {

    @Override
    public final List<ValueCombination> getValueCombinations() {
        throw new UnsupportedOperationException();
    }

}

package in.ac.iisc.cds.dsl.cdgvendor.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains separate maps indexed by (orig)columnnames having a Map of old boundry value to domainMapped boudry value
 * @author dsladmin
 *
 */
public class HistogramMappingInfo {
    public Map<String, Map<Double, Integer>> shownValuesByNumberColumns = new HashMap<>();
    public Map<String, Map<String, Integer>> shownValuesByStringColumns = new HashMap<>();
    public Map<String, Map<Date, Integer>>   shownValuesByDateColumns   = new HashMap<>();

    @Override
    public String toString() {
        return "HistogramMappingInfo [shownValuesByNumberColumns=" + shownValuesByNumberColumns + ", shownValuesByStringColumns=" + shownValuesByStringColumns
                + ", shownValuesByDateColumns=" + shownValuesByDateColumns + "]";
    }

}

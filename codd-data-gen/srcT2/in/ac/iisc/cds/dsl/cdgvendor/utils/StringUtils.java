package in.ac.iisc.cds.dsl.cdgvendor.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;

public class StringUtils {

    public enum Separator {
        SPACE(" "),
        COMMA(","),
        PIPE("|"),
        NEWLINE("\n");

        private final String str;

        private Separator(String str) {
            this.str = str;
        }
    }

    public static final String SPACE = "";

    public static String join(IntList list, Separator separator) {
        if (list.isEmpty())
            return "";

        StringBuilder sb = new StringBuilder();
        IntIterator iter = list.iterator();
        sb.append(iter.nextInt());
        while (iter.hasNext()) {
            sb.append(separator.str).append(iter.nextInt());
        }
        return sb.toString();
    }

    public static String join(Collection<? extends Object> collection, Separator separator) {
        return org.apache.commons.lang3.StringUtils.join(collection, separator.str);
    }
    
    public static List<String> toStringList(List<Object> list) {
    	List<String> newList = new ArrayList<String>();
    	for (Object obj : list) {
			newList.add(obj.toString());
		}
    	return newList;
    }
}

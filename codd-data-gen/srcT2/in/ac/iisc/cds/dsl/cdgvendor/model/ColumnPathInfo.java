package in.ac.iisc.cds.dsl.cdgvendor.model;

import java.util.List;

/*
 *  using this class for replacing column name with column identifier,
 *  so, as to diffrentiate same column name with different path.
 * 
 */

public class ColumnPathInfo {
	
	String colName;			// anonymized column name
	List<String> colPath;  // list of relations
	
	public ColumnPathInfo(String colName, List<String> colPath)
	{
		this.colName = colName;
		this.colPath = colPath;
	}

	// Don't change toString() method, it used for generating columnPath in vendor side by
	// string manipulation
	@Override
	public String toString()
	{
		return "(" + colName + ": "  + colPath + ")";
	}

	public String getColname() {
		
		return colName;
	}
	
	public List<String> getColPath() {
		return colPath;
	}
	
	public String getTargetView() {
		int len = colPath.size()-1;
		return colPath.get(colPath.size()-1);
	}
	
}


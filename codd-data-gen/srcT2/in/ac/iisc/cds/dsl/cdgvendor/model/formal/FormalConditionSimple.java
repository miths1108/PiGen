package in.ac.iisc.cds.dsl.cdgvendor.model.formal;


import java.util.ArrayList;
import java.util.List;

public abstract class FormalConditionSimple extends FormalCondition {

    private static final long     serialVersionUID = -603197913843185001L;
    protected static final String DOT              = ".";
    protected static final String SPLITTER         = "\\.";

    protected String              columnname;
    protected Symbol              symbol;
    protected List<String>        colPath;   // added 			

    public FormalConditionSimple(String columnname, String symbolStr) {
        //Removing Aliasing. assuming columnnames are anyways unique
        if (columnname.contains(DOT)) {
            columnname = columnname.split(SPLITTER)[1];
        }
        this.columnname = columnname;
        symbol = Symbol.getSymbolFromString(symbolStr);
    }
    
    public FormalConditionSimple(FormalConditionSimple that) {
    	this.columnname = that.columnname;
        this.symbol = that.symbol;
        this.viewname = that.viewname;
        this.outputCardinality = that.outputCardinality;
        this.colPath = that.colPath;
        
        this.queryName = that.queryName;
    }

    public String getColumnname() {
        return columnname;
    }

    public void setColumnname(String columnname) {
        this.columnname = columnname;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    @Override
    public String asString() {
        return columnname + " " + symbol;
    }

    @Override
    public String asQueryString() {
        return columnname + " " + symbol.symbolString;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (columnname == null ? 0 : columnname.hashCode());
        result = prime * result + (symbol == null ? 0 : symbol.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        FormalConditionSimple other = (FormalConditionSimple) obj;
        if (columnname == null) {
            if (other.columnname != null)
                return false;
        } else if (!columnname.equals(other.columnname))
            return false;
        if (symbol != other.symbol)
            return false;
        return true;
    }
    
    public List<String> getColPath()
    {
    	return colPath;
    }
    
    public void setColPath(List<String> col_path)
    {
    	this.colPath = new ArrayList<>();
    	this.colPath.addAll(col_path);
    }
}

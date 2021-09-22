package in.ac.iisc.cds.dsl.cdgvendor.model;

public class ColumnInfo {
	private final int ordinalPosition;
	private final String columnType;
	
	public int getOrdinalPosition() {
		return ordinalPosition;
	}
	
	public String getColumnType() {
		return columnType;
	}
	
	public ColumnInfo(int ordinalPosition, String columnType) {
		this.ordinalPosition = ordinalPosition;
		this.columnType = columnType;
	}
	
	@Override
	public String toString() {
		return ordinalPosition+":"+columnType;
	}
}
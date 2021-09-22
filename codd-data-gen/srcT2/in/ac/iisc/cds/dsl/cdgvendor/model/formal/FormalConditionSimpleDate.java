package in.ac.iisc.cds.dsl.cdgvendor.model.formal;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class FormalConditionSimpleDate extends FormalConditionSimple {
    protected Date                  value;

    private static final DateFormat DATE_FORMAT      = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        TIMESTAMP_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public FormalConditionSimpleDate(String columnname, String symbolStr, String dateStr) {
        super(columnname, symbolStr);

        try {
            value = TIMESTAMP_FORMAT.parse(dateStr);
        } catch (ParseException e) {
            try {
                value = DATE_FORMAT.parse(dateStr);
            } catch (ParseException es) {
                throw new ExceptionInInitializerError("Unable to parse dateStr " + dateStr);
            }
        }
    }

    public FormalConditionSimpleDate(String columnname, String symbolStr, Date value) {
    	super(columnname, symbolStr);
    	this.value = value;
	}

	public FormalConditionSimpleDate(FormalConditionSimpleDate formalConditionSimpleDate) {
		super(formalConditionSimpleDate);
		this.value = formalConditionSimpleDate.value;
	}

	@Override
    public String asString() {
        return super.asString() + " " + TIMESTAMP_FORMAT.format(value);
    }

    @Override
    public String asQueryString() {
        return super.asQueryString() + " " + TIMESTAMP_FORMAT.format(value);
    }

    public Date getValue() {
        return value;
    }

    @Override
    public FormalConditionSimpleDate getDeepCopy() {
        FormalConditionSimpleDate another = new FormalConditionSimpleDate(columnname, symbol.symbolString, TIMESTAMP_FORMAT.format(value));
        another.outputCardinality = outputCardinality;
        another.viewname = viewname;
        another.colPath = colPath;
        
        return another;
    }
}

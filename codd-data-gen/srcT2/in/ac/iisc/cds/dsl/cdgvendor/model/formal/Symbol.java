package in.ac.iisc.cds.dsl.cdgvendor.model.formal;

public enum Symbol {
    EQ("="),
    LT("<"),
    LE("<="),
    GT(">"),
    GE(">=");

    String symbolString;

    private Symbol(String symbolString) {
        this.symbolString = symbolString;
    }

    public static Symbol getSymbolFromString(String symbolStr) {
        for (Symbol symbol : values()) {
            if (symbol.symbolString.equals(symbolStr) || symbol.toString().equals(symbolStr))
                return symbol;
        }
        throw new ExceptionInInitializerError("Unable to recognize symbolStr " + symbolStr);
    }

}

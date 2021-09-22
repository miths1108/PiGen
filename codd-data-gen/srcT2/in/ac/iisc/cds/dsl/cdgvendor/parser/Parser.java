package in.ac.iisc.cds.dsl.cdgvendor.parser;

import in.ac.iisc.cds.dsl.cdgvendor.model.Alqp;

/**
 * The second step.
 * Parse various engines standard textual (json) Explain Analyze outputs to Alqp which later deduce cardinality constraints
 * @author raghav
 *
 */
public abstract class Parser {
    public abstract Alqp parse(String ea);
}

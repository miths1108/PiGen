package in.ac.iisc.cds.dsl.cdgclient.preprocess;

import java.util.List;

import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalCondition;

public class AlqpToCCPostgresRes {

    public final List<List<FormalCondition>> alqpOrigFormalConditions;
    public final List<List<FormalCondition>> alqpAnonymFormalConditions;
    public final List<String>                anonymQueries;

    public AlqpToCCPostgresRes(List<List<FormalCondition>> alqpFormalConditions, List<List<FormalCondition>> alqpAnonymFormalConditions,
            List<String> anonymQueries) {
        alqpOrigFormalConditions = alqpFormalConditions;
        this.alqpAnonymFormalConditions = alqpAnonymFormalConditions;
        this.anonymQueries = anonymQueries;
    }
}

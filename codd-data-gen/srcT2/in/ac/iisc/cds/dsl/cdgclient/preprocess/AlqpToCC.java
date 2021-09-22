package in.ac.iisc.cds.dsl.cdgclient.preprocess;

import java.util.List;

import in.ac.iisc.cds.dsl.cdgclient.anonymizer.Anonymizer;
import in.ac.iisc.cds.dsl.cdgvendor.model.Alqp;
import in.ac.iisc.cds.dsl.cdgvendor.model.HistogramMappingInfo;

/**
 * Extracts conditions from given list of ALQPs and anonymizes and domainmaps them.
 * Also generates anonymized and domainmapped workload queries (as per the new schema)
 * @author raghav
 *
 */
public abstract class AlqpToCC {

    protected final Anonymizer anonymizer;

    public AlqpToCC(Anonymizer anonymizer) {
        this.anonymizer = anonymizer;
    }

    public abstract AlqpToCCPostgresRes anonymizeAlqpsAndGenerateCCsAndQueries(List<Alqp> alqps, HistogramMappingInfo histogramMappingInfo);
}

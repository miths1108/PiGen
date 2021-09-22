package in.ac.iisc.cds.dsl.cdgclient.preprocess;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import in.ac.iisc.cds.dsl.cdgclient.anonymizer.Anonymizer;
import in.ac.iisc.cds.dsl.cdgclient.constants.PostgresCConfig;
import in.ac.iisc.cds.dsl.cdgclient.constants.PostgresCConfig.Key;
import in.ac.iisc.cds.dsl.cdgvendor.model.Alqp;
import in.ac.iisc.cds.dsl.cdgvendor.model.TableInfo;
import in.ac.iisc.cds.dsl.cdgvendor.utils.FileUtils;

public class QueryGenerator {

    private static final String COMMA   = ",";
    private static final String NEWLINE = "\n";

    public void generateDDL(Anonymizer anonymizer, List<Alqp> alqps) {
        Map<String, TableInfo> tableInfoMap = anonymizer.getAnonymTableInfoMap();

        StringBuilder sb = new StringBuilder();

        String str = sb.toString();
        str = StringUtils.removeEnd(str, NEWLINE);
        try {
            FileUtils.writeStringToFile(PostgresCConfig.getProp(Key.DDLGENERATED_TARGETFILE), str);
        } catch (Exception ex) {
            throw new RuntimeException("Unable to write generate ddl", ex);
        }

    }

}

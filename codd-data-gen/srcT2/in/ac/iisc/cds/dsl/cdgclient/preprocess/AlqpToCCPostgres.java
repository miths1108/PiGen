package in.ac.iisc.cds.dsl.cdgclient.preprocess;


import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONObject;

import in.ac.iisc.cds.dsl.cdgclient.anonymizer.Anonymizer;
import in.ac.iisc.cds.dsl.cdgclient.constants.PostgresCConfig;
import in.ac.iisc.cds.dsl.cdgclient.constants.PostgresCConfig.Key;
import in.ac.iisc.cds.dsl.cdgvendor.model.Alqp;
import in.ac.iisc.cds.dsl.cdgvendor.model.CCInfo;
import in.ac.iisc.cds.dsl.cdgvendor.model.ColumnPathInfo;
import in.ac.iisc.cds.dsl.cdgvendor.model.Condition;
import in.ac.iisc.cds.dsl.cdgvendor.model.HistogramMappingInfo;
import in.ac.iisc.cds.dsl.cdgvendor.model.formal.FormalCondition;
import in.ac.iisc.cds.dsl.cdgvendor.utils.Converter;
import in.ac.iisc.cds.dsl.cdgvendor.utils.DebugHelper;
import in.ac.iisc.cds.dsl.cdgvendor.utils.FileUtils;
import in.ac.iisc.cds.dsl.cdgvendor.utils.SerializationHelper;
//import jdk.internal.org.objectweb.asm.tree.AnnotationNode;

/**
 * Reads explain analyze jsons and extracts conditions out of it.
 * Also anonymizes the constraints
 * @author raghav
 *
 */
public class AlqpToCCPostgres extends AlqpToCC {

    private static final String NEWLINE = "\n";

    public AlqpToCCPostgres(Anonymizer anonymizer) {
        super(anonymizer);
    }

    @Override
    public AlqpToCCPostgresRes anonymizeAlqpsAndGenerateCCsAndQueries(List<Alqp> alqps, HistogramMappingInfo histogramMappingInfo) {

        DebugHelper.printInfo("-------- Generating anonymized CCs from " + alqps.size() + " Postgres ALQPs ------------");
        
        
        
    // code added for shadab: for query names in formal condition
        List<String> eaIndex = FileUtils.readLines(PostgresCConfig.getProp(Key.EXPANALYZE_LOCATION), PostgresCConfig.EXPANALYZE_INDEX);
        List<String> easFromFiles = new ArrayList<>();    // EA, Plan in Json String
        List<String> queryNames = new ArrayList<>();
        for (String eaFilename : eaIndex) {
            if (eaFilename.startsWith("#"))
                continue;
            String eaStr = FileUtils.readFileToString(PostgresCConfig.getProp(Key.EXPANALYZE_LOCATION), eaFilename);
            easFromFiles.add(eaStr);
            queryNames.add(eaFilename);
        }

        
        
        System.err.println(anonymizer.getTablenameAnonymMap());
        System.err.println(anonymizer.getColumnnameAnonymMap());

        List<List<FormalCondition>> alqpFormalConditions = new ArrayList<>(alqps.size());

        List<FormalCondition> allFormalConditions = new ArrayList<>();
        for (int i = 0; i < alqps.size(); i++) {
        	System.out.println(i);;
            Alqp alqp = alqps.get(i);
            DebugHelper.printDebug("\nAlqp " + (i + 1) + "/" + alqps.size() + ": ");

            //Step 1: Extract CCs
            /*
             *   someConditon : | σ ( (d_year = 2001) ) (date_dim≡date_dim) | = 365
             * 
             *   someFormalConditon : View date_dim d_year EQ 2001.0 = 365
             */
            List<Condition> someConditions = alqp.getAllConditions(PostgresCConfig.BASICSCHEMA_INFO.getTableInfos());
            DebugHelper.printDebug("\nAlqp conditions: ");
            DebugHelper.printConditions(someConditions);
            
            List<FormalCondition> someFormalConditions = Converter.getAsFormalConditions(someConditions, anonymizer);
            DebugHelper.printDebug("\nAlqp formal conditions: ");
            DebugHelper.printFormalConditions(someFormalConditions);
            
            // adding for shadab
            String queryname = queryNames.get(i);
            for(FormalCondition formalConditions : someFormalConditions)
            {
                formalConditions.setQueryname(queryname);
            }
            alqpFormalConditions.add(deepCopy(someFormalConditions));
            allFormalConditions.addAll(someFormalConditions);
        }

        //Step 2: Anonymize CCs
        anonymizer.anonymizeAllFormalConditions(allFormalConditions);
        DebugHelper.printDebug("\nAll anonym formal conditions: ");
        DebugHelper.printFormalConditions(allFormalConditions);

        List<String> anonymQueries = new ArrayList<>();
        //Step 3: Domainmap CCs
        /*
         *  allFormalConditons : List of anonymized FormalConditon
         *  alqps : List of Alqp(plan tree) of each query
         *  can remove anonymQueries from function parameter 
         *  
         */
        
        List<FormalCondition> mappedFormalConditions =
                anonymizer.domainmapAllFormalConditionsAndGenerateQueries(allFormalConditions, histogramMappingInfo, alqps, anonymQueries);
        
        
        System.out.println("tarun anonymQueries : " + mappedFormalConditions);
        List<List<FormalCondition>> alqpAnonymFormalConditions = new ArrayList<>(alqps.size());
        System.out.println("arun alqps.size() : " + alqps.size());
        // Make list of list of formal conditions from above computed anonymized domain-mapped formal condition
        for (int i = 0, lastSeenIndx = -1; i < alqps.size(); i++) {
            int startIndx = lastSeenIndx + 1; //included
            int endIndx = startIndx + alqpFormalConditions.get(i).size(); //excluded
            List<FormalCondition> someAnonymFormalConditions = mappedFormalConditions.subList(startIndx, endIndx);
            alqpAnonymFormalConditions.add(someAnonymFormalConditions);
            lastSeenIndx = endIndx - 1;
        }
        System.out.println("Tarun alqpAnonymFormalConditions : " + alqpAnonymFormalConditions);

        //Step 4: Unique Domainmap CCs : remove duplicate CCs
        List<FormalCondition> uniqueMappedFormalConditions = new ArrayList<>(new HashSet<>(mappedFormalConditions));

        DebugHelper.printDebug("All unique domainmapped anonym formal conditions (" + mappedFormalConditions.size() + " uniqued "
                + uniqueMappedFormalConditions.size() + "): ");
        DebugHelper.printFormalConditions(uniqueMappedFormalConditions);
        
        // Step 4.1 (After 4) :  giving identifier to each anonymized column 4
//        Map<String, ColumnPathInfo> ColumnIndentifierMap = new HashMap<>();  // String:columnId  t01_c001 + '_'  +  Hash(ColPAth) : t01_c001_131313 : {Colname, Columnpath}
//        anonymizer.giveUniqueIdToAnonymizedCol(uniqueMappedFormalConditions, ColumnIndentifierMap);
//        System.out.println("After column identifier " + ColumnIndentifierMap);
//
//       // System.out.println("After column identifier " + anonymizer);
//        DebugHelper.printFormalConditions(uniqueMappedFormalConditions);
//        
//        JSONObject result = new JSONObject();
//        for(Entry<String, ColumnPathInfo> e :ColumnIndentifierMap.entrySet())
//        {
//        	result.put(e.getKey(),e.getValue());
//        }
//        
//        //FileUtils.writeStringToFile(PostgresCConfig.getProp(Key.COLUMNIDMAP_TARGETLOCATION), result.toString());
//               
//        try (FileWriter file = new FileWriter(PostgresCConfig.getProp(Key.COLUMNIDMAP_TARGETLOCATION))) {
//        	 
//            file.write(result.toString());
//            file.flush();
//            file.close();
// 
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        
             

        //Step 5: Dump CCs
        Map<String, List<FormalCondition>> viewnameToCCMap = new HashMap<>();
        for (FormalCondition mappedFormalCondition : uniqueMappedFormalConditions) {
            String viewname = mappedFormalCondition.getViewname();
            List<FormalCondition> ccList = viewnameToCCMap.get(viewname);
            if (ccList == null) {
                ccList = new ArrayList<>();
                viewnameToCCMap.put(viewname, ccList);
            }
            ccList.add(mappedFormalCondition);
        }

        CCInfo ccInfo = new CCInfo();
        ccInfo.setViewnameToCCMap(viewnameToCCMap);
        // dumps CCs to targetLocation : ANONYMIZEDCCS_TARGETFILE
        SerializationHelper.serializeCCInfo(ccInfo, PostgresCConfig.getProp(Key.ANONYMIZEDCCS_TARGETFILE));
        DebugHelper.printInfo("-------- Done Generating anonymized CCs from " + alqps.size() + " Postgres ALQPs ------------");

        AlqpToCCPostgresRes res = new AlqpToCCPostgresRes(alqpFormalConditions, alqpAnonymFormalConditions, anonymQueries);
        return res;
    }

    //    private static List<Condition> getAllConditions(List<Alqp> alqps) {
    //
    //        List<Condition> allconditions = new ArrayList<>();
    //        for (Alqp alqp : alqps) {
    //            List<Condition> conditions = alqp.getAllConditions();
    //            allconditions.addAll(conditions);
    //        }
    //        return allconditions;
    //
    //    }

    private List<FormalCondition> deepCopy(List<FormalCondition> formalConditions) {
        List<FormalCondition> another = new ArrayList<>();
        for (FormalCondition formalCondition : formalConditions) {
            another.add(formalCondition.getDeepCopy());
        }
        return another;
    }
}

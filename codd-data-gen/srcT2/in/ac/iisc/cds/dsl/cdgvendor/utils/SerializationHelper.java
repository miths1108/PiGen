package in.ac.iisc.cds.dsl.cdgvendor.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import in.ac.iisc.cds.dsl.cdgvendor.model.CCInfo;
import in.ac.iisc.cds.dsl.cdgvendor.model.ColumnPathInfo;

public class SerializationHelper {

    public static void serializeCCInfo(CCInfo ccInfo, String filename) {
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(ccInfo);
            oos.close();
            fos.close();
        } catch (IOException ex) {
            throw new RuntimeException("Unable to serialize ccInfo", ex);
        }
    }

    public static CCInfo deserializeCCInfo(String filename) {
        try {
            FileInputStream fis = new FileInputStream(filename);
            ObjectInputStream ois = new ObjectInputStream(fis);
            CCInfo ccInfo = (CCInfo) ois.readObject();
            ois.close();
            fis.close();
            return ccInfo;
        } catch (ClassNotFoundException | IOException ex) {
            throw new RuntimeException("Unable to deserialize ccInfo", ex);
        }
    }

	public static void serilaizeColumnIdMap(Map<String, ColumnPathInfo> columnIndentifierMap, String filename) {
		
		try {
			FileOutputStream fos = new FileOutputStream(filename);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(columnIndentifierMap);
            oos.close();
            fos.close();
        } catch (IOException ex) {
            throw new RuntimeException("Unable to serialize columnIndentifierMap", ex);
        }
	}
	
	public static Map<String, ColumnPathInfo> deserilaizeColumnIdMap(String filename) {
        try {
            FileInputStream fis = new FileInputStream(filename);
            ObjectInputStream ois = new ObjectInputStream(fis);
            //CCInfo ccInfo = (CCInfo) ois.readObject();
            Map<String, ColumnPathInfo> columnIndentifierMap =  (Map<String, ColumnPathInfo>) ois.readObject();;
            ois.close();
            fis.close();
            return columnIndentifierMap;
        } catch (ClassNotFoundException | IOException ex) {
            throw new RuntimeException("Unable to deserialize ccInfo", ex);
        }
    }
		
	
    
    
    
    
}

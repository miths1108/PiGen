package in.ac.iisc.cds.dsl.cdgvendor.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public enum ConfigProvider {
    INSTANCE;

    public Map<String, String> getPropsFromFile(String propsFilename) {
        Properties props = new Properties();

        try {
            InputStream ins = getClass().getClassLoader().getResourceAsStream(propsFilename);
            if (ins != null) {
                props.load(ins);
                try {
                    ins.close();
                } catch (Exception ex) {}
            } else {
                throw new FileNotFoundException("Properties file '" + propsFilename + "' not found in the classpath");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new ExceptionInInitializerError(ex);
        }

        Map<String, String> propsMap = new HashMap<>();
        for (Object k : props.keySet()) {
            String key = (String) k;
            propsMap.put(key, props.getProperty(key));
        }
        return propsMap;
    }
}

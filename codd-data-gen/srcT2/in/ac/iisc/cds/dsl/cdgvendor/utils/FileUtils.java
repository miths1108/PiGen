package in.ac.iisc.cds.dsl.cdgvendor.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import in.ac.iisc.cds.dsl.cdgclient.constants.PostgresCConfig;

public class FileUtils {
    public static String readFileToString(String filename) {
        try {
            String str = org.apache.commons.io.FileUtils.readFileToString(new File(filename), PostgresCConfig.CHARSET);
            return str;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String readFileToString(String location, String filename) {
        try {
            String str = org.apache.commons.io.FileUtils.readFileToString(new File(location, filename), PostgresCConfig.CHARSET);
            return str;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static List<String> readLines(String filename) {
        try {
            List<String> lines = org.apache.commons.io.FileUtils.readLines(new File(filename), PostgresCConfig.CHARSET);
            return lines;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static List<String> readLines(String location, String filename) {
        try {
            List<String> lines = org.apache.commons.io.FileUtils.readLines(new File(location, filename), PostgresCConfig.CHARSET);
            return lines;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void writeStringToFile(String filename, String text) {
        try {
            org.apache.commons.io.FileUtils.writeStringToFile(new File(filename), text, PostgresCConfig.CHARSET);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void writeStringToFile(String location, String filename, String text) {
        try {
            org.apache.commons.io.FileUtils.writeStringToFile(new File(location, filename), text, PostgresCConfig.CHARSET);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}

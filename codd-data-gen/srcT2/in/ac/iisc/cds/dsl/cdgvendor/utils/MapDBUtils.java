package in.ac.iisc.cds.dsl.cdgvendor.utils;

import java.util.List;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

public class MapDBUtils {
    private static final DB db;

    private static boolean  isValid;

    static {
        //db = DBMaker.fileDB(MAPDB_SCRATCH_FILEPATH).fileMmapEnable().make();
        //db = DBMaker.memoryDB().closeOnJvmShutdown().concurrencyDisable().make();
        //db = DBMaker.tempFileDB().fileMmapEnable().closeOnJvmShutdown().concurrencyDisable().make();  //fileMmapEnable failing. Refer http://www.mapdb.org/blog/mmap_file_and_jvm_crash/
        db = DBMaker.tempFileDB().closeOnJvmShutdown().concurrencyDisable().make();

        isValid = true;
    }

    public static <E extends Object> List<E> createIndexTreeList(String listname, Serializer<E> serializer) {
        return db.indexTreeList(listname, serializer).create();
    }

    public static void closeAll() {
        if (isValid) {
            db.close();
            isValid = false;
        } else
            throw new RuntimeException("Trying to close on FileBackedMapUtils which is already invalid");
    }

}

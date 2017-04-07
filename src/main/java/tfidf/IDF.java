package tfidf;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import p1.DirectIndex;
import p1.FileLoader;
import p1.MyPair;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Gheorghe on 4/6/2017.
 */
public class IDF {

//    public  static Map<String, Double> idf = new ConcurrentHashMap<>();

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public void calculateIDF(String pathToDir) throws IOException {

        int nrTotalFisiere = ((ArrayList<File>)FileLoader.getFilesForDirectoryPath(pathToDir, ".txt")).size();
        List<File> files = FileLoader.getFilesForInternalPath("InverseIndex", ".ii");

        for(File file : files){
            calculateIDF(file, nrTotalFisiere);
        }
    }

    private void calculateIDF(File file, int nrFile) throws IOException {
        System.out.println("CALCULATE IDF " + Thread.currentThread().getId() + " " + file.getName());

        Map<String, List<MyPair>> map = objectMapper.readValue(file, new TypeReference<TreeMap<String,ArrayList<MyPair>>>() {});
        Map<String, Double> idfMap = new TreeMap<>();

        for(Map.Entry entry : map.entrySet()){
            int nrFilesForWord = ((List<DirectIndex>)entry.getValue()).size();

            idfMap.put((String) entry.getKey(), Math.log((double)nrFile / nrFilesForWord));
        }

        String newPath = file.getPath().toString().replace("InverseIndex", "IDF")
                                                    .replace(".ii", ".idf");
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(newPath), idfMap);
    }


}

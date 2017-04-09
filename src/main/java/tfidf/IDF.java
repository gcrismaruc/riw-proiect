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
public class IDF implements Runnable{

//    public  static Map<String, Double> idf = new ConcurrentHashMap<>();

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private File file;
    private double nrTotalFisiere;

    public IDF(File file, double nrTotalFisiere){
        this.file = file;
        this.nrTotalFisiere = nrTotalFisiere;
    }

    public void calculateIDF(String pathToDir) throws IOException {

        int nrTotalFisiere = ((ArrayList<File>)FileLoader.getFilesForDirectoryPath(pathToDir, ".txt")).size();
        List<File> files = FileLoader.getFilesForInternalPath("InverseIndex", ".ii");

        for(File file : files){
            calculateIDF(file, nrTotalFisiere);
        }
    }

    /**
     * Calculeaza idf-ul pentru toate cuvintele dintr-un fisier dat ca parametru
     * @param file
     * @param nrFile
     * @throws IOException
     */
    private void calculateIDF(File file, double nrFile) throws IOException {
        System.out.println("CALCULATE IDF " + Thread.currentThread().getId() + " " + file.getName());

        Map<String, List<MyPair>> map = objectMapper.readValue(file, new TypeReference<TreeMap<String,ArrayList<MyPair>>>() {});
        Map<String, Double> idfMap = new TreeMap<>();

        for(Map.Entry entry : map.entrySet()){
            int nrFilesForWord = ((List<DirectIndex>)entry.getValue()).size();

            idfMap.put((String) entry.getKey(), Math.log((double)nrFile / nrFilesForWord) + 1);
        }

        String newPath = file.getPath().toString().replace("InverseIndex", "IDF")
                                                    .replace(".ii", ".idf");
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(newPath), idfMap);
    }


    @Override
    public void run() {
        try {
            calculateIDF(this.file, this.nrTotalFisiere);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

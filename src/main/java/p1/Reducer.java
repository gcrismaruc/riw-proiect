package p1;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Gheorghe on 4/2/2017.
 */
public class Reducer implements  Runnable{

    private final Object writeLock = new Object();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final Gson gson = new Gson();


    public static String PHASE_ONE = "PHASE_ONE";
    public static String PHASE_TWO = "PHASE_TWO";

    private String phase;
    private Path path;
    private char c;

    public Reducer(){

    }

    public Reducer(Path path, String phase){
        this.path = path;
        this.phase = phase;
    }

    public Reducer( char c, String phase){
        this.phase = phase;
        this.c = c;
    }

    /**
     * Executa o reducere pe un set de fisiere ce incep cu un anumit caracter si creaza un fisier de forma
     * <litera>DirectIndex.idc
     * outputul se va afla in directorul DirectIndex
     * fisierele pentru care se face reducerea sunt in directorul temp
     * @param c
     * @throws IOException
     */
    public void reducePhaseOne(char c) throws IOException {
        List<File> files = FileLoader.getFilesForChar(c);

        List<DirectIndex> tempMap = new ArrayList<>();

        for(File file : files){
            tempMap.addAll(objectMapper.readValue(file, new TypeReference<ArrayList<DirectIndex>>() {}));
        }

        String filePath = Constants.PATH_TO_DIRECT_INDEX_DIRECTORY + c + "DirectIndex.idc";
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), tempMap);
    }

    /**
     * Calculeaza indexul invers pentru fisierele din directorul DirectIndex
     * outputul se va afla in directorul InverseIndex
     * @param path
     * @throws IOException
     */
    public void reducePhaseTwo(Path path) throws IOException {

           List<DirectIndex> directIndex = objectMapper.readValue(new File(String.valueOf(path)), new TypeReference<ArrayList<DirectIndex>>() {
           });
           System.out.println(PHASE_TWO + "_REDUCER  " + Thread.currentThread().getId() + "   " + path.toString());

           String fileName = path.toString().replace("DirectIndex", "InverseIndex").replace(".idc", ".ii");
           Map<String, List<MyPair>> map = new TreeMap<>();

           for (DirectIndex d : directIndex) {
               String word = d.getKey();
               MyPair pair = d.getValue();

               if (map.containsKey(word)) {
                   List<MyPair> pairs = map.get(word);
                   pairs.add(pair);
                   map.put(word, pairs);
               } else {
                   List<MyPair> pairs = new ArrayList<>();
                   pairs.add(pair);
                   map.put(word, pairs);
               }
           }
           File file = new File(fileName);
           objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, map);
    }

    @Override
    public void run() {
        switch (this.phase) {
            case "PHASE_ONE":
                try {
                    reducePhaseOne(this.c);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case "PHASE_TWO":
                try {
                    reducePhaseTwo(this.path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}

package p1;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static String PHASE_ONE = "PHASE_ONE";
    public static String PHASE_TWO = "PHASE_TWO";

    private String phase;
    private Path path;

    public Reducer(){

    }

    public Reducer(Path path, String phase){
        this.path = path;
        this.phase = phase;
    }

    public void method1(Path path) throws IOException {
        List<DirectIndex> directIndex = objectMapper.readValue(new File(String.valueOf(path)), new TypeReference<ArrayList<DirectIndex>>() {});
        System.out.println(PHASE_TWO + "_REDUCER  " + Thread.currentThread().getId() + "   " + path.toString());

        String fileName = path.toString().replace("DirectIndex","InverseIndex").replace(".idc", ".ii");

        Map<String, List<MyPair>> map = new TreeMap<>();

        for(DirectIndex d : directIndex){
            String word = d.getKey();
            MyPair pair=  d.getValue();

            if(map.containsKey(word)){
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
            case "PHASE_TWO":
                try {
                   // long startTime = System.currentTimeMillis();
                    method1(this.path);
                   // long endTime = System.currentTimeMillis();
                    //System.out.println("Thread: " + Thread.currentThread().getId() + " time = " + (endTime - startTime));

                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}

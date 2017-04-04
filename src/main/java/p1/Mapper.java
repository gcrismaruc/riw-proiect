package p1;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Gheorghe on 4/2/2017.
 */
public class Mapper implements  Runnable{

    private static final JsonFactory jsonFactory = new JsonFactory();
    private final Object writeLock = new Object();
    public  static Map<String, Long> totalNumberOfWords = new ConcurrentHashMap<>();

    public static ObjectMapper objectMapper = new ObjectMapper();

    private Path path;

    public static String PHASE_ONE = "PHASE_ONE";
    public static String PHASE_TWO = "PHASE_TWO";
    public static String COUNT_WORDS = "COUNT_WORDS";

    private String phase;
    public Mapper(){

    }

    public Mapper(Path path, String phase){
        this.path = path;
        this.phase = phase;
        objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
//        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
    }
    public static String mapPhaseOne(Path path) throws IOException {

        int countedWords = 0;
        String fileName = String.valueOf(path.getFileName());
        fileName.replace(".txt", ".idc");
        String text = new String(Files.readAllBytes(path));

        Map<String, MyPair> directIndex = TextParser.getParsedWords(text, path.toString().replace(".txt", ".idc"));
        File indexDirectFile = new File(String.valueOf(path).replace(".txt", ".idc"));

        objectMapper.writerWithDefaultPrettyPrinter().writeValue(indexDirectFile, directIndex);

        return path.toString().replace(".txt", ".idc");
    }

    public void mapPhaseTwo(Path path) throws IOException {
        Map<String, MyPair> directIndex = objectMapper.readValue(new File(String.valueOf(path)), new TypeReference<TreeMap<String, MyPair>>() {});

        char c = 'a';
        List<DirectIndex> tempMap = new ArrayList<DirectIndex>();
        for(Map.Entry entry : directIndex.entrySet()){
            if(Character.toLowerCase(entry.getKey().toString().charAt(0)) == c){
                DirectIndex d = new DirectIndex();
                d.setKey(entry.getKey().toString());
                d.setValue((MyPair) entry.getValue());
                tempMap.add(d);
            } else {

                String filePath = "E:\\RIW-proiect\\src\\DirectIndex\\" + c + "DirectIndex.idc";
                File file = new File(filePath);
                file.createNewFile();
                c = Character.toLowerCase(entry.getKey().toString().charAt(0));
                synchronized (writeLock){
                    List<DirectIndex> objectList;
                    FileReader reader = new FileReader(file);
                    BufferedReader br = new BufferedReader(reader);
                    try {
                        if (br.readLine() != null) {
                            objectList = objectMapper.readValue(new File(String.valueOf(filePath)), new TypeReference<ArrayList<DirectIndex>>() {
                            });
                        } else {
                            objectList = new ArrayList<>();
                        }

                        for (DirectIndex directIndex1 : tempMap) {
                            objectList.add(directIndex1);
                        }

                        FileOutputStream outputStream = new FileOutputStream(file);

                        objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputStream, objectList);
                        outputStream.close();
                    }catch (JsonParseException jsonParseException) {
                    } catch (JsonMappingException j){

                    } finally {
                        reader.close();
                        br.close();
                    }

                }
                tempMap.clear();
            }
        }

    }

    public long countWords(Path path) throws IOException {
        long count = 0;
        Map<String, MyPair> directIndex = objectMapper.readValue(new File(String.valueOf(path)), new TypeReference<TreeMap<String, MyPair>>() {});

        for(Map.Entry entry : directIndex.entrySet()){
            MyPair pair = (MyPair)(entry.getValue());
            count += pair.getValue();
        }
        return count;
    }
    @Override
    public void run() {

        switch (this.phase){
            case "PHASE_ONE":
                try {
                    long startTime = System.currentTimeMillis();
                    mapPhaseOne(this.path);
                    long endTime   = System.currentTimeMillis();
                    System.out.println("Thread: " + Thread.currentThread().getId() + " time = " + (endTime - startTime));

                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case "PHASE_TWO":
                try {
                    mapPhaseTwo(this.path);
//                    System.out.println("Thread: " + Thread.currentThread().getId() + path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "COUNT_WORDS":
                try{
                    long count = countWords(this.path);
                    totalNumberOfWords.put(path.toString(), count);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}

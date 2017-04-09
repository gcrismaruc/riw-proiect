package p1;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Gheorghe on 4/2/2017.
 */
public class Mapper implements Runnable {

    private static final JsonFactory jsonFactory = new JsonFactory();
    //    private final Object writeLock = new Object();
    public static Map<String, Long> totalNumberOfWords = new ConcurrentHashMap<>();

    private static final Gson gson = new Gson();


    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private Path path;

    public static String PHASE_ONE = "PHASE_ONE";
    public static String PHASE_TWO = "PHASE_TWO";
    public static String COUNT_WORDS = "COUNT_WORDS";
    public static String PHASE_TWO_TEST = "PHASE_TWO_TEST";

    private String phase;

    public Mapper() {

    }

    public Mapper(Path path, String phase) {
        this.path = path;
        this.phase = phase;

//        objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
    }

    /**
        Imparte fisierul in cuvinte;
        Pentru fiecare cuvant numara aparitiile acestuia si creaza un obiect de tipul MyPair
     ce va contine numele fisierului si numarul de apratii pentru cuvantul curent;
        La final, rezultatul este scris intr-un fisier de forma nume_fisier_curent.idc
     */
    public String mapPhaseOne(Path path) throws IOException {
        System.out.println(PHASE_ONE + " " + Thread.currentThread().getId() + " " + path.toString());

        String fileName = String.valueOf(path.getFileName());
        fileName.replace(".txt", ".idc");
        String text = new String(Files.readAllBytes(path));

        Map<String, MyPair> directIndex = TextParser.getParsedWords(text, path.toString());
        File indexDirectFile = new File(String.valueOf(path).replace(".txt", ".idc"));

        this.objectMapper.writerWithDefaultPrettyPrinter().writeValue(indexDirectFile, directIndex);

        return path.toString().replace(".txt", ".idc");
    }

    /**
     * Creaza fisiere pentru indexul direct de forma <litera>DirectIndex.idc
     * ex: aDirectIndex.idc
     * @param path calea catre fisierul de mapat
     * @throws IOException
     */
    @Deprecated
    public void mapPhaseTwo(Path path) throws IOException {

        try {
            writeLock.lock();

            Map<String, MyPair> directIndex = objectMapper.readValue(new File(String.valueOf(path)), new TypeReference<TreeMap<String, MyPair>>() {
            });

            System.out.println(PHASE_TWO + "  " + Thread.currentThread().getId() + "   " + path.toString());
            char c = 'a';
            List<DirectIndex> tempMap = new ArrayList<DirectIndex>();

            for (Map.Entry entry : directIndex.entrySet()) {
                if (!entry.getKey().equals("")) {
                    if (Character.toLowerCase(entry.getKey().toString().charAt(0)) != c) {
                        printToCharFile(c, tempMap);
                        c = Character.toLowerCase(entry.getKey().toString().charAt(0));
                        tempMap.clear();

                        DirectIndex d = new DirectIndex();
                        d.setKey(entry.getKey().toString());
                        d.setValue((MyPair) entry.getValue());
                        tempMap.add(d);
                    } else {
                        DirectIndex d = new DirectIndex();
                        d.setKey(entry.getKey().toString());
                        d.setValue((MyPair) entry.getValue());
                        tempMap.add(d);
                    }
                }
            }

            if (!tempMap.isEmpty() && c <= 'z') {
                printToCharFile(c, tempMap);
            }
        } catch (JsonMappingException e) {

        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Scrie in fisier, indexul direct pentru toate cuvintele ce incep cu o anumita litera
     * @param c caracterul cu care va incepe numele fisierului
     * @param tempMap
     * @throws IOException
     */
    private void printToCharFile(char c, List<DirectIndex> tempMap) throws IOException {

        String filePath = Constants.PATH_TO_DIRECT_INDEX_DIRECTORY + c + "DirectIndex.idc";
        File file = new File(filePath);
        List<DirectIndex> objectList;
        if (file.exists()) {
            objectList = objectMapper.readValue(new File(String.valueOf(filePath)), new TypeReference<ArrayList<DirectIndex>>() {
            });
        } else {
            objectList = new ArrayList<>();
        }

        for (DirectIndex directIndex1 : tempMap) {
            objectList.add(directIndex1);
        }

        readLock.lock();
        FileOutputStream outputStream = new FileOutputStream(file);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputStream, objectList);
        outputStream.close();
        readLock.unlock();
    }

    /**
     * Creaza, pentru fiecare fisier de tipul numeFisier.idc, un set de fisiere de forma <litera>numeFisier.idc
     * si sunt scrise in directorul temp, urmand a fi reduse ulterior
     * @param path calea catre fisierul de mapat
     * @throws IOException
     */
    public void mapPhaseTwoTest(Path path) throws IOException {

        Map<String, MyPair> directIndex = objectMapper.readValue(new File(String.valueOf(path)), new TypeReference<TreeMap<String, MyPair>>() {
        });

        System.out.println(PHASE_TWO_TEST + "  " + Thread.currentThread().getId() + "   " + path.toString());
        char c = 'a';
        List<DirectIndex> tempMap = new ArrayList<DirectIndex>();

        for (Map.Entry entry : directIndex.entrySet()) {
            if (!entry.getKey().equals("")) {
                if (Character.toLowerCase(entry.getKey().toString().charAt(0)) != c) {
                    String fileName = path.getFileName().toString().replaceFirst("[.][^.]+$", "");
                    String filePath = Constants.PATH_TO_TEMP_DIR + c + fileName + "_DirectIndex.idc";

                    objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), tempMap);
                    c = Character.toLowerCase(entry.getKey().toString().charAt(0));
                    tempMap.clear();

                    DirectIndex d = new DirectIndex();
                    d.setKey(entry.getKey().toString());
                    d.setValue((MyPair) entry.getValue());
                    tempMap.add(d);
                } else {
                    DirectIndex d = new DirectIndex();
                    d.setKey(entry.getKey().toString());
                    d.setValue((MyPair) entry.getValue());
                    tempMap.add(d);
                }
            }
        }

        if (!tempMap.isEmpty() && c <= 'z') {
            String fileName = path.getFileName().toString().replaceFirst("[.][^.]+$", "");
            String filePath = Constants.PATH_TO_TEMP_DIR + c + fileName + "_DirectIndex.idc";
            File file = new File(filePath);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), tempMap);
        }

    }


    /**
     * Numara toate cuvintele dintr-un document specificat
     * @param path calea catre fisierul pentru care se numara cuvintele
     * @return numarul total de cuvinte din fisiere
     * @throws IOException
     */
    public long countWords(Path path) throws IOException {
        long count = 0;
        System.out.println(COUNT_WORDS + " " + Thread.currentThread().getId() + " " + path.toString());
        Map<String, MyPair> directIndex = objectMapper.readValue(new File(String.valueOf(path)), new TypeReference<TreeMap<String, MyPair>>() {
        });

        for (Map.Entry entry : directIndex.entrySet()) {
            MyPair pair = (MyPair) (entry.getValue());
            count += pair.getValue();
        }
        return count;
    }


    @Override
    public void run() {

        switch (this.phase) {
            case "PHASE_ONE":
                try {
                    mapPhaseOne(this.path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case "PHASE_TWO":
                try {
                    mapPhaseTwo(this.path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "COUNT_WORDS":
                try {
                    long count = countWords(this.path);
                    totalNumberOfWords.put(path.toString(), count);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "PHASE_TWO_TEST":
                try {
                    mapPhaseTwoTest(this.path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}

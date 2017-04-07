package tfidf;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import p1.Constants;
import p1.DirectIndex;
import p1.MyPair;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Gheorghe on 4/6/2017.
 */
public class TF implements Runnable{

    public static Map<String, Integer> countedWords;
    public static ObjectMapper objectMapper;

    private File file;

    public TF(){
        objectMapper = new ObjectMapper();
        File words  = new File(Constants.COUNTED_WORDS_PATH_FILE);

        try {
            countedWords = objectMapper.readValue(words, new TypeReference<TreeMap<String, Integer>>() {});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public TF(File file){
        this.file = file;
        objectMapper = new ObjectMapper();
        File words  = new File(Constants.COUNTED_WORDS_PATH_FILE);

        try {
            countedWords = objectMapper.readValue(words, new TypeReference<TreeMap<String, Integer>>() {});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void calculateTF(File file) throws IOException {
        Map<String, DirectIndex> map = objectMapper.readValue(file, new TypeReference<TreeMap<String, MyPair>>() {});
        Map<String, Double> tf = new TreeMap<>();
        int nrTotalCuvinte = countedWords.get(file.getAbsolutePath());

        for(Map.Entry entry : map.entrySet()) {
            int nrAp = ((MyPair)entry.getValue()).getValue();
            double tfForWord =(double)nrAp / nrTotalCuvinte;
            tf.put((String) entry.getKey(), tfForWord);
        }

        String path = Constants.PATH_TO_TF + file.getName().replace(".idc", ".tf");
        File f = new File(path);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(f, tf);
    }


    @Override
    public void run() {
        System.out.println("CALCULATE TF " + Thread.currentThread().getId() + " " + file.getName());
        try {
            calculateTF(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

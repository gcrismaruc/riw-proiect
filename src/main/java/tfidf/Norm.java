package tfidf;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.util.Pair;
import p1.Constants;
import p1.ObjectList;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by Gheorghe on 4/7/2017.
 */
public class Norm implements  Runnable{

    ObjectMapper objectMapper = new ObjectMapper();

    private File file;

    public Norm(){}

    public Norm(File file){
        this.file = file;
    }

    public void calculateNormsForFile(File file) throws IOException {
        String pathToNewFile = Constants.PATH_TO_NORM + file.getName().replace(".tf", ".norm");
        String pathToIdf = Constants.PATH_TO_IDF;

        char c = 'a';
        double wordIdf;
        double wordTf;
        double sum = 0.0;

        Map<String, Double> tf = objectMapper.readValue(file, new TypeReference<TreeMap<String, Double>>() {});
        Set<String> words = tf.keySet();
        pathToIdf += c + "IDF.idf";
        Map<String, Double> idf = objectMapper.readValue(new File(pathToIdf), new TypeReference<TreeMap<String, Double>>() {
        });

        for (String word : words) {
            if (word != "") {
                if (word.charAt(0) != c && c >= 'a' && c <= 'z') {
                    char prC = c;
                    c = word.charAt(0);
                    pathToIdf = pathToIdf.replace(prC + "IDF", c + "IDF");
                    idf = objectMapper.readValue(new File(pathToIdf), new TypeReference<TreeMap<String, Double>>() {
                    });

                    if(idf.containsKey(word)) {
                        wordIdf = idf.get(word);
                        wordTf = tf.get(word);

                        sum += (wordIdf * wordTf) * (wordIdf * wordTf);
                    } else {
                        System.out.println(word + "  " + file.getName());
                    }
                } else {
                    if(idf.containsKey(word)) {
                        wordIdf = idf.get(word);
                        wordTf = tf.get(word);

                        sum += (wordIdf * wordTf) * (wordIdf * wordTf);
                    } else {
                        System.out.println(word + "  " + file.getName());
                    }
                }
            }
            Map<String, Double> map = new HashMap<>();
            map.put(file.getName().replace(".tf", ".txt"), Math.sqrt(sum));
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(pathToNewFile), map);
        }
    }

    @Override
    public void run() {
        System.out.println("CALCULATE NORM " + Thread.currentThread().getId() + " " + file.getName());
        try {
            calculateNormsForFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

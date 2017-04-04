package search;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.util.Pair;
import p1.FileLoader;
import p1.MyPair;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Gheorghe on 4/4/2017.
 */
public class TFIDF {

    public Map<String, List<MyPair>> invertedIndexForQuerryWords;
    private Map<String, Double> idfMap;
    private Set<String> directIndexFiles;
//    private Map
    private String querry;

    public TFIDF(String querry){
        invertedIndexForQuerryWords = new HashMap<>();
        idfMap = new HashMap<>();
        directIndexFiles = new HashSet<>();
        this.querry = querry;
    }

    public TFIDF(){
        this.invertedIndexForQuerryWords = new HashMap<>();
    }

    private void getDirectIndexFiles() {
        for (List<MyPair> myPairs : invertedIndexForQuerryWords.values()) {
            if (myPairs != null) {
                directIndexFiles.addAll(myPairs.stream().map(pair -> pair.getKey()).collect(Collectors.toSet()));
            }
        }

//        directIndexFiles = invertedIndexForQuerryWords.values().stream().forEach(myPairs ->
//         myPairs.stream().map(pair -> pair.getKey()).collect(Collectors.toSet()));
    }

    public void booleanSearch() throws IOException {
        Queue<String> queue = QuerryParser.parse(this.querry);
        ObjectMapper objectMapper = new ObjectMapper();

        List<File> files = FileLoader.getFiles("InverseIndex", ".ii");

        while(queue.size() != 0){
            String word = queue.poll();
            char c = word.charAt(0);
            File file = getFileOnChar(c, files);
            Map<String, List<MyPair>> stringListMap = objectMapper.readValue(file, new TypeReference<HashMap<String, ArrayList<MyPair>>>(){});
            invertedIndexForQuerryWords.put(word, stringListMap.get(word));
        }

        System.out.println(invertedIndexForQuerryWords);
//        System.out.println(directIndexFiles);
    }

    public void calculateIDF(){
        Queue<String> queue = QuerryParser.parse(this.querry);

        getDirectIndexFiles();

        while(queue.size() != 0){
            String word = queue.poll();
            if(invertedIndexForQuerryWords.get(word) != null) {
                double idf = Math.log(directIndexFiles.size() / invertedIndexForQuerryWords.get(word).size());
                idfMap.put(word, idf);
            } else {
                idfMap.put(word, 0.0);
            }
        }

        System.out.println(idfMap);
    }

    private File getFileOnChar(char c, List<File> files){

        for(File file : files){
            if(file.getName().charAt(0) == c)
                return file;
        }
        return null;
    }

}

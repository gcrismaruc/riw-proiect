package tfidf;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.util.Pair;
import p1.Constants;
import p1.FileLoader;
import p1.MyPair;
import search.QuerryParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Gheorghe on 4/4/2017.
 */
public class TFIDF {

    public Map<String, List<MyPair>> invertedIndexForQuerryWords;
    private Map<String, Double> idfMap;
    private Map<String, Integer> totalNumberOfWords;
    private Set<String> directIndexFiles;
    private Map<String, List<Pair<String, Double>>> docTF;
    private ObjectMapper objectMapper = new ObjectMapper();




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

        List<File> files = FileLoader.getFilesForInternalPath("InverseIndex", ".ii");

        while(queue.size() != 0){
            String word = queue.poll();
            char c = word.charAt(0);
            File file = getFileOnChar(Character.toLowerCase(c), files);
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
                double idf = 1.0 + Math.log(directIndexFiles.size() / invertedIndexForQuerryWords.get(word).size());
                idfMap.put(word, idf);
            } else {
                idfMap.put(word, 0.0);
            }
        }

        System.out.println(idfMap);
    }

    @Deprecated
    public void calculateTF() throws IOException {
        File file = new File("\\RIW-proiect\\src\\main\\resources\\CountWords.txt");

        totalNumberOfWords = objectMapper.readValue(file, new TypeReference<HashMap<String, Integer>>() {});

        this.calculateIDF();

        docTF  = new HashMap<>();

//        for(Map.Entry entry : invertedIndexForQuerryWords.entrySet()){
//            List<MyPair> myPairs = (List<MyPair>) entry.getValue();
//            if(myPairs != null) {
//                for (MyPair pair : myPairs) {
//
//                    double tf = ((double) pair.getValue()) / totalNumberOfWords.get(pair.getKey().replace(".txt", ".idc"));
//                    double val = tf * idfMap.get(entry.getKey());
//
//                    Pair wordTF = new Pair(entry.getKey(), val);
//                    if (docTF.containsKey(pair.getKey())) {
//                        Set<Pair<String, Double>> pairs = docTF.get(pair.getKey());
//                        pairs.add(wordTF);
//                        docTF.put(pair.getKey(), pairs);
//                    } else {
//                        Set<Pair<String, Double>> pairs = new TreeSet<>();
//                        pairs.add(wordTF);
//                        docTF.put(pair.getKey(), pairs);
//                    }
//                }
//            }
//        }
//
//        Set<String> words = invertedIndexForQuerryWords.keySet();
//
//        for(Map.Entry entry : docTF.entrySet()){
//            Set<Pair<String, Double>> set = (Set<Pair<String, Double>>) entry.getValue();
//
//            for(String word : words){
//                if(set.stream().map(p->p.getKey().equals(word)).count() != 1){
//                    set.add(new Pair<>(word, 0.0));
//                }
//            }
//
//            docTF.put((String) entry.getKey(), set);
//        }

        Set<String> words = invertedIndexForQuerryWords.keySet();

        for(String doc : directIndexFiles){
            List<Pair<String, Double>> pairs = new ArrayList<>();

            for(String word : words) {
                List<MyPair> myPairs = invertedIndexForQuerryWords.get(word);
                if (myPairs != null) {
                    for (MyPair pair : myPairs) {
                        double tf, val;
                        if (pair.getKey().equals(doc)) {
                            tf = ((double) pair.getValue()) / totalNumberOfWords.get(pair.getKey().replace(".txt", ".idc"));
                            val = tf * idfMap.get(word);

                            List<Pair<String, Double>> l = new ArrayList<>(pairs);
                            for(Pair p : l){
                                if(p.getKey().equals(word))
                                    pairs.remove(p);
                            }

                            pairs.add(new Pair<>(word, val));
                        } else {
                            if(pairs.stream().filter(p->p.getKey().equals(word)).count() == 0){
                                pairs.add(new Pair<>(word, 0.0));
                            }
                        }
                    }
                }
            }
            docTF.put(doc, pairs);
        }

        for(Map.Entry entry : docTF.entrySet()){
            System.out.println(entry);
        }

    }

    public Set<String> calculateDistance() throws IOException {
        booleanSearch();

        Set<String> words = invertedIndexForQuerryWords.keySet();
        Map<String, Double> tfidfQuerry = new TreeMap<>();

        //load idf
        char c='a';
        char prC = 'v';
        for (String word : words){
            c = word.charAt(0);
            if(c != prC) {
                String path = Constants.PATH_TO_IDF + c + "IDF.idf";
                prC = c;
                idfMap.putAll(objectMapper.readValue(new File(path), new TypeReference<TreeMap<String, Double>>(){}));
            }
        }

        Map<String, Double> distanceMap = new HashMap<>();
        double sum = 0.0;
        //calculate norm for query
        for(String word:words){
            double idf = idfMap.get(word);
            double tf = 1.0/words.size();
            tfidfQuerry.put(word, tf*idf);

            sum += (tf*idf)*(tf*idf);
        }

        double normQuerry = Math.sqrt(sum);

        //get all docs after boolean search
//        Set<List<MyPair>> values = (Set<List<MyPair>>) invertedIndexForQuerryWords;

        Set<String> docs = new TreeSet<>();
        for(List<MyPair> list : invertedIndexForQuerryWords.values()){
            docs.addAll(list.stream().map(p->p.getKey()).collect(Collectors.toSet()));
        }

        List<File> documents = FileLoader.getFilesForInternalPath("Norms", ".norm");

        Map<String, Double> norms = new HashMap<>();
        for(File file : documents) {
            norms.putAll(objectMapper.readValue(file, new TypeReference<TreeMap<String, Double>>(){}));
        }

        documents = FileLoader.getFilesForInternalPath("TF", ".tf");
        for(String doc : docs) {

            String fileName = Paths.get(doc).getFileName().toString();

            String filePath = Constants.PATH_TO_TF + fileName.replaceFirst("[.][^.]+$", ".tf");;

            Map<String, Double> tf = objectMapper.readValue(new File(filePath), new TypeReference<TreeMap<String, Double>>(){});
            double wordTF, wordIDF;
            double vectorialProduct = 0.0;
            double tfidfForQueryWord;

            for(String word : words) {
                if (tf.containsKey(word)) {
                    wordTF = tf.get(word);
                    wordIDF = idfMap.get(word);
                    tfidfForQueryWord = tfidfQuerry.get(word);

                    vectorialProduct += tfidfForQueryWord * wordIDF * wordTF;
                }
            }

            double normProduct = normQuerry * norms.get(Paths.get(doc).getFileName().toString());

            distanceMap.put(doc, vectorialProduct / normProduct);
        }

        //sort distance map
        distanceMap = distanceMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));

        System.out.println("Cos distance:");
        for(Map.Entry entry : distanceMap.entrySet()){
            System.out.println(entry);
        }

        return distanceMap.keySet();
    }

    private File getFileOnChar(char c, List<File> files){

        for(File file : files){
            if(file.getName().charAt(0) == c)
                return file;
        }
        return null;
    }


}

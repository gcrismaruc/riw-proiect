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
    private Map<String, Integer> totalNumberOfWords;
    private Set<String> directIndexFiles;
    private Map<String, List<Pair<String, Double>>> docTF;

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

    public void calculateTF() throws IOException {
        File file = new File("E:\\RIW-proiect\\src\\main\\resources\\CountWords.txt");
        ObjectMapper objectMapper = new ObjectMapper();

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
        calculateTF();

        Set<String> words = invertedIndexForQuerryWords.keySet();
        List<Pair<String, Double>> tfidfQuerry = new ArrayList<>();

        Map<String, Double> distanceMap = new HashMap<>();
        double sum = 0.0;
        for(String word:words){
            double idf = idfMap.get(word);
            double tf = 1.0/words.size();
            tfidfQuerry.add(new Pair<>(word, tf*idf));

            sum += (tf*idf)*(tf*idf);
        }

        double normQuerry = Math.sqrt(sum);

        for(Map.Entry entry : docTF.entrySet()){
            List<Pair<String, Double>> list = (List<Pair<String, Double>>) entry.getValue();

            double sumDoc =0.0;
            double normDoc;
            double vecProd = 0.0;

            for(int i = 0; i < list.size(); i++){
                Pair<String, Double> p1 = list.get(i);
                Pair<String, Double> p2 = tfidfQuerry.get(i);

                sumDoc += p1.getValue()*p1.getValue();
                vecProd += p1.getValue()*p2.getValue();
            }

            normDoc = Math.sqrt(sumDoc);

            double cosDistance = vecProd/(normDoc * normQuerry);

            distanceMap.put((String) entry.getKey(), cosDistance);
        }

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

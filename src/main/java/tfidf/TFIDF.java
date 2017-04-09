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
    }

    /**
     * Realizeaza boolean search pentru cuvintele dintr-o interogare
     * @throws IOException
     */
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
    }

    /**
     * calculeaza distanta cosinus intre o interogare si setul de documente
     * rezultat in urma booleanSerach
     * @return
     * @throws IOException
     */
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
        //calculez norma interogarii
        for(String word:words){
            double idf, tf;
            if(idfMap.containsKey(word)) {
                idf = idfMap.get(word);
                tf = 1.0 / words.size();
                tfidfQuerry.put(word, tf * idf);

                sum += (tf * idf) * (tf * idf);
            } else {
                sum += 0.0;
            }
        }

        double normQuerry = Math.sqrt(sum);

        //iau toate documentele rezultate din boolean search
        Set<String> docs = new TreeSet<>();
        for(List<MyPair> list : invertedIndexForQuerryWords.values()) {
            if (list != null) {
                docs.addAll(list.stream().map(p -> p.getKey()).collect(Collectors.toSet()));
            }
        }

        List<File> documents = FileLoader.getFilesForInternalPath("Norms", ".norm");

        Map<String, Double> norms = new HashMap<>();
        for(File file : documents) {
            norms.putAll(objectMapper.readValue(file, new TypeReference<TreeMap<String, Double>>(){}));
        }

        //pentru fiecare document din boolean search calculez distanta cosinus
        for(String doc : docs) {

            String fileName = Paths.get(doc).getFileName().toString();

            String filePath = Constants.PATH_TO_TF + fileName.replaceFirst("[.][^.]+$", ".tf");

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

        //sortare distance map
        distanceMap = distanceMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));

//        System.out.println("Cos distance:");
//        for(Map.Entry entry : distanceMap.entrySet()){
//            System.out.println(entry);
//        }

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

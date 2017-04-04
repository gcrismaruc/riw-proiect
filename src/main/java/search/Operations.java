package search;
import p1.MyPair;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Gheorghe on 3/9/2017.
 */
public class Operations {

    //TODO change map name
    private Map<String, List<MyPair>> mappedWords;

    public Operations() throws IOException {
//        this.mappedWords = Indexer.getInvertedIndex();
    }

    public Map<String, List<MyPair>> getMappedWords() {
        return mappedWords;
    }

    public void setMappedWords(Map<String, List<MyPair>> mappedWords) {
        this.mappedWords = mappedWords;
    }

    public Set<String> doUnion(String cuv1, String cuv2){
        Set<String> set = new HashSet<>();
        if(mappedWords.containsKey(cuv1)) {
            set.addAll(mappedWords.get(cuv1).stream().map(p -> p.getKey()).collect(Collectors.toSet()));
        }

        if(mappedWords.containsKey(cuv2)){
            set.addAll(mappedWords.get(cuv2).stream().map(p -> p.getKey()).collect(Collectors.toSet()));
        }

        return set;
    }

    public Set<String> doIntersection(String cuv1, String cuv2){

        if(mappedWords.containsKey(cuv1)) {
            Set<String> set1 = mappedWords.get(cuv1).stream().map(p -> p.getKey()).collect(Collectors.toSet());

            if(mappedWords.containsKey(cuv2)) {
                Set<String> set2 = mappedWords.get(cuv2).stream().map(p -> p.getKey()).collect(Collectors.toSet());
                return set1.stream().filter(f -> set2.contains(f)).collect(Collectors.toSet());
            }
            return set1;
        }
       return new HashSet<>();
    }

    public Set<String> doDifference(String cuv1, String cuv2){
        if(mappedWords.containsKey(cuv1)) {
            Set<String> set1 = mappedWords.get(cuv1).stream().map(p -> p.getKey()).collect(Collectors.toSet());

            if(mappedWords.containsKey(cuv2)) {
                Set<String> set2 = mappedWords.get(cuv2).stream().map(p -> p.getKey()).collect(Collectors.toSet());
                return set1.stream().filter(f -> !set2.contains(f)).collect(Collectors.toSet());
            }
            return set1;
        }
        return new HashSet<>();
    }

    public Set<String> doUnion(Set<String> set1, Set<String> set2){
        Set<String> set = new HashSet<>();
        set.addAll(set1);
        set.addAll(set2);

        return set;
    }

    public List<String> doIntersection(Set<String> set1, Set<String> set2){
        return set1.stream().filter(f -> set2.contains(f)).collect(Collectors.toList());
    }

    public List<String> doDifference(Set<String> set1, Set<String> set2){
        return set1.stream().filter(f -> !set2.contains(f)).collect(Collectors.toList());
    }

    public Set<String> doUnion(Set<String> set1, String cuv){
        Set<String> set = new HashSet<>();
        set.addAll(set1);
        if(mappedWords.containsKey(cuv)) {
            set.addAll(mappedWords.get(cuv).stream().map(p -> p.getKey()).collect(Collectors.toSet()));
        }

        return set;
    }

    public Set<String> doIntersection(Set<String> set1, String cuv) {
        if (mappedWords.containsKey(cuv)) {
            Set<String> set2 = mappedWords.get(cuv).stream().map(p -> p.getKey()).collect(Collectors.toSet());
            return set1.stream().filter(f -> set2.contains(f)).collect(Collectors.toSet());
        } else {
            return set1;
        }
    }

    public Set<String> doDifference(Set<String> set1, String cuv) {
        if (mappedWords.containsKey(cuv)) {
            Set<String> set2 = mappedWords.get(cuv).stream().map(p -> p.getKey()).collect(Collectors.toSet());
            return set1.stream().filter(f -> !set2.contains(f)).collect(Collectors.toSet());
        } else {
            return set1;
        }
    }

    public Set<String> getSetForKey(String key) {
        if (mappedWords.containsKey(key)) {
            return mappedWords.get(key).stream().map(p -> p.getKey()).collect(Collectors.toSet());
        } else {
            return new HashSet<>();
        }
    }
}

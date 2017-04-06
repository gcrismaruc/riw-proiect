package search;

import p1.Porter;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Created by Gheorghe on 3/13/2017.
 */
public class QuerryParser {

//    private List<String> querryWords;

    public QuerryParser(){
//        this.querryWords = new ArrayList<>();
    }

    public static Queue<String> parse(String querry){
        StringBuilder word = new StringBuilder();
        Queue<String> querryWords = new ArrayDeque<>();
        Porter porter = new Porter();

        for(int i = 0; i < querry.length(); i++) {
            if (!Character.isAlphabetic(querry.charAt(i)) && querry.charAt(i) != '+' && querry.charAt(i) != '-') {
                String canonicalForm = porter.stripAffixes(word.toString());
                querryWords.add(canonicalForm);
                word = word.delete(0, word.length());
            } else {
                word.append(querry.charAt(i));
            }
        }

        String canonicalForm = porter.stripAffixes(word.toString());
        querryWords.add(canonicalForm);
        return querryWords;
    }
}

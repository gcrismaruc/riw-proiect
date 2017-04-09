package p1;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gheorghe on 3/1/2017.
 */
//clasa care mapeaza exceptiile din fisierul /resources/exceptionWords
public class ExceptionWords {

    public static List<String> exceptionWords;

    public ExceptionWords(){
        this.exceptionWords = new ArrayList<>();
    }

    public ExceptionWords(List<String> exceptionWords){
        this.exceptionWords = new ArrayList<>(exceptionWords);
    }

    public List<String> getExceptionWords() {
        return exceptionWords;
    }

    public static void loadExceptionWords(){
//        String data = new String(Files.readAllBytes(FileLoader.getResourceFile("stopWords").toPath()), StandardCharsets.UTF_8);
//
//        textParser.getWords(data);
//        StopWords.stopWords = new ArrayList<>(textParser.getAparitii().keySet());

        ExceptionWords.exceptionWords = new ArrayList<>();
    }

}

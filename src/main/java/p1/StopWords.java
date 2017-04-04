package p1;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gheorghe on 3/1/2017.
 */
public class StopWords {

    public static List<String> stopWords;

    public StopWords(){
        this.stopWords = new ArrayList<>();
    }

    public StopWords(List<String> stopWords){
        this.stopWords = new ArrayList<>(stopWords);
    }

    public List<String> getStopWords() {
        return stopWords;
    }

    public static void loadStopWords() throws IOException {
        String data = new String(Files.readAllBytes(FileLoader.getResourceFile("stopWords").toPath()), StandardCharsets.UTF_8);

        TextParser textParser = new TextParser();
        textParser.getWords(data);
        StopWords.stopWords = new ArrayList<>(textParser.getAparitii().keySet());
    }
}

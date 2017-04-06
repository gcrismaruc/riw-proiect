import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import p1.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import search.TFIDF;

/**
 * Created by Gheorghe on 4/2/2017.
 */
public class RunMain {
    public static int NO_THREADS = 8;

    public static void DoIndexing() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        StopWords.loadStopWords();
        ExceptionWords.loadExceptionWords();
        List<File> files = FileLoader.getFiles("books", ".txt");

        ExecutorService executorService = Executors.newFixedThreadPool(NO_THREADS);

        for(File file : files){
            executorService.execute(new Mapper(Paths.get(file.getAbsolutePath()), Mapper.PHASE_ONE));
        }

        System.out.println("Mapper phase one done!");


        //after calculated direct index for each file calculate total number of words;
        FileUtils.cleanDirectory(new File("E:\\RIW-proiect\\src\\DirectIndex"));
        for(File file : files) {
            executorService.execute(new Mapper(Paths.get(file.getAbsolutePath().replace(".txt", ".idc")), Mapper.PHASE_TWO));
        }

        for(File file : files) {
            executorService.execute(new Mapper(Paths.get(file.getAbsolutePath().replace(".txt", ".idc")), Mapper.COUNT_WORDS));
        }
        executorService.shutdown();

        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        File file = new File("E:\\RIW-proiect\\src\\main\\resources\\CountWords.txt");
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, Mapper.totalNumberOfWords);

        FileUtils.cleanDirectory(new File("E:\\RIW-proiect\\src\\InverseIndex"));

        executorService = Executors.newFixedThreadPool(NO_THREADS);
        files = FileLoader.getFiles("DirectIndex", ".idc");

        for(File file1 : files) {
            executorService.execute(new Reducer(Paths.get(file1.getAbsolutePath()), Reducer.PHASE_TWO));
        }

        executorService.shutdown();
    }

    public static void main(String[] args) throws IOException {


        DoIndexing();

//        TFIDF tfidf = new TFIDF("text JEPHRO zip");
//
//        Set<String> set = tfidf.calculateDistance();
//
//        for(String value : set){
//            System.out.println(value);
//        }
    }
}

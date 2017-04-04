import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import p1.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import search.TFIDF;

/**
 * Created by Gheorghe on 4/2/2017.
 */
public class RunMain {

    public static void main(String[] args) throws IOException {
        StopWords.loadStopWords();
        ExceptionWords.loadExceptionWords();
        List<File> files = FileLoader.getFiles("books", ".txt");

//        long startTime = System.currentTimeMillis();
//        for(File file : files) {
//            new Mapper().map(Paths.get(file.getAbsolutePath().replace(".txt",".idc")));
//        }
//        long endTime   = System.currentTimeMillis();
//        System.out.println("Single threaded: " + (endTime - startTime));

//        FileUtils.cleanDirectory(new File("E:\\RIW-proiect\\src\\DirectIndex"));

//        ExecutorService executorService = Executors.newFixedThreadPool(8);

        //after calculated direct index on each file calculate total number of words;
//        for(File file : files) {
//            executorService.execute(new Mapper(Paths.get(file.getAbsolutePath().replace(".txt", ".idc")), Mapper.COUNT_WORDS));
//        }
//        executorService.shutdown();
//
//        try {
//            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        File file = new File("E:\\RIW-proiect\\src\\main\\resources\\CountWords.txt");
//        Mapper.objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, Mapper.totalNumberOfWords);

//        FileUtils.cleanDirectory(new File("E:\\RIW-proiect\\src\\InverseIndex"));
//
//        executorService = Executors.newFixedThreadPool(8);
//        files = FileLoader.getFiles("DirectIndex", ".idc");
//        for(File file : files) {
//            executorService.execute(new Reducer(Paths.get(file.getAbsolutePath()), Reducer.PHASE_TWO));
//        }
//
//        executorService.shutdown();

        TFIDF tfidf = new TFIDF("text word sa");

        tfidf.booleanSearch();
        tfidf.calculateIDF();

    }
}

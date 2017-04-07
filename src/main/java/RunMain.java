import com.fasterxml.jackson.databind.ObjectMapper;
import p1.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import tfidf.IDF;
import tfidf.Norm;
import tfidf.TF;
import tfidf.TFIDF;

import static tfidf.TF.objectMapper;

/**
 * Created by Gheorghe on 4/2/2017.
 */
public class RunMain {
    public static int NO_THREADS = 10;

    public static void DoIndexing(String pathToFile) throws IOException {
        FileUtils.cleanDirectory(new File(Constants.PATH_TO_TF));
        FileUtils.cleanDirectory(new File(Constants.PATH_TO_IDF));
        FileUtils.cleanDirectory(new File(Constants.PATH_TO_NORM));
        FileUtils.cleanDirectory(new File(Constants.PATH_TO_INVERSE_INDEX_DIRECTORY));
        FileUtils.cleanDirectory(new File(Constants.PATH_TO_DIRECT_INDEX_DIRECTORY));


        ObjectMapper objectMapper = new ObjectMapper();
        StopWords.loadStopWords();
        ExceptionWords.loadExceptionWords();
        List<File> files = FileLoader.getFilesForDirectoryPath(pathToFile, ".txt");

        //calculate direct index for each file
        ExecutorService executorService = Executors.newFixedThreadPool(NO_THREADS);
        for(File file : files){
            executorService.execute(new Mapper(Paths.get(file.getAbsolutePath()), Mapper.PHASE_ONE));
        }


        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Mapper phase one done!");

        executorService = Executors.newFixedThreadPool(NO_THREADS);
        //count all words in each file
        for(File file : files) {
            executorService.execute(new Mapper(Paths.get(file.getAbsolutePath().replace(".txt", ".idc")), Mapper.COUNT_WORDS));
        }


        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //after calculated direct index for each file calculate total number of words;
        FileUtils.cleanDirectory(new File(Constants.PATH_TO_DIRECT_INDEX_DIRECTORY));
        for(File file : files) {
//            executorService.execute(new Mapper(Paths.get(file.getAbsolutePath().replace(".txt", ".idc")), Mapper.PHASE_TWO));
            new Mapper().mapPhaseTwo(Paths.get(file.getAbsolutePath().replace(".txt", ".idc")));
        }


        File file1 = new File(Constants.COUNTED_WORDS_PATH_FILE);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file1, Mapper.totalNumberOfWords);

        //calculate inverse index
        FileUtils.cleanDirectory(new File(Constants.PATH_TO_INVERSE_INDEX_DIRECTORY));
        executorService = Executors.newFixedThreadPool(NO_THREADS);
        files = FileLoader.getFilesForInternalPath("DirectIndex", ".idc");

        for(File file2 : files) {
            executorService.execute(new Reducer(Paths.get(file2.getAbsolutePath()), Reducer.PHASE_TWO));
        }

        //calculte TF
        files = FileLoader.getFilesForDirectoryPath(pathToFile, ".idc");
        for(File file : files) {
            executorService.execute(new TF(file));
//            new TF().calculateTF(file);
        }

        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //calcul  IDF
         new IDF().calculateIDF(pathToFile);

        executorService = Executors.newFixedThreadPool(NO_THREADS);

        files = FileLoader.getFilesForDirectoryPath(Constants.PATH_TO_TF, ".tf");
        for(File file : files){
            executorService.execute(new Norm(file));
        }

        executorService.shutdown();

    }

    public static void main(String[] args) throws IOException {


        DoIndexing("D:\\de toate\\books");


//        TFIDF tfidf = new TFIDF("arrive");
//
//        Set<String> set = tfidf.calculateDistance();
//
//        System.out.println("-----------------Result set-------------------");
//        System.out.println("");
//        for(String value : set){
//            System.out.println(value);
//        }

    }
}

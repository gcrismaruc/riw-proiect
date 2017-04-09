import com.fasterxml.jackson.databind.ObjectMapper;
import p1.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
        createDirs();

        ObjectMapper objectMapper = new ObjectMapper();
        StopWords.loadStopWords();
        ExceptionWords.loadExceptionWords();
        List<File> files = FileLoader.getFilesForDirectoryPath(pathToFile, ".txt");
        double nrTotalFisiere = files.size();
        
        //calculez indexul direct pentru toate fisierele txt
        ExecutorService executorService = Executors.newFixedThreadPool(NO_THREADS);
        for(File file : files){
            executorService.execute(new Mapper(Paths.get(file.getAbsolutePath()), Mapper.PHASE_ONE));
        }
        stopThreadPool(executorService);

        //split pe litere la fiecare fisier
        executorService = Executors.newFixedThreadPool(NO_THREADS);
        for(File file : files){
            executorService.execute(new Mapper(Paths.get(file.getAbsolutePath().toString().replace(".txt", ".idc")), Mapper.PHASE_TWO_TEST));
        }
        stopThreadPool(executorService);

        executorService = Executors.newFixedThreadPool(NO_THREADS);
        for (char c = 'a'; c <= 'z'; c++){
            executorService.execute(new Reducer(c, Reducer.PHASE_ONE));
        }
        stopThreadPool(executorService);

        executorService = Executors.newFixedThreadPool(NO_THREADS);
        //calculez numarul de cuvinte pentru toate fisierele
        for(File file : files) {
            executorService.execute(new Mapper(Paths.get(file.getAbsolutePath().replace(".txt", ".idc")), Mapper.COUNT_WORDS));
        }
        stopThreadPool(executorService);

        File file1 = new File(Constants.COUNTED_WORDS_PATH_FILE);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file1, Mapper.totalNumberOfWords);

        //calculez indexul invers
        FileUtils.cleanDirectory(new File(Constants.PATH_TO_INVERSE_INDEX_DIRECTORY));
        executorService = Executors.newFixedThreadPool(NO_THREADS);
        files = FileLoader.getFilesForInternalPath("DirectIndex", ".idc");

        for(File file2 : files) {
            executorService.execute(new Reducer(Paths.get(file2.getAbsolutePath()), Reducer.PHASE_TWO));
        }

        //calculez TF
        files = FileLoader.getFilesForDirectoryPath(pathToFile, ".idc");
        for(File file : files) {
            executorService.execute(new TF(file));
        }
        stopThreadPool(executorService);

        //calculez  IDF
        executorService = Executors.newFixedThreadPool(NO_THREADS);
        files = FileLoader.getFilesForInternalPath("InverseIndex", ".ii");
        for(File file : files){
            executorService.execute(new IDF(file, nrTotalFisiere));
        }
        stopThreadPool(executorService);

        //calculez normele
        executorService = Executors.newFixedThreadPool(NO_THREADS);
        files = FileLoader.getFilesForDirectoryPath(Constants.PATH_TO_TF, ".tf");
        for(File file : files){
            executorService.execute(new Norm(file));
        }

        stopThreadPool(executorService);
        FileUtils.cleanDirectory(new File(Constants.PATH_TO_TEMP_DIR));
    }

    /**
     * Creez directoarele necesare daca nu exista
     * Daca exista atunci sterg continutul
     * @throws IOException
     */
    private static void createDirs() throws IOException {
        if(!Files.exists(Paths.get(Constants.PATH_TO_TF))){
            Files.createDirectories(Paths.get(Constants.PATH_TO_TF));
        } else {
            FileUtils.cleanDirectory(new File(Constants.PATH_TO_TF));

        }
        if(!Files.exists(Paths.get(Constants.PATH_TO_IDF))){
            Files.createDirectories(Paths.get(Constants.PATH_TO_IDF));
        } else {
            FileUtils.cleanDirectory(new File(Constants.PATH_TO_IDF));

        }

        if(!Files.exists(Paths.get(Constants.PATH_TO_NORM))){
            Files.createDirectories(Paths.get(Constants.PATH_TO_NORM));
        } else {
            FileUtils.cleanDirectory(new File(Constants.PATH_TO_NORM));

        }
        if(!Files.exists(Paths.get(Constants.PATH_TO_INVERSE_INDEX_DIRECTORY))){
            Files.createDirectories(Paths.get(Constants.PATH_TO_INVERSE_INDEX_DIRECTORY));
        } else {
            FileUtils.cleanDirectory(new File(Constants.PATH_TO_INVERSE_INDEX_DIRECTORY));

        }
        if(!Files.exists(Paths.get(Constants.PATH_TO_DIRECT_INDEX_DIRECTORY))){
            Files.createDirectories(Paths.get(Constants.PATH_TO_DIRECT_INDEX_DIRECTORY));
        } else {
            FileUtils.cleanDirectory(new File(Constants.PATH_TO_DIRECT_INDEX_DIRECTORY));
        }

        if(!Files.exists(Paths.get(Constants.PATH_TO_TEMP_DIR))){
            Files.createDirectories(Paths.get(Constants.PATH_TO_TEMP_DIR));
        } else {
            FileUtils.cleanDirectory(new File(Constants.PATH_TO_TEMP_DIR));
        }
    }

    /**
     * Stop la threadPool si astept sa se termine toate threadurile sa isi termine executia
     * @param executorService
     */
    private static void stopThreadPool(ExecutorService executorService) {
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {

        if(args.length != 0){
            if(args[0].equals("-i") && args.length > 1){
                if(args[1] != null)
                    DoIndexing(args[1]);
                else{
                    System.out.println("Introduceti un path catre directorul cu fisiere de indexat. -h pentru ajutor");
                }
            }

            if(args[0].equals("-s") && args.length > 1){
                if(args[1] != null) {
                    TFIDF tfidf = new TFIDF(args[1]);

                    Set<String> set = tfidf.calculateDistance();

                    System.out.println("-----------------Result set-------------------");
                    System.out.println("");
                    for (String value : set) {
                        System.out.println(value);
                    }
                }
                else{
                    System.out.println("Introduceti introduceti cuvintele pe care doriti sa le cautati. -h pentru ajutor");
                }
            }
            if(args[0].equals("-h")){
                System.out.println("-h Help");
                System.out.println("-i <Calea catre directorul ce contine fisierele de indexat>");
                System.out.println("-s <Interogarea prntru care doriti sa aflati documentele>");
            }
        }
    }
}

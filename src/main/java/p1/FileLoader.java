package p1;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Gheorghe on 2/23/2017.
 */
public class FileLoader {

    private String directoryName;

    public FileLoader(String directoryName){
        this.directoryName = directoryName;
    }

    public FileLoader(){

    }

    //returneaza toate fisierele cu o anumita extensie, dintr-un director din cadrul proiectului
    public static List<File> getFilesForInternalPath(String directoryName, String extension) throws IOException {

        File targetDir = getTargetFile(directoryName);

        List<File> filesInFolder = Files.walk(Paths.get(targetDir.getAbsolutePath()))
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .filter(p -> p.getName().contains(extension))
                .collect(Collectors.toList());
        return filesInFolder;
    }

    //returneaza o lista de fisiere cu o anumita extensie, dintr-un director cu o cale specificata
    public static List<File> getFilesForDirectoryPath(String directoryPath, String extension) throws IOException {
        if(Files.isDirectory(Paths.get(directoryPath.toString()))) {
            List<File> filesInFolder = Files.walk(Paths.get(directoryPath))
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .filter(p -> p.getName().contains(extension))
                    .collect(Collectors.toList());
            return filesInFolder;
        } else {
            System.out.println("Nu este un director valid");
            return null;
        }
    }

    //returneaza o lista de fisiere ce incep cu un caracter specificat din directorul "temp"
    public static List<File> getFilesForChar(char c) throws IOException {
        File targetDir = getTargetFile("temp");

        List<File> filesInFolder = Files.walk(Paths.get(targetDir.getAbsolutePath()))
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .filter(p -> p.getName().charAt(0)==c)
                .filter(p -> p.getName().contains(".idc"))
                .collect(Collectors.toList());

        return filesInFolder;
    }

    public static int getNoFiles(String directoryName, String extension) throws IOException {

        File targetDir = getTargetFile(directoryName);

        List<File> filesInFolder = Files.walk(Paths.get(targetDir.getAbsolutePath()))
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .filter(p -> p.getName().contains(extension))
                .collect(Collectors.toList());
        return filesInFolder.size();
    }

    public static File getTargetFile(String directoryName) throws IOException {
        File currentDir = new File( "." );
        File targetDir = null;
        if (currentDir.isDirectory()) {
            File parentDir = currentDir.getCanonicalFile().getParentFile();
            targetDir = new File("RIW-proiect/working/" + directoryName + "/" );
        }
        return targetDir;
    }

    //returneaza un fisier din directorul de resurse
    public static File getResourceFile(String fileName) throws IOException {
        File currentDir = new File( "." );
        File targetDir = null;
        if (currentDir.isDirectory()) {
            File parentDir = currentDir.getCanonicalFile().getParentFile();
            targetDir = new File(fileName);
        }
        return targetDir;
    }
}

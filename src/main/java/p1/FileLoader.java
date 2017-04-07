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

    public static List<File> getFilesForInternalPath(String directoryName, String extension) throws IOException {

        File targetDir = getTargetFile(directoryName);

        List<File> filesInFolder = Files.walk(Paths.get(targetDir.getAbsolutePath()))
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .filter(p -> p.getName().contains(extension))
                .collect(Collectors.toList());
        return filesInFolder;
    }

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
        File currentDir = new File( "." ); // Read current file location
        File targetDir = null;
        if (currentDir.isDirectory()) {
            File parentDir = currentDir.getCanonicalFile().getParentFile(); // Resolve parent location out fo the real path
            targetDir = new File( parentDir, "RIW-proiect\\working\\" + directoryName + "\\" ); // Construct the target directory file with the right parent directory
        }
        return targetDir;
    }

    public static File getResourceFile(String fileName) throws IOException {
        File currentDir = new File( "." ); // Read current file location
        File targetDir = null;
        if (currentDir.isDirectory()) {
            File parentDir = currentDir.getCanonicalFile().getParentFile(); // Resolve parent location out fo the real path
            targetDir = new File( parentDir, "RIW-proiect\\src\\main\\resources\\" + fileName); // Construct the target directory file with the right parent directory
        }
        return targetDir;
    }
}

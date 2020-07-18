package tempfilemanager;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

public class TemporaryFileManager {
    Set<File> registered = new HashSet<>();
    Set<File> removed = new HashSet<>();

    public File createTemporaryFile(String prefix, String suffix) throws IOException {
        return createTemporaryFile(prefix, suffix, null);
    }

    public File createTemporaryFile(String prefix, String suffix, File directory) throws IOException {
        File result = File.createTempFile(prefix, suffix, directory);
        registered.add(result);
        return result;
    }

    public boolean unregister(File file) {
        return registered.remove(file);
    }

    public boolean remove(File file) {
        if (!registered.contains(file)) {
            throw new AssertionError("removing unregistered file");
        }
        if (removed.add(file) && file.exists()) {
            System.out.println("Removing " + file.getAbsolutePath());
            if (file.isDirectory()) {
                try {
                    FileUtils.deleteDirectory(file);
                } catch (IOException e) {
                    System.err.println("Couldn't remove directory " + file.getAbsolutePath() + ": " + e.getMessage());
                    return false;
                }
            } else {
                if (!file.delete()) {
                    System.err.println("Couldn't remove " + file.getAbsolutePath());
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public void removeAll() {
        registered.forEach(this::remove);
    }

    public File[] filesToRemove() {
        return registered.stream().filter(x -> !removed.contains(x) && x.exists()).toArray(File[]::new);
    }

    public File createTemporaryDirectory(String prefix, File directory) throws IOException {
        File result = (directory == null ?
                Files.createTempDirectory(prefix) :
                Files.createTempDirectory(directory.toPath(), prefix)).toFile();
        registered.add(result);
        return result;
    }

    public File createTemporaryDirectory(String prefix) throws IOException {
        return createTemporaryDirectory(prefix, null);
    }
}

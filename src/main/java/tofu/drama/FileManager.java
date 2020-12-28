package tofu.drama;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.zip.GZIPOutputStream;

public class FileManager {
    public class ManagedFile {
        private final int id;
        private final FileManager manager;

        public ManagedFile(int id, FileManager manager) {
            this.id = id;
            this.manager = manager;
        }

        public void write(String format, Object... arguments) {
            manager.write(id, format, arguments);
        }

        public void flush() {
            manager.flush(id);
        }
    }

    private class FileInfo {
        public Path dir;
        public String coreName;
        public Path path;
        public BufferedWriter file;
        public long maxSize;
    }

    private static final long MAX_FILE_SIZE = 1024 * 1000; // 1M
    private static final DateTimeFormatter FILE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyMMdd.HHmmss");
    private static final DateTimeFormatter LOG_TIME_FORMAT = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss");

    private final String _root;
    private final List<FileInfo> _files;

    public FileManager(String root){
        _root = root;
        _files = new LinkedList<>();
    }

    public ManagedFile manageFile(String folder, String coreName) {
        return manageFile(folder, coreName, MAX_FILE_SIZE);
    }

    public ManagedFile manageFile(String folder, String coreName, long maxSize) {
        FileInfo info = new FileInfo();
        try {
            Path path = Paths.get(_root, folder);

            info.dir = Files.createDirectories(path);
            info.coreName = coreName;
            info.path = path.resolve(coreName + ".log");
            info.maxSize = (maxSize <= 1024) ? MAX_FILE_SIZE : maxSize;
            info.file = Files.newBufferedWriter(info.path, StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } catch (Exception e) {
            Drama.LOGGER.error("Couldn't initialize file:" + coreName, e);
            return null;
        }

        _files.add(info);
        return new ManagedFile(_files.size() - 1, this);
    }

    private void write(int id, String format, Object... arguments) {
        if (id < 0 || id >= _files.size()) {
            Drama.LOGGER.error("FileManager received bad fileId : " + id);
            return;
        }

        FileInfo info = _files.get(id);
        try {
            info.file.write(LocalDateTime.now().format(LOG_TIME_FORMAT));
            info.file.write('\t');
            info.file.write(MessageFormat.format(format, arguments));
            info.file.newLine();
        } catch (Exception e) {
            Drama.LOGGER.warn("Couldn't write to " + info.path);
        }
    }

    private void rollFile(FileInfo info) throws IOException {
        String formattedTime = LocalDateTime.now().format(FILE_TIME_FORMAT);

        info.file.flush();
        info.file.close();
        info.file = null;

        Path target = info.dir.resolve(info.coreName + "_" + formattedTime + ".log");
        Files.move(info.path, target);
        info.file = Files.newBufferedWriter(info.path, StandardCharsets.UTF_8); // create|truncate|write

        // archive the old file in the background
        CompletableFuture.runAsync(() -> archive(target));
    }

    private void archive(Path file) {
        try (GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(file.getParent().resolve(file.getFileName().toString()+".gz").toFile()))){
            try (FileInputStream in = new FileInputStream(file.toFile())){
                byte[] buffer = new byte[1024];
                int len;
                while((len=in.read(buffer)) != -1){
                    out.write(buffer, 0, len);
                }
            }
            Files.delete(file);
        } catch(Exception e){
            Drama.LOGGER.warn("Failed to archive " + file);
        }
    }

    private void flush(int id) {
        if (id < 0 || id >= _files.size()) {
            Drama.LOGGER.warn(MessageFormat.format("Bad file id: {0}", id));
            return;
        }

        FileInfo info = _files.get(id);
        try {
            info.file.flush();
        } catch (Exception e) {
            Drama.LOGGER.error("Failed to close file " + info.path, e);
        }

        try {
            if (Files.size(info.path) > info.maxSize) {
                rollFile(info);
            }
        } catch (Exception e) {
            Drama.LOGGER.error("Failed to archive " + info.path, e);
        }
    }

    public void closeAll() {
        for (FileInfo info : _files) {
            try {
                info.file.flush();
                info.file.close();
            } catch (Exception e) {
                Drama.LOGGER.error("Failed to close " + info.path, e);
            }
        }
    }
}

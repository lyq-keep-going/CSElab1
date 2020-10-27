package interfaces;

import java.io.IOException;

public interface FileManager {
    File getFile(Id fileId) throws IOException;
    File newFile(Id fileId) throws IOException;
}

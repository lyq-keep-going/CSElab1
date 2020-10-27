package interfaces;


import java.io.FileNotFoundException;
import java.io.IOException;

// Block write , immutatable
public interface Block {
    Id getIndexId();
    BlockManager getBlockManager();
    byte[] read() ;
    int blockSize();
}

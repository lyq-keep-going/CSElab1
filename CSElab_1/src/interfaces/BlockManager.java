package interfaces;

import java.io.IOException;

public interface BlockManager {
    Block getBlock(Id indexId) throws IOException;
    Block newBlock(byte[] b) throws IOException;
    default Block newEmptyBlock(int blockSize) throws IOException {
        return newBlock(new byte[blockSize]);
    }
}

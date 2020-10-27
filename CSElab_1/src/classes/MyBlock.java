package classes;

import interfaces.Block;
import interfaces.BlockManager;
import interfaces.Id;

import java.io.*;

public class MyBlock implements Block {
    private Id blockId;
    private MyBlockManager blockManager;
    private int blockSize = PublicVars.BLOCKSIZE;
    private int blockLoad;

    MyBlock(Id blockId, MyBlockManager blockManager,int loadSize) {
        this.blockId = blockId;
        this.blockManager = blockManager;
        this.blockLoad = loadSize;
    }

    @Override
    public Id getIndexId() {
        return  blockId;
    }

    @Override
    public BlockManager getBlockManager() {
        return blockManager;
    }

    @Override
    public byte[] read() throws ErrorCode {
        try {
            File dataFile = new File("BM//bm-" + blockManager.getBlockManagerId().getId() + "//" + blockId.getId() + ".data");
            File metaFile = new File("BM//bm-" + blockManager.getBlockManagerId().getId() + "//" + blockId.getId() + ".meta");

            if(!dataFile.isFile() || !metaFile.isFile()){
                throw new ErrorCode(ErrorCode.BLOCK_DATA_MISSING);
            }

            FileInputStream in = new FileInputStream(dataFile);
            BufferedInputStream bin = new BufferedInputStream(in);

            byte[] res = new byte[blockLoad];
            int count = bin.read(res,0,blockLoad);
            if(count != blockLoad){
                throw new ErrorCode(ErrorCode.INCOMPLETE_READING);
            }

            long checksum = PublicMethods.getCRC32Checksum(res);

            //要从blockmeta中读出校验码
            FileReader reader = new FileReader(metaFile);
            BufferedReader breader = new BufferedReader(reader);
            String line;
            while((line = breader.readLine()) != null){
                if(line.contains("checksum")){
                    long checksum0 = Long.parseLong(line.substring(line.indexOf(":") + 1));
                    if(checksum != checksum0){
                        throw new ErrorCode(ErrorCode.CHECKSUM_CHECK_FAILED);
                    }
                }
            }

            return res;
        }catch (IOException e){
            throw new ErrorCode(ErrorCode.IO_EXCEPTION);
        }


    }

    @Override
    public int blockSize() {
        return blockSize;
    }

    public int getBlockLoad() {
        return blockLoad;
    }
}

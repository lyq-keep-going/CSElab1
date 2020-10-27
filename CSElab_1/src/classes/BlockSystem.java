package classes;

import interfaces.Block;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

//运用单例模式
public class BlockSystem {
    private static MyBlockManager[] blockManagers;
    private static final int BLOCKMANAGERNUM = 10;
    private static BlockSystem single = null;

    private BlockSystem() {
        //初始化
        blockManagers = new MyBlockManager[BLOCKMANAGERNUM];
        for (int i = 0; i < BLOCKMANAGERNUM; i++) {
            blockManagers[i] = new MyBlockManager(new BlockManagerId(i));
        }
    }

    public static BlockSystem getInstance(){
        if(single == null) {
            single = new BlockSystem();
        }

        return single;

    }

    public String[][] allocateContentToBlocks(byte[] content) throws ErrorCode {
        //首先需要弄清楚一共需要多少个block
        int size = content.length;
        int numOfBlocks = 0;
        if (size % PublicVars.BLOCKSIZE == 0) {
            numOfBlocks = size / PublicVars.BLOCKSIZE;
        } else {
            numOfBlocks = size / PublicVars.BLOCKSIZE + 1;
        }
        //每个block维持三个副本
        String[][] res = new String[numOfBlocks][3];
        Random random = new Random();
        int count = 0;//记录已经安置好的字节数
        try {
            for (int i = 0; i < numOfBlocks; i++) {
                for (int j = 0; j < 3; j++) {
                    //随机选择一个blockmanager
                    int bm = random.nextInt(BLOCKMANAGERNUM);
                    if (count + PublicVars.BLOCKSIZE <= size) {
                        byte[] toAllocate = Arrays.copyOfRange(content, count, count + PublicVars.BLOCKSIZE);
                        MyBlock allocatedBlock = (MyBlock) blockManagers[bm].newBlock(toAllocate);
                        res[i][j] = bm + "|" + allocatedBlock.getIndexId().getId();
                    } else {
                        byte[] toAllocate = Arrays.copyOfRange(content, count, size);
                        MyBlock allocatedBlock = (MyBlock) blockManagers[bm].newBlock(toAllocate);
                        res[i][j] = bm + "|" + allocatedBlock.getIndexId().getId();
                    }
                }
                count = Math.min(count + PublicVars.BLOCKSIZE, size);
            }
        }catch (ErrorCode e){
            //由于newblock可能会有异常，对异常进行包装后抛出
            throw (ErrorCode)new ErrorCode(ErrorCode.DATA_ALLOCATION_FAIL).initCause(e);
        }

        return res;
    }

    //如果读取失败会返回null，不会抛出错误
    public byte[] readBlock(int blockManagerId, int blockId, int offset) {
        Block block = blockManagers[blockManagerId].getBlock(new BlockId(blockId));
        byte[] blockData = null;
        try {
            blockData = block.read();
        }catch (ErrorCode e){
            blockData = null;
           //在控制台打印读取block的错误信息
            System.out.println("Warning from Block System:" + ErrorCode.getErrorText(e.getErrorCode()) + " at block:" +blockManagerId + "|" + blockId);
        }

        return blockData == null ? null: Arrays.copyOfRange(blockData,offset,blockData.length);
    }

    public Block getBlock(int blockManagerId, int blockId){
        return blockManagers[blockManagerId].getBlock(new BlockId(blockId));
    }


}

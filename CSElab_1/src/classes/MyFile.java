package classes;

import com.sun.org.apache.regexp.internal.RE;
import interfaces.Block;
import interfaces.File;
import interfaces.FileManager;
import interfaces.Id;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class MyFile implements File {

    private Id fileId;//对应meta文件和文件名
    private MyFileManager fileManager;
    private FileMeta fileMeta;//由MyFile类控制file的meta文件
    private long currPos;//表示当前指的是第几个字节，从1开始
    private static Buffer bufferHead;
    private static Buffer bufferTail;


    public MyFile(FileId fileId, MyFileManager fileManager) throws ErrorCode {
        this.fileId = fileId;
        this.fileManager = fileManager;
        this.currPos = 1;
        this.fileMeta = new FileMeta(fileId,fileManager,0,0,new String[0][0]);
        //初始化buffer
        initBuffer();
    }

    public MyFile(FileId fileId, MyFileManager fileManager, boolean fileAlreadyExisted) throws ErrorCode{
        this.fileId = fileId;
        this.fileManager = fileManager;
        this.currPos = 1;
        this.fileMeta = new FileMeta(fileId,fileManager);
        //初始化buffer
        initBuffer();
    }

    //buffer系列操作--------------------------------------------------------------------------
    private static class Buffer{
        int logicBlockNum;
        byte[] data;
        boolean isDirty;
        int addLength;//在isDirty时要读入，在像block中写入时要修改!!!!!!
        Buffer next;

        public Buffer(int logicBlockNum, byte[] data) {
            this.logicBlockNum = logicBlockNum;
            this.data = data;
            this.isDirty = false;
            this.addLength = 0;
        }

    }

    private void initBuffer(){
        bufferHead = new Buffer(-1, new byte[0]);
        Buffer ptr = bufferHead;
        for(int i = 0; i < 9; i++){
            ptr.next = new Buffer(-1, new byte[0]);
            ptr = ptr.next;
        }
        bufferTail = ptr;
    }

    private void addBlockToBuffer(int logicBlockNum, byte[] data) throws ErrorCode {
        Buffer newBuffer = new Buffer(logicBlockNum, data);
        //将新块插入在头部
        newBuffer.next = bufferHead;
        bufferHead = newBuffer;
        //将末尾的块写入内存
        if(bufferTail.isDirty){
            String[][] allocateResult = BlockSystem.getInstance().allocateContentToBlocks(bufferTail.data);
            String[][] fileMetaAllocation = fileMeta.getLogicBlock();
            int len = allocateResult.length + fileMetaAllocation.length - 1;
            String[][] res = new String[len][3];
            int[] oldLoad = fileMeta.getLoad();
            int[] newLoad = new int[len];
            System.arraycopy(oldLoad,0,newLoad,0,logicBlockNum);
            int dataSize = bufferTail.data.length;
            for(int k = logicBlockNum; k < logicBlockNum + allocateResult.length; k++){
                newLoad[k] = Math.min(PublicVars.BLOCKSIZE,dataSize);
                dataSize -= PublicVars.BLOCKSIZE;
            }
            System.arraycopy(oldLoad,logicBlockNum + 1,newLoad,logicBlockNum + allocateResult.length, len - (logicBlockNum + allocateResult.length));
            fileMeta.setLoad(newLoad);
            int i = 0;
            for( ; i < logicBlockNum; i++){
                System.arraycopy(fileMetaAllocation[i], 0, res[i], 0, 3);
            }
            for(; i < logicBlockNum + allocateResult.length; i++){
                System.arraycopy(allocateResult[i - logicBlockNum], 0, res[i], 0, 3);
            }
            for (;i < len; i++){
                System.arraycopy(fileMetaAllocation[i - allocateResult.length + 1], 0, res[i], 0, 3);
            }
            fileMeta.setLogicBlock(res);
            fileMeta.setLogicBlockNum(len);
            fileMeta.setSize(fileMeta.getSize() + bufferTail.addLength);
            fileMeta.updateFileMeta();

        }
        //将末尾的块从buffer中移除
        Buffer ptr = bufferHead;
        for(int count = 0; count < 9; count++){
            ptr = ptr.next;
        }
        bufferTail = ptr;
        ptr.next = null;
    }

    private Buffer search(int logicBlockNum){
        Buffer ptr = bufferHead, ptr2 = bufferHead;
        while(ptr != null){
            if(ptr.logicBlockNum == logicBlockNum){
               if(ptr != bufferHead){
                   ptr2.next = ptr.next;
                   ptr.next = bufferHead;
                   bufferHead = ptr;
               }
               return ptr;
            }
            ptr2 = ptr;
            ptr = ptr.next;
        }
        return null;//表示没找到
    }

    public void flush() throws ErrorCode {
        //要将dirty的块都写入磁盘
        Buffer ptr = bufferHead;
        while(ptr != null){
            if(ptr.isDirty){

                String[][] allocateResult = BlockSystem.getInstance().allocateContentToBlocks(ptr.data);
                String[][] fileMetaAllocation = fileMeta.getLogicBlock();
                int len = allocateResult.length + fileMetaAllocation.length - 1;
                int logicBlockNum = ptr.logicBlockNum;
                //改load[]
                int[] oldLoad = fileMeta.getLoad();
                int[] newLoad = new int[len];
                System.arraycopy(oldLoad,0,newLoad,0,logicBlockNum);
                int dataSize = ptr.data.length;
                for(int k = logicBlockNum; k < logicBlockNum + allocateResult.length; k++){
                    newLoad[k] = Math.min(PublicVars.BLOCKSIZE,dataSize);
                    dataSize -= PublicVars.BLOCKSIZE;
                }
                System.arraycopy(oldLoad,logicBlockNum + 1,newLoad,logicBlockNum + allocateResult.length, len - (logicBlockNum + allocateResult.length));
                fileMeta.setLoad(newLoad);
                //----------------------------------------------------
                String[][] res = new String[len][3];
                int i = 0;
                for( ; i < logicBlockNum; i++){
                    System.arraycopy(fileMetaAllocation[i], 0, res[i], 0, 3);
                }
                for(; i < logicBlockNum + allocateResult.length; i++){
                    System.arraycopy(allocateResult[i - logicBlockNum], 0, res[i], 0, 3);
                }
                for (;i < len; i++){
                    System.arraycopy(fileMetaAllocation[i - allocateResult.length + 1], 0, res[i], 0, 3);
                }
                fileMeta.setLogicBlock(res);
                fileMeta.setLogicBlockNum(len);
                fileMeta.setSize(fileMeta.getSize() + ptr.addLength);
                fileMeta.updateFileMeta();
                //将buffer中的data恢复成<=PublicVars.BLOCKSIZE
                if(ptr.data.length > PublicVars.BLOCKSIZE){
                    byte[] catData = new byte[PublicVars.BLOCKSIZE];
                    System.arraycopy(ptr.data,0,catData,0,PublicVars.BLOCKSIZE);
                    ptr.data = catData;
                }
                ptr.isDirty = false;
                ptr.addLength = 0;
            }
            ptr = ptr.next;
        }
    }

    @Override
    public void close() {
        //释放buffer链表
        Buffer ptr1 = bufferHead, ptr2 = bufferHead.next;
        while(ptr2 != null){
            ptr1.next =null;
            ptr1.data = null;
            ptr1 = ptr2;
            ptr2 = ptr2.next;
        }
        bufferHead = null;
        bufferTail = null;
    }
    //----------------------------------------------------------------------------------------
    @Override
    public Id getFileId() {
        return fileId;
    }

    @Override
    public FileManager getFileManager() {
        return fileManager;
    }

    private int getCurrLogicBlock(){
        int[] load = fileMeta.getLoad();
        if(load == null){
            //说明是没写入过的空文件
            return 0;
        }
        long pos = currPos;
        for(int i = 0 ; i < load.length; i++){
            if(pos <= load[i]){
                return i;
            }else {
                pos -= load[i];
            }
        }
        return -1;//没什么用，要是返回-1说明currPos和load记录不和谐
    }

    private int getCurrBias(){
        int[] load = fileMeta.getLoad();
        if(load == null){
            //说明是没写入过的空文件
            return 0;
        }
        long pos = currPos;
        for (int value : load) {
            if (pos <= value) {
                return (int) pos;
            } else {
                pos -= value;
            }
        }
        return -1;//没什么用，要是返回-1说明currPos和load记录不和谐
    }

    public FileMeta getFileMeta() {
        return fileMeta;
    }


    @Override
    //从currPos开始读length长的字节（包括currPos指向的字节）
    public byte[] read(int length) throws ErrorCode {
        if(currPos + length - 1> fileMeta.getSize()){
            throw new ErrorCode(ErrorCode.OFFSET_OUT_OF_BOUNDARY);
        }

        int startBlock = getCurrLogicBlock();

        //第一块读取的block偏移为
        int bias = getCurrBias();
        byte[] res = new byte[length];
        int count = 0;//记录已经读取的字节数
        //每一个logicblock从前往后读，遇到readBlock返回null则读后面duplication的一块，三块均return null则报文件损坏
        int i = startBlock;
        String[][] logicBlocks = fileMeta.getLogicBlock();
        while(count < length){
            Buffer currBlock = null;
            //缓冲区中有内容
            if( ( currBlock= search(i)) != null){
                int offset = i == startBlock ? bias - 1 : 0;
                int limit = Math.min(currBlock.data.length + count - offset, length);
                if (limit - count >= 0) System.arraycopy(currBlock.data, offset, res, count, limit - count);
                count = limit;
                i++;
            }else {
                int j = 0;
                for (; j < 3; j++){
                    int bm = Integer.parseInt(logicBlocks[i][j].substring(0,logicBlocks[i][j].indexOf("|")));
                    int blockId = Integer.parseInt(logicBlocks[i][j].substring(logicBlocks[i][j].indexOf("|") + 1));
                    int offset = i == startBlock ? bias - 1 : 0;
                    byte[] resBlockData = BlockSystem.getInstance().readBlock(bm,blockId,offset);
                    if(resBlockData != null){
                        int limit = Math.min(resBlockData.length + count, length);
                        if (limit - count >= 0) {
                            System.arraycopy(resBlockData, 0, res, count, limit - count);
                        }
                        //将这一块放进buffer
                        addBlockToBuffer(i,BlockSystem.getInstance().readBlock(bm,blockId,0));
                        count = limit;
                        i++;
                        break;
                    }
                }

                if(j == 3){
                    throw new ErrorCode(ErrorCode.FILE_IS_BROKEN);
                }
            }

        }


        return res;
    }

    @Override
    //从currPos后面开始插入
    public void write(byte[] b) throws ErrorCode {
        //思路：找出要写的块，先将块中的内容分两块读出
        //先看看buffer里面有没有，如果有，对Buffer数据进行改写；如果没有从底层读
        //将b与这两块拼接
        //作为一整个byte[]输入allocateContentToBlocks，用返回值修改fileMeta
        //将修改写入fileMeta对应的文件
        int currLogicBlock = getCurrLogicBlock();
        int bias = getCurrBias();
        Buffer currBufferBlock = null;
        if((currBufferBlock = search(currLogicBlock) )!= null){
            byte[] beforeAdd = Arrays.copyOfRange(currBufferBlock.data,0,bias);
            byte[] afterAdd = Arrays.copyOfRange(currBufferBlock.data, bias, currBufferBlock.data.length);
            byte[] res = new byte[beforeAdd.length + afterAdd.length + b.length];
            System.arraycopy(beforeAdd,0,res,0,beforeAdd.length);
            System.arraycopy(b,0,res,beforeAdd.length,b.length);
            System.arraycopy(afterAdd,0,res,beforeAdd.length + b.length,afterAdd.length);
            currBufferBlock.data = res;
            currBufferBlock.addLength += b.length;
            currBufferBlock.isDirty = true;
        }else {
            String[][] logicBlocks = fileMeta.getLogicBlock();
            if(logicBlocks.length == 0){
                //文件第一次写入
                String[][] newLogicBlocks = BlockSystem.getInstance().allocateContentToBlocks(b);//如果这里出现了错误也不会修改fileMeta
                fileMeta.setLogicBlock(newLogicBlocks);
                fileMeta.setSize(b.length);
                fileMeta.setLogicBlockNum(newLogicBlocks.length);
                int[] newLoad = new int[newLogicBlocks.length];
                int dataSize = b.length;
                for(int k = 0; k < newLoad.length; k++){
                    newLoad[k] = Math.min(PublicVars.BLOCKSIZE,dataSize);
                    dataSize -= PublicVars.BLOCKSIZE;
                }
                fileMeta.setLoad(newLoad);
                fileMeta.updateFileMeta();
            }else {
                int i = 0;
                for(; i < 3; i++){
                    int bm = Integer.parseInt(logicBlocks[currLogicBlock][i].substring(0,logicBlocks[currLogicBlock][i].indexOf("|")));
                    int blockId = Integer.parseInt(logicBlocks[currLogicBlock][i].substring(logicBlocks[currLogicBlock][i].indexOf("|") + 1));
                    byte[] currBlockData = BlockSystem.getInstance().readBlock(bm, blockId, 0);
                    if(currBlockData == null){
                        //如果读取失败会读取下一个block,所以不会改变filemeta
                        continue;
                    }

                    byte[] beforeAdd = Arrays.copyOfRange(currBlockData,0,bias);
                    byte[] afterAdd = Arrays.copyOfRange(currBlockData, bias, currBlockData.length);
                    byte[] res = new byte[beforeAdd.length + afterAdd.length + b.length];
                    System.arraycopy(beforeAdd,0,res,0,beforeAdd.length);
                    System.arraycopy(b,0,res,beforeAdd.length,b.length);
                    System.arraycopy(afterAdd,0,res,beforeAdd.length + b.length,afterAdd.length);
                    String[][] allocation = BlockSystem.getInstance().allocateContentToBlocks(res);//这是文件可能写入失败的地方
                    String[][] newLogicBlocks = new String[fileMeta.getLogicBlock().length + allocation.length - 1][3];
                    int k = 0;
                    for( ;k < currLogicBlock; k++){
                        for(int j = 0; j < 3; j++){
                            newLogicBlocks[k][j] = fileMeta.getLogicBlock()[k][j];
                        }
                    }
                    for(;k < currLogicBlock + allocation.length; k++){
                        System.arraycopy(allocation[k - currLogicBlock], 0, newLogicBlocks[k], 0, 3);
                    }
                    for (;k < newLogicBlocks.length; k++){
                        for(int j = 0; j < 3; j++){
                            newLogicBlocks[k][j] = fileMeta.getLogicBlock()[k - allocation.length + 1][j];
                        }
                    }
                    //缓冲写入的整块logicblock（即使它被写得超过size）
                    int toPutInBufferSize = Math.min(currBlockData.length + b.length, PublicVars.BLOCKSIZE);
                    byte[] toPutInBuffer = Arrays.copyOfRange(res,0,toPutInBufferSize);
                    addBlockToBuffer(currLogicBlock, toPutInBuffer);
                    fileMeta.setLogicBlock(newLogicBlocks);
                    //改load[]
                    int[] oldLoad = fileMeta.getLoad();
                    int[] newLoad = new int[newLogicBlocks.length];
                    System.arraycopy(oldLoad,0,newLoad,0,currLogicBlock);
                    int dataSize = res.length;
                    for(int n = currLogicBlock; n < currLogicBlock + allocation.length; n++){
                        newLoad[n] = Math.min(PublicVars.BLOCKSIZE,dataSize);
                        dataSize -= PublicVars.BLOCKSIZE;
                    }
                    System.arraycopy(oldLoad,currLogicBlock + 1,newLoad,currLogicBlock + allocation.length,
                            newLogicBlocks.length  - (currLogicBlock + allocation.length));
                    fileMeta.setLoad(newLoad);
                    //----------------------------------------------------
                    fileMeta.setLogicBlockNum(newLogicBlocks.length);
                    fileMeta.setSize(fileMeta.getSize() + b.length);
                    fileMeta.updateFileMeta();
                    break;
                }
                if(i == 3){
                    throw new ErrorCode(ErrorCode.FILE_IS_BROKEN);
                }
            }

        }

    }

    @Override
    public void setSize(long newSize) {
        if(newSize > fileMeta.getSize()){
            long addLength = newSize - fileMeta.getSize();
            byte[] addByte = new byte[(int) addLength];
            for(int i = 0; i < addByte.length; i++){
                addByte[i] = 0x00;
            }
            move(0,MOVE_TAIL);
            write(addByte);
        }else if(newSize < fileMeta.getSize()){
            move(newSize - 1, MOVE_HEAD);
            int logicBlock = getCurrLogicBlock();
            int bias = getCurrBias();
            String[] physicalBlock = fileMeta.getLogicBlock()[logicBlock];
            byte[] lastBytes = null;
            int i = 0;
            for(; i < 3; i++){
                int bm = Integer.parseInt(physicalBlock[i].substring(0,physicalBlock[i].indexOf("|")));
                int blkid = Integer.parseInt(physicalBlock[i].substring(physicalBlock[i].indexOf("|") + 1));
                if((lastBytes = BlockSystem.getInstance().readBlock(bm,blkid,0)) != null){
                    lastBytes = Arrays.copyOfRange(lastBytes,0,bias);
                    break;
                }
            }
            if(i == 3){
                throw new ErrorCode(ErrorCode.FILE_IS_BROKEN);
            }

            assert lastBytes != null;
            String[][] allocation = BlockSystem.getInstance().allocateContentToBlocks(lastBytes);//这个理论上铁定只有一行
            String[][] newLogicBlocks = new String[logicBlock + 1][3];
            for(int j = 0; j < logicBlock; j++){
                System.arraycopy(fileMeta.getLogicBlock()[j],0,newLogicBlocks[j],0,3);
            }
            System.arraycopy(allocation[0],0,newLogicBlocks[logicBlock],0,3);

            //改load
            int[] newLoad = new int[logicBlock + 1];
            System.arraycopy(fileMeta.getLoad(),0,newLoad,0,logicBlock + 1);
            newLoad[logicBlock] = bias;

            fileMeta.setLoad(newLoad);
            fileMeta.setLogicBlock(newLogicBlocks);
            fileMeta.setLogicBlockNum(logicBlock + 1);
            fileMeta.setSize(newSize);
            fileMeta.updateFileMeta();

        }
    }

    @Override
    //从where开始往后移offset,还要进行一些边界处理，offset有正有负
    public long move(long offset, int where) throws ErrorCode{
        try {
            //去读文件的meta，读出size
            java.io.File fmeta = new java.io.File("FM//fm-" + fileManager.getFileManagerId().getId() + "//" + fileId.getId() + ".meta");
            FileReader reader = new FileReader(fmeta);
            BufferedReader breader = new BufferedReader(reader);
            breader.readLine();
            String sizeLine = breader.readLine();
            int size = Integer.parseInt(sizeLine.substring(sizeLine.indexOf(":") + 1));

            switch (where){
                case 0:
                    if(currPos + offset > size || currPos + offset < 0){
                        throw new ErrorCode(ErrorCode.OFFSET_OUT_OF_BOUNDARY);
                    }else {
                        currPos += offset;
                        return currPos;
                    }
                case 1:
                    if(offset + 1 > size || offset + 1 < 0){
                        throw new ErrorCode(ErrorCode.OFFSET_OUT_OF_BOUNDARY);
                    }else {
                        currPos = offset + 1;
                        return currPos;
                    }
                case 2:
                    if(size + offset > size || size + offset < 0){
                        throw new ErrorCode(ErrorCode.OFFSET_OUT_OF_BOUNDARY);
                    }else {
                        currPos = size + offset;
                        return currPos;
                    }

            }
            //如果where输入错误
            return -1;
        }catch (IOException e){
            throw (ErrorCode)new ErrorCode(ErrorCode.GET_FILE_SIZE_FAIL).initCause(e);
        }


    }


    @Override
    public long size() throws ErrorCode {
        return move(0,MOVE_TAIL);
    }



}



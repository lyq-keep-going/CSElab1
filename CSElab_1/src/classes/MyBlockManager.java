package classes;

import interfaces.Block;
import interfaces.BlockManager;
import interfaces.Id;

import java.io.*;

public class MyBlockManager implements BlockManager {
    private Id BlockManagerId;
    public MyBlockManager(Id id) throws ErrorCode{
        this.BlockManagerId = id;
        File dir =  new File("BM//bm-"+ BlockManagerId.getId());

        if(!dir.isDirectory()){
            if(!dir.mkdirs()){
                throw new ErrorCode(ErrorCode.BLOCKMANAGER_CREATION_FAIL);
            }
        }
    }

    public Id getBlockManagerId() {
        return BlockManagerId;
    }

    @Override
    public Block getBlock(Id indexId)  throws ErrorCode{
        try{
            //要查询一下blockload
            int load = 0;
            File metafile = new File("BM//bm-" + BlockManagerId.getId() + "//" + indexId.getId() + ".meta");
            if(!metafile.isFile()){
                throw new ErrorCode(ErrorCode.BLOCK_NOT_EXIST);
            }
            FileReader reader = new FileReader(metafile);
            BufferedReader breader = new BufferedReader(reader);
            String line;
            while((line = breader.readLine()) != null){
                if(line.contains("load")){
                    load = Integer.parseInt(line.substring(line.indexOf(":") + 1));
                }
            }
            return new MyBlock(indexId, this, load);
        }catch (IOException e){
            throw new ErrorCode(ErrorCode.IO_EXCEPTION);
        }


    }

    @Override
    public Block newBlock(byte[] b) throws ErrorCode {
        try {
            //要先检查b是否在规定大小内
            if(b.length > PublicVars.BLOCKSIZE){
                throw new ErrorCode(ErrorCode.DATA_LENGTH_EXCEED_BLOCK_SIZE);
            }
            File dir =  new File("BM//bm-"+ BlockManagerId.getId());

            //选择一个未分配的block号
            int blockId = PublicMethods.chooseSpareId("BM//bm-"+ BlockManagerId.getId() + "//meta");
            //创建一个.data文件
            File data = new File(dir,blockId + ".data");
            if(!data.createNewFile()){
                throw new ErrorCode(ErrorCode.BLOCK_DATA_CREATION_FAIL);
            }
            FileOutputStream out = new FileOutputStream(data);
            BufferedOutputStream bout = new BufferedOutputStream(out);
            bout.write(b,0,b.length);
            bout.flush();
            out.close();


            //创建一个.meta文件
            File meta = new File(dir,blockId + ".meta");
            if(!meta.createNewFile()){
                throw new ErrorCode(ErrorCode.BLOCK_META_CREATION_FAIL);
            }

            FileWriter writer  = new FileWriter(meta);
            BufferedWriter bwriter = new BufferedWriter(writer);
            //写入size、load和checksum
            String s = "size:"+PublicVars.BLOCKSIZE;
            bwriter.write(s,0,s.length());
            bwriter.newLine();
            s = "load:" + b.length;
            bwriter.write(s,0,s.length());
            bwriter.newLine();
            s = "checksum:" + PublicMethods.getCRC32Checksum(b);
            bwriter.write(s,0,s.length());
            bwriter.newLine();
            bwriter.flush();
            writer.close();

            return new MyBlock(new BlockId(blockId),this, b.length);
        }catch (IOException e){
            throw new ErrorCode(ErrorCode.IO_EXCEPTION);
        }

    }

}

package classes;

import java.io.*;

public class FileMeta {
    private FileId fileId;
    private  MyFileManager fileManager;
    private long size;
    private int logicBlockNum;
    private String[][] logicBlock;
    private int[] load;

    //此构造方法用于文件已经被创建时使用FileMeta
    public FileMeta(FileId fileId, MyFileManager fileManager) throws ErrorCode {
        try {
            //先找到meta文件
            String path = "FM//fm-" + fileManager.getFileManagerId().getId() + "//" +  fileId.getId()+ ".meta";
            File fileMeta = new File(path);
            FileReader reader = new FileReader(fileMeta);
            BufferedReader breader = new BufferedReader(reader);
            String tmp = breader.readLine();//这一行是filename，可以忽视掉
            tmp = breader.readLine();//这一行是size
            size = Long.parseLong(tmp.substring(tmp.indexOf(":") + 1));
            tmp = breader.readLine();//这一行是logic block num
            logicBlockNum = Integer.parseInt(tmp.substring(tmp.indexOf(":") + 1));
            logicBlock = new String[logicBlockNum][3];
            load = new int[logicBlockNum];
            while((tmp = breader.readLine()) != null){
                int i = Integer.parseInt(tmp.substring(0,tmp.indexOf("#")));//当前行号
                load[i] = Integer.parseInt(tmp.substring(tmp.indexOf("#") + 1,tmp.indexOf(":")));
                tmp = tmp.substring(tmp.indexOf(":") + 1);
                String[] split = tmp.split(",");
                System.arraycopy(split, 0, logicBlock[i], 0, 3);
            }
            this.fileId = fileId;
            this.fileManager = fileManager;
        }catch (IOException e){
            throw (ErrorCode)new ErrorCode(ErrorCode.READ_FILEMETA_FAIL).initCause(e);
        }


    }

    //此方法用于创建文件时创建meta
    public FileMeta(FileId fileId, MyFileManager fileManager, long size, int logicBlockNum, String[][] logicBlock) throws ErrorCode{
        this.fileId = fileId;
        this.fileManager = fileManager;
        this.size = size;
        this.logicBlockNum = logicBlockNum;
        this.logicBlock = logicBlock;
        this.load = null;
        updateFileMeta();
    }

    public void updateFileMeta() throws ErrorCode {
        try {
            //先创建文件
            String path = "FM//fm-" + fileManager.getFileManagerId().getId() + "//" + fileId.getId() + ".meta";
            File fileMeta = new File(path);
            if(!fileMeta.isFile()){
                if(!fileMeta.createNewFile()){
                    throw new ErrorCode(ErrorCode.FILEMETA_CREATION_FAIL);
                }
            }
            FileWriter writer= new FileWriter(fileMeta,false);
            BufferedWriter bwriter = new BufferedWriter(writer);
            StringBuilder add = new StringBuilder("filename:" + fileId.getFilename() + System.getProperty("line.separator"));
            bwriter.write(add.toString(),0,add.toString().length());
            add = new StringBuilder("size:" + size + System.getProperty("line.separator"));
            bwriter.write(add.toString(),0,add.toString().length());
            add = new StringBuilder("logic block num:" + logicBlockNum + System.getProperty("line.separator"));
            bwriter.write(add.toString(),0,add.toString().length());
            //开始写logicblock
            for(int i = 0; i < logicBlock.length; i++){
                add = new StringBuilder(i + "#" + load[i] + ":");
                for(int j = 0; j < logicBlock[i].length; j++){
                    add.append(logicBlock[i][j]).append(",");
                }
                add = new StringBuilder(add.substring(0, add.length() - 1) + System.getProperty("line.separator"));
                bwriter.write(add.toString(),0,add.toString().length());
            }
            bwriter.flush();
            writer.close();
        }catch (IOException e){
            throw (ErrorCode) new ErrorCode(ErrorCode.FILEMETA_UPDATE_FAIL).initCause(e);
        }

    }

    public FileId getFile() {
        return fileId;
    }

    public MyFileManager getFileManager() {
        return fileManager;
    }

    public long getSize() {
        return size;
    }

    public int getLogicBlockNum() {
        return logicBlockNum;
    }

    public int[] getLoad() {
        return load;
    }

    public String[][] getLogicBlock() {
        return logicBlock;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setLoad(int[] load) {
        this.load = load;
    }

    public void setLogicBlockNum(int logicBlockNum) {
        this.logicBlockNum = logicBlockNum;
    }

    public void setLogicBlock(String[][] logicBlock) {
        this.logicBlock = logicBlock;
    }
}

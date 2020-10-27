package classes;

import interfaces.File;
import interfaces.FileManager;
import interfaces.Id;

import java.io.FileWriter;
import java.io.IOException;

public class MyFileManager implements FileManager {
    private Id fileManagerId;

    public MyFileManager(Id id) throws ErrorCode{
        this.fileManagerId = id;
        //在DM中生成一个目录
        java.io.File fm = new java.io.File("FM//fm-" + id.getId());
        if(!fm.isDirectory()){
            if(!fm.mkdir()){
                throw new ErrorCode(ErrorCode.FILEMANAGER_CREATION_FAIL);
            }
            java.io.File meta = new java.io.File(fm,"meta");
            //创建meta文件
            if(!meta.isFile()){
                try {
                    if(!meta.createNewFile()){
                        throw new ErrorCode(ErrorCode.FILEMANAGER_CREATION_FAIL);
                    }
                    FileWriter w = new FileWriter(meta);
                    w.write("used:");
                    w.close();
                }catch (IOException e){
                    throw (ErrorCode)new ErrorCode(ErrorCode.FILEMANAGER_CREATION_FAIL).initCause(e);
                }


            }
        }
    }

    public Id getFileManagerId() {
        return fileManagerId;
    }

    @Override
    public File getFile(Id fileId) throws ErrorCode {
        return new MyFile((FileId)fileId,this,true);
    }

    @Override
    public File newFile(Id fileId) throws ErrorCode {
        //在文件中记录filename对应的FileManager和fileId
        //然后调用MyFile的构造方法
        java.io.File nameToFmAndId = new java.io.File("FM//filenameToFmAndId");
        if(!nameToFmAndId.isFile()){
            try {
                if(!nameToFmAndId.createNewFile()){
                    throw new ErrorCode(ErrorCode.FILENAME_MAP_CREATION_FAIL);
                }
            } catch (IOException e) {
                throw (ErrorCode)new ErrorCode(ErrorCode.FILENAME_MAP_CREATION_FAIL).initCause(e);
            }
        }
        try {
            FileWriter writer = new FileWriter(nameToFmAndId,true);
            writer.write(((FileId)fileId).getFilename() + ":" +fileManagerId.getId() + "-" + ((FileId)fileId).getId());
            writer.write(System.getProperty("line.separator"));
            writer.flush();
            writer.close();
            FileSystem.updateFilenameToFmAndId();
        }catch (IOException e){
            throw (ErrorCode)new ErrorCode(ErrorCode.FILENAME_ADD_TO_FILENAME_MAP_FAIL).initCause(e);
        }

        return new MyFile((FileId)fileId,this);
    }
}

package classes;

import interfaces.Id;

import java.io.IOException;

public class FileId implements Id {
    private int id;
    private String filename;

    public FileId(String filename, MyFileManager fileManager) throws ErrorCode{
        //先对filename进行查找，如果有记录则不需要新生成id
        FileSystem.updateFilenameToFmAndId();
        if(FileSystem.filenameToFmAndId.containsKey(filename)){
            String value = FileSystem.filenameToFmAndId.get(filename);
            id = Integer.parseInt(value.substring(value.indexOf("-") + 1));
        }else {
            try {
                id = PublicMethods.chooseSpareId("FM//fm-" + fileManager.getFileManagerId().getId() +"//meta");
            } catch (IOException e) {
                throw new ErrorCode(ErrorCode.FILEID_GENERATION_FAIL);
            }
        }
        this.filename = filename;
    }

    public int getId() {
        return id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}


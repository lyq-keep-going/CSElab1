package classes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

public class FileSystem {
    private static MyFileManager[] fileManagers;
    public static HashMap<String,String> filenameToFmAndId;
    private static final int NUMOFFILEMANAGERS = 10;
    private static FileSystem single = null;

    private FileSystem() throws ErrorCode{
        //先初始化十个fileManager
        fileManagers = new MyFileManager[NUMOFFILEMANAGERS];
        for(int i = 0; i < NUMOFFILEMANAGERS; i++){
            fileManagers[i] = new MyFileManager(new FileManagerId(i));
        }
        //将filename与fm和id对应表读入HashMap
        updateFilenameToFmAndId();
    }

    public  static FileSystem getInstance(){
        if(single == null){
            single = new FileSystem();
        }
        return single;
    }

    public static void updateFilenameToFmAndId() throws ErrorCode {
        try {
            File f2fmandid = new File("FM//filenameToFmAndId");
            if(!f2fmandid.isFile()){
                filenameToFmAndId = new HashMap<>();
                if(!f2fmandid.createNewFile()){
                    throw new ErrorCode(ErrorCode.FILENAME_MAP_CREATION_FAIL);
                }
                return;
            }
            FileReader reader = new FileReader(f2fmandid);
            BufferedReader breader = new BufferedReader(reader);
            HashMap<String,String> res = new HashMap<>();
            String tmp;
            while((tmp = breader.readLine()) != null){
                String filename = tmp.substring(0, tmp.indexOf(":"));
                String fmAndId = tmp.substring(tmp.indexOf(":") + 1);
                res.put(filename,fmAndId);
            }
            filenameToFmAndId = res;
        }catch (IOException e){
            throw (ErrorCode)new ErrorCode(ErrorCode.READ_FILENAME_MAP_FAIL).initCause(e);
        }

    }

    public interfaces.File newFile(String filename) throws ErrorCode {
        //判断该文件名是否用过
        if(filenameToFmAndId.containsKey(filename)){
            throw new ErrorCode(ErrorCode.DUPLICATE_FILENAME);
        }
        //先随机选择一个FileManger来接管这个文件
        Random random = new Random();
        int manager = random.nextInt(10);
        return fileManagers[manager].newFile(new FileId(filename, fileManagers[manager]));
    }

    public interfaces.File getFile(String filename) throws ErrorCode {
        if(!filenameToFmAndId.containsKey(filename)){
            throw new ErrorCode(ErrorCode.FILE_NOT_CREATED);
        }

        String fmAndId = filenameToFmAndId.get(filename);
        int fm = Integer.parseInt(fmAndId.substring(0,fmAndId.indexOf("-")));
        return  fileManagers[fm].getFile(new FileId(filename,fileManagers[fm]));

    }
}

package classes;

import java.util.HashMap;
import java.util.Map;

public class ErrorCode extends RuntimeException {
    public static final int IO_EXCEPTION = 1;
    public static final int CHECKSUM_CHECK_FAILED = 2;
    public static final int BLOCK_DATA_MISSING = 3;
    public static final int INCOMPLETE_READING = 4;
    public static final int BLOCK_NOT_EXIST = 5;
    public static final int DATA_LENGTH_EXCEED_BLOCK_SIZE = 6;
    public static final int BLOCKMANAGER_CREATION_FAIL = 7;
    public static final int BLOCK_DATA_CREATION_FAIL = 8;
    public static final int BLOCK_META_CREATION_FAIL = 9;
    public static final int DATA_ALLOCATION_FAIL = 10;
    public static final int FILEMETA_CREATION_FAIL = 11;
    public static final int FILEMETA_UPDATE_FAIL = 12;
    public static final int READ_FILEMETA_FAIL = 13;
    public static final int OFFSET_OUT_OF_BOUNDARY = 14;
    public static final int FILE_IS_BROKEN =15;
    public static final int GET_FILE_SIZE_FAIL = 16;
    public static final int FILEMANAGER_CREATION_FAIL = 17;
    public static final int FILENAME_MAP_CREATION_FAIL = 18;
    public static final int FILENAME_ADD_TO_FILENAME_MAP_FAIL = 19;
    public static final int READ_FILENAME_MAP_FAIL = 20;
    public static final int DUPLICATE_FILENAME = 21;
    public static final int FILEID_GENERATION_FAIL = 22;
    public static final int FILE_NOT_CREATED = 23;



    private static final Map<Integer, String> ErrorCodeMap = new HashMap<>();

    static {
        ErrorCodeMap.put(IO_EXCEPTION, "IO exception");
        //Block类中异常-------------------------------------------------------------------
        ErrorCodeMap.put(CHECKSUM_CHECK_FAILED, "block checksum check failed");
        ErrorCodeMap.put(BLOCK_DATA_MISSING,"the data of the block is missing");
        ErrorCodeMap.put(INCOMPLETE_READING,"incomplete reading");//没有读入全部block数据
        //BlockManager中异常
        ErrorCodeMap.put(BLOCK_NOT_EXIST,"the block doesn't exist");
        ErrorCodeMap.put(DATA_LENGTH_EXCEED_BLOCK_SIZE,"the data is too large for the block! creation fail...");
        ErrorCodeMap.put(BLOCKMANAGER_CREATION_FAIL,"block manager creation fail");
        //BlockSystem中异常
        ErrorCodeMap.put(DATA_ALLOCATION_FAIL,"data allocation fail");
        //FileMeta中异常
        ErrorCodeMap.put(FILEMETA_CREATION_FAIL,"file meta creation fail");
        ErrorCodeMap.put(FILEMETA_UPDATE_FAIL,"file meta update fail");
        ErrorCodeMap.put(READ_FILEMETA_FAIL,"read file meta fail");
        //File中的异常
        ErrorCodeMap.put(OFFSET_OUT_OF_BOUNDARY,"the offset is out of boundary");
        ErrorCodeMap.put(FILE_IS_BROKEN,"the file is broken");
        ErrorCodeMap.put(GET_FILE_SIZE_FAIL,"get file size fail");
        //FileManager中的异常
        ErrorCodeMap.put(FILEMANAGER_CREATION_FAIL,"file manager creation fail");
        ErrorCodeMap.put(FILENAME_MAP_CREATION_FAIL,"filename map creation fail");
        ErrorCodeMap.put(FILENAME_ADD_TO_FILENAME_MAP_FAIL,"filename add to filename map fail");
        //FileSystem中的异常
        ErrorCodeMap.put(READ_FILENAME_MAP_FAIL,"read filename map fail");
        ErrorCodeMap.put(DUPLICATE_FILENAME,"the filename has been used");
        ErrorCodeMap.put(FILE_NOT_CREATED,"the file hasn't been created");
        //FileId中异常
        ErrorCodeMap.put(FILEID_GENERATION_FAIL,"file id generation fail");
    }

    public static String getErrorText(int errorCode) {
        return ErrorCodeMap.getOrDefault(errorCode, "invalid");
    }

    private int errorCode;

    public ErrorCode(int errorCode) {
        super(String.format("error code '%d' \"%s\"", errorCode, getErrorText(errorCode))) ;
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}

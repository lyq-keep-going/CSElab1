package classes;

import com.sun.xml.internal.fastinfoset.tools.FI_DOM_Or_XML_DOM_SAX_SAXEvent;
import interfaces.Block;

import java.io.*;
import java.util.Arrays;
import java.util.Scanner;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class PublicMethods {
    public static long getCRC32Checksum(byte[] bytes) {
        Checksum crc32 = new CRC32();
        crc32.update(bytes, 0, bytes.length);
        return crc32.getValue();
    }

    public static int chooseSpareId(String metaPath) throws IOException {
        File file = new File(metaPath);
        //判断meta文件是否已被创造
        if(!file.isFile()){
            if(file.createNewFile()){
                FileWriter writer = new FileWriter(file);
                String s = "used:0";
                writer.write(s);
                writer.close();
                return 0;
            }

        }
        FileReader reader = new FileReader(file);
        BufferedReader breader = new BufferedReader(reader);
        String metaString = breader.readLine();
        reader.close();

        metaString = metaString.substring(metaString.indexOf(":") + 1);
        //如果已分配块的记录为空
        if(metaString.length() == 0){
            FileWriter wmeta = new FileWriter(file,true);
            wmeta.append("0");
            wmeta.close();
            return 0;
        }
        String[] chars = metaString.split(",");
        int[] nums = new int[chars.length];
        for(int i = 0; i < chars.length; i++){
            nums[i] = Integer.parseInt(chars[i]);
        }
        Arrays.sort(nums);

        for(int i = 0; i < nums.length; i++){
            if(i + 1 == nums.length || nums[i + 1] != nums[i] + 1){
                //写入已分配的block号
                int res = nums[i] + 1;
                FileWriter wmeta = new FileWriter(file,true);
                String add = "," + res;
                wmeta.append(add);
                wmeta.close();
                return  res;
            }
        }

        int res = nums[nums.length - 1] + 1;
        //写入已分配的block号
        FileWriter wmeta = new FileWriter(file,true);
        String add = "," + res;
        wmeta.append(add);
        wmeta.close();

        return res;
    }

    public static void smart_cat(String fileName) throws UnsupportedEncodingException {
        FileSystem fs = FileSystem.getInstance();
        interfaces.File file = fs.getFile(fileName);
        byte[] fileBytes =  file.read((int) ((MyFile)file).getFileMeta().getSize());
        System.out.println(new String(fileBytes,"UTF-8"));

    }

    public static void smart_hex(Block block){
        byte[] blockData = block.read();
        String dataString = bytesToHexString(blockData);
        System.out.println(dataString);
    }

    public static String bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public static boolean smart_write(String fileName, int index){
        interfaces.File file = FileSystem.getInstance().getFile(fileName);
        file.move(index - 1, interfaces.File.MOVE_HEAD);
        System.out.println("请输入想给文件中添加的内容~~");
        Scanner input = new Scanner(System.in);
        String userAdd = input.next();
        file.write(userAdd.getBytes());
        return true;
    }
    public static boolean smart_copy(String from, String to){
        interfaces.File fileFrom = FileSystem.getInstance().getFile(from);
        interfaces.File fileTo = FileSystem.getInstance().getFile(to);
        //如果文件不存在会抛出异常，如果要求需要新建文件可以在异常处理中进行
        FileMeta fromMeta =  ((MyFile)fileFrom).getFileMeta();
        FileMeta toMeta = ((MyFile)fileTo).getFileMeta();
        toMeta.setSize(fromMeta.getSize());
        toMeta.setLogicBlockNum(fromMeta.getLogicBlockNum());
        toMeta.setLogicBlock(fromMeta.getLogicBlock());
        toMeta.setLoad(fromMeta.getLoad());
        toMeta.updateFileMeta();
        return true;
    }

}

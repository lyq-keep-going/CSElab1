package test;

import java.io.*;
import java.util.Arrays;
import java.util.Scanner;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import classes.*;
import interfaces.Block;
import interfaces.File;

public class CheckSumtest {
    public static void main(String[] args) throws IOException {
//        File testFile = new File("1.txt");
//
//        FileInputStream input = new FileInputStream(testFile);
//        byte[] buffer = new byte[5000];
//        input.read(buffer);
//
//        PublicMethods.smart_cat("test4");

       File file2 = FileSystem.getInstance().getFile("file3");
       file2.move(-2,File.MOVE_HEAD);
        file2.write("abcdefghijk".getBytes());
        PublicMethods.smart_cat("file3");


    }



}

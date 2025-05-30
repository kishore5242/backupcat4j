package org.kapps;

import java.io.File;
import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) {
        try {
            File source = new File("D:\\projects\\temp\\source");
            File target = new File("D:\\projects\\temp\\destination");
            FileManager.copyFolder(source, target);
            System.out.println("Folder copied successfully.");
        } catch (IOException e) {
            System.err.println("Copy failed: " + e.getMessage());
        }
    }
}

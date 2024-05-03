package org.example;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Main {

    public static void main(String[] args) {
        String content = "Hello, world! This is a sample file content.";
        String fileName = "file.txt";

        // Specify the file path and name for the ZIP archive
        String zipFilePath = "/home/xplocode/OM/AWS/downloadZip/filename.zip";

        // Save the file as a ZIP archive with the specified name
        boolean saved = saveFileAsZip(content, fileName, zipFilePath);

        if (saved) {
            System.out.println("File saved successfully.");
        } else {
            System.out.println("Failed to save file.");
        }
    }

    private static boolean saveFileAsZip(String content, String fileName, String zipFilePath) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            // Add a new ZIP entry with the file name
            zos.putNextEntry(new ZipEntry(fileName));

            // Write content to the ZIP file
            zos.write(content.getBytes());
            zos.closeEntry();

            zos.finish();
            // Write the ZIP file to the specified file path
            Files.write(Paths.get(zipFilePath), baos.toByteArray());
            return true;
        } catch (IOException e) {
            // Handle file saving error
            e.printStackTrace();
            return false;
        }
    }
}


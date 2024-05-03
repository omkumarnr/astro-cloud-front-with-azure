package org.example;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class StreamLambdaHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

        // Check if the request body contains form data (multipart/form-data)
        if (event.getIsBase64Encoded() && event.getBody() != null && !event.getBody().isEmpty()) {
            // Decode the base64-encoded body
            String decodedBody = new String(java.util.Base64.getDecoder().decode(event.getBody()));

            // Log the decoded body to inspect the structure
            context.getLogger().log("Decoded Body: " + decodedBody);

            // Split the decoded body to extract individual parts
            String[] parts = decodedBody.split("\r\n\r\n");

            // Expecting at least one part (one file)
            if (parts.length >= 2) {
                String filename = extractFileName(parts[0]);

                if (filename != null) {
                    // Save the file as a ZIP archive
                    String savePath = "/home/xplocode/OM/AWS/downloadZip/" + filename + ".zip";
                    boolean saved = saveFileAsZip(parts[1].getBytes(), savePath, filename);

                    if (saved) {
                        response.setStatusCode(200);
                        response.setBody("File saved successfully: " + savePath);
                        return response;
                    } else {
                        response.setStatusCode(500);
                        response.setBody("Failed to save file");
                        return response;
                    }
                }
            }
        }

        response.setStatusCode(400);
        response.setBody("Bad Request: At least one file expected in the request");
        return response;
    }

    private String extractFileName(String part) {
        String[] headerLines = part.split("\r\n");
        String filename = null;

        // Find Content-Disposition header to extract filename
        for (String line : headerLines) {
            if (line.startsWith("Content-Disposition: form-data;") && line.contains("filename=")) {
                String[] filenameParts = line.split("filename=\"");
                if (filenameParts.length > 1) {
                    filename = filenameParts[1].substring(0, filenameParts[1].length() - 1); // Remove trailing quote
                    break;
                }
            }
        }

        return filename;
    }

    private boolean saveFileAsZip(byte[] fileContent, String zipFilePath, String filename) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            // Add a new ZIP entry with the file name
            ZipEntry entry = new ZipEntry(filename);
            zos.putNextEntry(entry);

            // Write content to the ZIP file
            zos.write(fileContent);
            zos.closeEntry();

            // Finish writing the ZIP file
            zos.finish();

            // Write the ZIP file to the specified file path
            java.nio.file.Files.write(java.nio.file.Paths.get(zipFilePath), baos.toByteArray());
            return true;
        } catch (IOException e) {
            // Handle file saving error
            e.printStackTrace();
            return false;
        }
    }
}






















package com.unicenta.pos.einvoice;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;

/**
 * Handles submission of signed e-CF XMLs to DGII and stores the response locally.
 */
public class ECFTransport {

    /**
     * Sends a signed XML file to DGII and returns the path to the response XML saved locally.
     * 
     * @param signedXmlPath Full path to the signed XML file
     * @return Path to saved DGII response XML
     * @throws Exception on failure
     */
    public String sendToDGII(String signedXmlPath) throws Exception {
        // Load auth token (placeholder for production OAuth or WS logic)
        String authToken = getAuthToken(); // e.g. "Bearer eyJ...";

        // Setup HTTP POST connection
        String urlStr = ECFConfig.get("endpoint.send");
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + authToken);
        conn.setRequestProperty("Content-Type", "application/xml; charset=utf-8");
        conn.setDoOutput(true);

        // 🔁 Send the signed XML file
        try (OutputStream os = conn.getOutputStream()) {
            Files.copy(new File(signedXmlPath).toPath(), os);
        }

        // 📥 Read DGII response (error stream if status >= 400)
        InputStream responseStream = conn.getResponseCode() >= 400
                ? conn.getErrorStream()
                : conn.getInputStream();

        String responsePath = signedXmlPath.replace("_signed.xml", "_response.xml");

        // 💾 Save response to local file (JDK 8-compatible)
        try (FileOutputStream fos = new FileOutputStream(responsePath)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = responseStream.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }

        return responsePath;
    }

    /**
     * Placeholder for actual token acquisition – to be replaced by real OAuth or WS token logic
     */
    private String getAuthToken() throws IOException {
        // MOCK ONLY – In production, obtain this from DGII auth service
        return ECFConfig.get("mock.auth.token", "eyJMOCKTOKEN...==");
    }
}

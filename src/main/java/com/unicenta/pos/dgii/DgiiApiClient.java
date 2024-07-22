package com.unicenta.pos.dgii;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DgiiApiClient {

    private static final String DGII_API_URL = "https://api.dgii.gov.do/encf";
    private Connection connection;

    public DgiiApiClient(Connection connection) {
        this.connection = connection;
    }

    public String sendEncfXml(String xml) throws Exception {
        URL url = new URL(DGII_API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/xml");

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = xml.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        String responseMessage = connection.getResponseMessage();

        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new Exception("Failed to send ENCF XML to DGII. HTTP response code: " + responseCode);
        }

        logRequestResponse(xml, responseMessage);
        return responseMessage;
    }

    private void logRequestResponse(String request, String response) {
        String sql = "INSERT INTO dgii_logs (request, response) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, request);
            stmt.setString(2, response);
            stmt.executeUpdate();
        } catch (SQLException e) {
            Logger.getLogger(DgiiApiClient.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}

package com.unicenta.pos.einvoice;

import com.unicenta.pos.einvoice.model.ECFLogEntry;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ECFLogger {

    private final Connection connection;

    public ECFLogger(Connection connection) {
        this.connection = connection;
    }

    public void logStatus(ECFLogEntry entry) {
        String sql = "INSERT INTO e_invoice_log (ticket_id, encf, status, xml_signed_path, xml_response_path, error_message, timestamp_sent, timestamp_response, retries) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, entry.ticketId);
            ps.setString(2, entry.encf);
            ps.setString(3, entry.status);
            ps.setString(4, entry.xmlSignedPath);
            ps.setString(5, entry.xmlResponsePath);
            ps.setString(6, entry.errorMessage);
            ps.setTimestamp(7, entry.timestampSent != null ? new Timestamp(entry.timestampSent.getTime()) : null);
            ps.setTimestamp(8, entry.timestampResponse != null ? new Timestamp(entry.timestampResponse.getTime()) : null);
            ps.setInt(9, entry.retries);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("ECFLogger failed to insert log: " + e.getMessage());
        }
    }

    public List<ECFLogEntry> getFailedInvoices() {
        List<ECFLogEntry> failed = new ArrayList<>();
        String sql = "SELECT * FROM e_invoice_log WHERE status = 'FAILED'";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ECFLogEntry entry = new ECFLogEntry();
                entry.ticketId = rs.getString("ticket_id");
                entry.encf = rs.getString("encf");
                entry.status = rs.getString("status");
                entry.xmlSignedPath = rs.getString("xml_signed_path");
                entry.xmlResponsePath = rs.getString("xml_response_path");
                entry.errorMessage = rs.getString("error_message");
                entry.retries = rs.getInt("retries");
                entry.timestampSent = rs.getTimestamp("timestamp_sent");
                entry.timestampResponse = rs.getTimestamp("timestamp_response");
                failed.add(entry);
            }
        } catch (SQLException e) {
            System.err.println("ECFLogger failed to load failed invoices: " + e.getMessage());
        }

        return failed;
    }
}

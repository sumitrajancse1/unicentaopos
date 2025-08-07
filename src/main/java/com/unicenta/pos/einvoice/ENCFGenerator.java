package com.unicenta.pos.einvoice;

import java.sql.*;

public class ENCFGenerator {

    private final Connection connection;

    public ENCFGenerator(Connection connection) {
        this.connection = connection;
    }

    public String getNextENCF(String tipoECF) throws SQLException {
        String sql = "SELECT * FROM ecf_sequence_pool WHERE tipo_ecf = ? AND active = TRUE LIMIT 1 FOR UPDATE";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tipoECF);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                throw new IllegalStateException("No active e-NCF series for type " + tipoECF);
            }

            long next = rs.getLong("next_sequence");
            long max = rs.getLong("max_sequence");
            String prefix = rs.getString("serie_prefix");

            if (next > max) {
                // Exhausted
                markSeriesInactive(tipoECF);
                throw new IllegalStateException("e-NCF series for type " + tipoECF + " has been exhausted.");
            }

            // Alert threshold
            if (max - next < 30) {
                System.err.println("[WARNING] e-NCF series for type " + tipoECF + " is running low. Only " + (max - next) + " left!");
            }

            // Generate
            String encf = prefix + String.format("%010d", next);

            // Update sequence
            PreparedStatement update = connection.prepareStatement("UPDATE ecf_sequence_pool SET next_sequence = ? WHERE tipo_ecf = ? AND active = TRUE");
            update.setLong(1, next + 1);
            update.setString(2, tipoECF);
            update.executeUpdate();

            return encf;
        }
    }

    private void markSeriesInactive(String tipoECF) throws SQLException {
        PreparedStatement update = connection.prepareStatement("UPDATE ecf_sequence_pool SET active = FALSE WHERE tipo_ecf = ? AND active = TRUE");
        update.setString(1, tipoECF);
        update.executeUpdate();
    }
}

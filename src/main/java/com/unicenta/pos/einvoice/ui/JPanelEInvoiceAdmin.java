package com.unicenta.pos.einvoice.ui;

import com.unicenta.pos.einvoice.ECFLogger;
import com.unicenta.pos.einvoice.ECFResender;
import com.unicenta.pos.einvoice.model.ECFLogEntry;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.util.List;

public class JPanelEInvoiceAdmin extends JPanel {

    private final JTable table;
    private final DefaultTableModel model;
    private final ECFLogger logger;
    private final ECFResender resender;

    public JPanelEInvoiceAdmin(Connection connection) {
        this.logger = new ECFLogger(connection);
        this.resender = new ECFResender(connection);

        setLayout(new BorderLayout());

        model = new DefaultTableModel(new String[]{
                "Ticket ID", "eNCF", "Status", "Signed XML", "Response XML", "Retries", "Last Error"
        }, 0);

        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JButton btnRefresh = new JButton("🔄 Refresh");
        JButton btnResend = new JButton("♻️ Resend Selected");

        btnRefresh.addActionListener(e -> loadTable());
        btnResend.addActionListener(e -> resendSelected());

        JPanel buttons = new JPanel();
        buttons.add(btnRefresh);
        buttons.add(btnResend);
        add(buttons, BorderLayout.SOUTH);

        loadTable();
    }

    private void loadTable() {
        model.setRowCount(0);
        List<ECFLogEntry> failed = logger.getFailedInvoices();

        for (ECFLogEntry entry : failed) {
            model.addRow(new Object[]{
                    entry.ticketId,
                    entry.encf,
                    entry.status,
                    entry.xmlSignedPath,
                    entry.xmlResponsePath,
                    entry.retries,
                    entry.errorMessage
            });
        }
    }

    private void resendSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a row to resend.");
            return;
        }

        String encf = (String) model.getValueAt(row, 1);
        String signedPath = (String) model.getValueAt(row, 3);

        try {
            // Manual resend call
            String responsePath = new com.unicenta.pos.einvoice.ECFTransport().sendToDGII(signedPath);

            ECFLogEntry entry = new ECFLogEntry();
            entry.ticketId = (String) model.getValueAt(row, 0);
            entry.encf = encf;
            entry.status = "CONFIRMED";
            entry.xmlSignedPath = signedPath;
            entry.xmlResponsePath = responsePath;
            entry.retries = ((Integer) model.getValueAt(row, 5)) + 1;

            logger.logStatus(entry);
            loadTable();

            JOptionPane.showMessageDialog(this, "Resent successfully!");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Resend failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

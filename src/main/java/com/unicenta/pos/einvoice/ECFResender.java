package com.unicenta.pos.einvoice;

import com.unicenta.pos.einvoice.model.ECFLogEntry;

import java.sql.Connection;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ECFResender {

    private final Connection connection;
    private final ECFLogger logger;
    private final Timer timer = new Timer("ECF-Resender", true);

    public ECFResender(Connection conn) {
        this.connection = conn;
        this.logger = new ECFLogger(conn);
    }

    public void start(long intervalMillis) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                retryFailedInvoices();
            }
        }, 0, intervalMillis);
    }

    private void retryFailedInvoices() {
        List<ECFLogEntry> failedInvoices = logger.getFailedInvoices();
        ECFTransport transport = new ECFTransport();

        for (ECFLogEntry entry : failedInvoices) {
            try {
                String newResponsePath = transport.sendToDGII(entry.xmlSignedPath);

                // Success: update status to CONFIRMED
                entry.status = "CONFIRMED";
                entry.xmlResponsePath = newResponsePath;
                entry.timestampResponse = new Date();
                entry.retries += 1;
                logger.logStatus(entry);

                System.out.println("[ECFResender] Success: " + entry.encf);

            } catch (Exception ex) {
                entry.retries += 1;
                entry.errorMessage = ex.getMessage();
                entry.timestampResponse = new Date();

                // Retry up to 5 times, else mark as GAVE_UP
                if (entry.retries >= 5) {
                    entry.status = "GAVE_UP";
                } else {
                    entry.status = "FAILED";
                }
                logger.logStatus(entry);

                System.err.println("[ECFResender] Retry failed for: " + entry.encf + " :: " + ex.getMessage());
            }
        }
    }

    public void stop() {
        timer.cancel();
    }
}

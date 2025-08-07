package com.unicenta.pos.einvoice;

import com.unicenta.pos.ticket.TicketInfo;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ECFTypeResolver {

    public static String resolveTipoeCF(TicketInfo ticket) {
        // Determine refund
        boolean isRefund = ticket.getTotal() < 0;

        // Export logic (to be improved later from customer info)
        boolean isExport = false; // Placeholder until export flag exists

        // Credit logic (if any line is "deferred payment")
        boolean isCredit = ticket.getPayments().stream()
            .anyMatch(p -> p.getName().toLowerCase().contains("credit") || p.getName().toLowerCase().contains("deferred"));

        if (isRefund) return "34"; // Nota de Crédito Electrónica
        if (isExport) return "46"; // Exportaciones Electrónica
        if (isCredit) return "31"; // Factura de Crédito Fiscal Electrónica

        return "32"; // Default: Factura de Consumo Electrónica
    }

    public static String generateNCF(String tipoeCF) {
        String rnc = ECFConfig.get("issuer.rnc");
        String sequence = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        return "E" + tipoeCF + rnc + sequence.substring(sequence.length() - 10);
    }
}

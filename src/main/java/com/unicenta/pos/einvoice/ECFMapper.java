package com.unicenta.pos.einvoice;

import com.unicenta.pos.ticket.TicketInfo;
import com.unicenta.pos.ticket.TicketLineInfo;
import com.unicenta.pos.forms.AppView;

import java.util.ArrayList;
import java.util.List;

public class ECFMapper {

    public static ECFData fromTicket(TicketInfo ticket, AppView app) {
        ECFData ecf = new ECFData();

        // 🛡 Validate ENCF presence
        String encf = ticket.getENCF();
        if (encf == null || encf.length() < 3) {
            throw new IllegalStateException("[ECF MAPPER] ENCF missing on ticket ID " + ticket.getTicketId());
        }

        // 📋 Set basic header fields
        ecf.seteNCF(encf);
        ecf.setTipoECF(encf.substring(0, 3)); // First 3 letters: E31 / E32 / E44 etc
        ecf.setFechaEmision(ticket.getDate());


        // 🏢 Emisor details
        ecf.setRncEmisor(ECFConfig.get("issuer.rnc", "131935486")); // from config
        ecf.setNombreComercial(ECFConfig.get("issuer.nombrecomercial", "Harbans International SRL"));

        // 👥 Receptor logic (for E31, E44)
if (encf.startsWith("E31") || encf.startsWith("E44")) {
    if (ticket.getCustomer() != null) {
        // Sanitize RNC (remove dashes)
        String rnc = ticket.getCustomer().getTaxid();
        if (rnc == null || rnc.trim().isEmpty()) {
            rnc = "000000000";
        } else {
            rnc = rnc.replaceAll("-", "");
        }
        ecf.setRncReceptor(rnc);
        ecf.setRazonSocialReceptor(ticket.getCustomer().getName() != null ? ticket.getCustomer().getName() : "CLIENTE");
    }
    ecf.setTipoIngresos("01"); // Default value for now
}

        // 💵 TipoPago
        String tipoPago = "01"; // Default cash
        if (!ticket.getPayments().isEmpty()) {
            String paymentMethod = ticket.getPayments().get(0).getName().toLowerCase();
            switch (paymentMethod) {
                case "cash":
                    tipoPago = "01";
                    break;
                case "magcard":
                case "card":
                    tipoPago = "02";
                    break;
                case "cheque":
                    tipoPago = "03";
                    break;
                case "debt":
                    tipoPago = "04";
                    break;
                default:
                    tipoPago = "99"; // Unknown
                    break;
            }
        }
        ecf.setTipoPago(tipoPago);
        boolean isRegimenEspecial = false;
         if (encf.startsWith("E44"))
         {
             isRegimenEspecial = true;
         }
         // Items and Totals
        List<ECFItem> items = new ArrayList<>();
        double totalITBIS = 0.0;
        int line = 1;
        for (TicketLineInfo lineInfo : ticket.getLines()) {
            ECFItem item = new ECFItem();
            item.setNumeroLinea(line++);
            item.setNombreItem(lineInfo.getProductName());
            item.setCantidadItem(lineInfo.getMultiply());
            item.setPrecioItem(lineInfo.getPrice());
            item.setMontoItem(lineInfo.getPrice() * lineInfo.getMultiply());
            if (isRegimenEspecial) {
                item.setITBIS(0.00);
            } else {
                item.setITBIS(lineInfo.getTax()); // Correct real tax
                totalITBIS += lineInfo.getTax();
            }
            items.add(item);
        }
        ecf.setItems(items);

        ecf.setMontoTotal(ticket.getTotal());
        ecf.setTotalITBIS(isRegimenEspecial ? 0.00 : totalITBIS);

        return ecf;
    }
}
package com.unicenta.pos.einvoice;

import java.text.SimpleDateFormat;
import java.util.List;

public class ECFXMLBuilder {

    public static String buildECFXML(ECFData ecfData) throws Exception {
        String tipoECF = ecfData.getTipoECF(); // E31, E32, E44
        switch (tipoECF) {
            case "E31":
                return buildFacturaCredito(ecfData);
            case "E32":
                return buildFacturaConsumo(ecfData);
            case "E44":
                return buildRegimenEspecial(ecfData);
            default:
                throw new IllegalArgumentException("Unsupported ECF Type: " + tipoECF);
        }
    }

    private static String buildFacturaCredito(ECFData ecf) {
        return buildCommonECF(ecf, "31");
    }

    private static String buildFacturaConsumo(ECFData ecf) {
        return buildCommonECF(ecf, "32");
    }

    private static String buildRegimenEspecial(ECFData ecf) {
        return buildCommonECF(ecf, "44");
    }

    private static String buildCommonECF(ECFData ecf, String tipoECFCode) {
        StringBuilder xml = new StringBuilder();

        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xml.append("<eCF xmlns=\"http://www.dgii.gov.do/eCF\">");

        // 🧾 Header
        xml.append("<Encabezado>");
        xml.append("<Version>1.0</Version>");
        xml.append("<TipoeCF>").append(tipoECFCode).append("</TipoeCF>");
        xml.append("<eNCF>").append(safe(ecf.geteNCF())).append("</eNCF>");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
String fechaFormatted = sdf.format(ecf.getFechaEmision());
xml.append("<FechaEmision>").append(fechaFormatted).append("</FechaEmision>");

        if (ecf.getTipoIngresos() != null) {
            xml.append("<TipoIngresos>").append(safe(ecf.getTipoIngresos())).append("</TipoIngresos>");
        }
        xml.append("<TipoPago>").append(safe(ecf.getTipoPago())).append("</TipoPago>");
        xml.append("<Moneda>").append("DOP").append("</Moneda>"); // 🔥 Added Moneda
        xml.append("</Encabezado>");

        // 🏢 Emisor
        xml.append("<Emisor>");
        xml.append("<RNCEmisor>").append(safe(ecf.getRncEmisor())).append("</RNCEmisor>");
        xml.append("<RazonSocialEmisor>").append(safe(ecf.getNombreComercial())).append("</RazonSocialEmisor>");
        xml.append("</Emisor>");

        // 👥 Receptor (optional)
        if (ecf.getRncReceptor() != null && ecf.getRazonSocialReceptor() != null) {
            xml.append("<Receptor>");
            xml.append("<RNCReceptor>").append(safe(ecf.getRncReceptor())).append("</RNCReceptor>");
            xml.append("<RazonSocialReceptor>").append(safe(ecf.getRazonSocialReceptor())).append("</RazonSocialReceptor>");
            xml.append("</Receptor>");
        }

        
        
        // 📦 Items
        xml.append("<DetallesItems>");
        List<ECFItem> items = ecf.getItems();
        for (ECFItem item : items) {
            xml.append("<DetalleItem>");
            xml.append("<NumeroLinea>").append(item.getNumeroLinea()).append("</NumeroLinea>");
            xml.append("<NombreItem>").append(safe(item.getNombreItem())).append("</NombreItem>");
            xml.append("<CantidadItem>").append(item.getCantidadItem()).append("</CantidadItem>");
            xml.append("<PrecioItem>").append(item.getPrecioItem()).append("</PrecioItem>");
            xml.append("<MontoItem>").append(item.getMontoItem()).append("</MontoItem>");
            xml.append("<ITBISItem>").append(String.format("%.2f", item.getITBIS())).append("</ITBISItem>");
            xml.append("</DetalleItem>");
        }
        xml.append("</DetallesItems>");

        // 🧮 Totales
        xml.append("<Totales>");
        xml.append("<MontoTotal>").append(ecf.getMontoTotal()).append("</MontoTotal>");
        // 🔥 Add TotalITBIS
    double totalItbis = ecf.getItems().stream().mapToDouble(ECFItem::getITBIS).sum();
    xml.append("<TotalITBIS>").append(String.format("%.2f", totalItbis)).append("</TotalITBIS>");
    
        xml.append("</Totales>");

        // 🔏 Signature placeholder
        xml.append("<Signature></Signature>");

        xml.append("</eCF>");
        return xml.toString();
    }

    // 🔒 Null-safe helper
    private static String safe(String value) {
        return value == null ? "" : value;
    }
}

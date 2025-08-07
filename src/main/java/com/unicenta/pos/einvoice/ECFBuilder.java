package com.unicenta.pos.einvoice;

import com.unicenta.pos.ticket.TicketInfo;
import com.unicenta.pos.ticket.TicketLineInfo;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ECFBuilder {

    public String generateECFXML(TicketInfo ticket, String outputDir) throws Exception {
        // Resolve dynamic e-CF type and e-NCF
        String tipoeCF = ECFTypeResolver.resolveTipoeCF(ticket);
        String encf = ECFTypeResolver.generateNCF(tipoeCF);

        String fileName = "ecf_" + encf + ".xml";
        String fullPath = outputDir + File.separator + fileName;

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        Document doc = docBuilder.newDocument();
        Element root = doc.createElement("eCF");
        doc.appendChild(root);

        // Encabezado
        Element encabezado = doc.createElement("Encabezado");
        appendTextNode(doc, encabezado, "TipoeCF", tipoeCF);
        appendTextNode(doc, encabezado, "eNCF", encf);
        appendTextNode(doc, encabezado, "FechaEmision", new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
        root.appendChild(encabezado);

        // Comprador (can be improved later from customer record)
        Element comprador = doc.createElement("Comprador");
        appendTextNode(doc, comprador, "RNCComprador", "000000000");
        appendTextNode(doc, comprador, "NombreRazonSocialComprador", "Cliente Final");
        root.appendChild(comprador);

        // Detalle
        Element detalle = doc.createElement("Detalle");
        int lineNum = 1;
        for (TicketLineInfo line : ticket.getLines()) {
            Element item = doc.createElement("Item");
            appendTextNode(doc, item, "NumeroLinea", String.valueOf(lineNum++));
            appendTextNode(doc, item, "Descripcion", line.getProductName());
            appendTextNode(doc, item, "Cantidad", String.format("%.2f", line.getMultiply()));
            appendTextNode(doc, item, "PrecioUnitario", String.format("%.2f", line.getPrice()));
            appendTextNode(doc, item, "ITBIS", String.format("%.2f", line.getTax()));
            detalle.appendChild(item);
        }
        root.appendChild(detalle);

        // Totales
        Element totales = doc.createElement("Totales");
        appendTextNode(doc, totales, "TotalBruto", String.format("%.2f", ticket.getSubTotal()));
        appendTextNode(doc, totales, "TotalITBIS", String.format("%.2f", ticket.getTax()));
        appendTextNode(doc, totales, "TotalNeto", String.format("%.2f", ticket.getTotal()));
        root.appendChild(totales);

        // Write XML file
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(fullPath));
        transformer.transform(source, result);

        return fullPath;
    }

    private void appendTextNode(Document doc, Element parent, String tag, String value) {
        Element node = doc.createElement(tag);
        node.appendChild(doc.createTextNode(value));
        parent.appendChild(node);
    }
}

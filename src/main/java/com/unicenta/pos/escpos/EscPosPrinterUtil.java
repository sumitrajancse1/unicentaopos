package com.unicenta.pos.escpos;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.PrinterName;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/**
 * Utility to send ESC/POS commands to the thermal printer.
 * Jake style optimized 🥷💥
 */
public class EscPosPrinterUtil {

    private static final byte[] CENTER_ALIGN = { 0x1B, 'a', 0x01 };  // Center align
    private static final byte[] LEFT_ALIGN   = { 0x1B, 'a', 0x00 };  // Left align
    private static final byte[] QR_MODEL     = { 0x1D, 0x28, 0x6B, 0x04, 0x00, 0x31, 0x41, 0x32, 0x00 };
    private static final byte[] QR_SIZE      = { 0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x43, 0x06 };
    private static final byte[] QR_ERROR     = { 0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x45, 0x30 };
    private static final byte[] CUT_PAPER    = { 0x1D, 'V', 1 };

    private static final String PRINTER_NAME = "POS-80"; // Your real printer name (change if needed)

    public void printQRCode(String data) throws Exception {
        byte[] qrData = buildQrData(data);
        sendToPrinter(qrData);
    }

    public void printCenteredText(String text) throws Exception {
        byte[] alignCenter = CENTER_ALIGN;
        byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
        byte[] lineFeed = new byte[] { 0x0A };

        byte[] fullData = new byte[alignCenter.length + textBytes.length + lineFeed.length];
        System.arraycopy(alignCenter, 0, fullData, 0, alignCenter.length);
        System.arraycopy(textBytes, 0, fullData, alignCenter.length, textBytes.length);
        System.arraycopy(lineFeed, 0, fullData, alignCenter.length + textBytes.length, lineFeed.length);

        sendToPrinter(fullData);
    }

    public void feedAndCut() throws Exception {
        sendToPrinter(new byte[] { 0x0A, 0x0A, 0x0A, 0x0A });
        sendToPrinter(CUT_PAPER);
    }

    private byte[] buildQrData(String data) {
        byte[] model = QR_MODEL;
        byte[] size = QR_SIZE;
        byte[] error = QR_ERROR;

        byte[] storeQRCode = createStoreQRCode(data);
        byte[] printQRCode = new byte[] { 0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x51, 0x30 };

        int totalLength = model.length + size.length + error.length + storeQRCode.length + printQRCode.length;
        byte[] qrCommand = new byte[totalLength];

        int pos = 0;
        System.arraycopy(model, 0, qrCommand, pos, model.length);
        pos += model.length;
        System.arraycopy(size, 0, qrCommand, pos, size.length);
        pos += size.length;
        System.arraycopy(error, 0, qrCommand, pos, error.length);
        pos += error.length;
        System.arraycopy(storeQRCode, 0, qrCommand, pos, storeQRCode.length);
        pos += storeQRCode.length;
        System.arraycopy(printQRCode, 0, qrCommand, pos, printQRCode.length);

        return qrCommand;
    }

    private byte[] createStoreQRCode(String data) {
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        int length = bytes.length + 3;
        byte pL = (byte) (length % 256);
        byte pH = (byte) (length / 256);

        byte[] command = new byte[bytes.length + 8];
        command[0] = 0x1D;
        command[1] = 0x28;
        command[2] = 0x6B;
        command[3] = pL;
        command[4] = pH;
        command[5] = 0x31;
        command[6] = 0x50;
        command[7] = 0x30;
        System.arraycopy(bytes, 0, command, 8, bytes.length);

        return command;
    }

    private void sendToPrinter(byte[] data) throws Exception {
        DocPrintJob job = findPrintService(PRINTER_NAME).createPrintJob();
        Doc doc = new SimpleDoc(new ByteArrayInputStream(data), DocFlavor.BYTE_ARRAY.AUTOSENSE, null);
        job.print(doc, new HashPrintRequestAttributeSet());
    }

    private PrintService findPrintService(String printerName) throws Exception {
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService service : services) {
            if (service.getName().equalsIgnoreCase(printerName)) {
                return service;
            }
        }
        throw new Exception("Printer not found: " + printerName);
    }
}

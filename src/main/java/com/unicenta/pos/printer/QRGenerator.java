package com.unicenta.pos.printer;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * QRGenerator - Thermal Centered Version (v2)
 * Developed by Jake 👾
 */
public class QRGenerator {

    /**
     * Generates a QR code image centered on a canvas.
     * 
     * @param data The text/data to encode into the QR.
     * @param qrWidth The width of the QR code block itself.
     * @param qrHeight The height of the QR code block itself.
     * @return A BufferedImage with QR centered on canvas.
     * @throws WriterException
     */
    public static BufferedImage generateQRImage(String data, int qrWidth, int qrHeight) throws WriterException {
        int canvasWidth = 384; // 🛡️ Printer paper width in pixels (adjust if needed)
        int canvasHeight = qrHeight; // Same height as QR for now

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

        // Step 1: Generate QR
        BitMatrix bitMatrix = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, qrWidth, qrHeight, hints);
        BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

        // Step 2: Create a blank white canvas
        BufferedImage canvas = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas.createGraphics();

        g.setColor(java.awt.Color.WHITE);
        g.fillRect(0, 0, canvasWidth, canvasHeight);

        // Step 3: Center QR on the canvas
        int xOffset = (canvasWidth - qrWidth) / 2;
        g.drawImage(qrImage, xOffset, 0, null);

        g.dispose();
        return canvas;
    }
    public static BufferedImage generateQR(String data, int size) throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

        BitMatrix bitMatrix = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, size, size, hints);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }
    

    /**
     * Overloaded method - Default size (200x200 QR on 384px canvas)
     * @param data
     * @return 
     * @throws com.google.zxing.WriterException 
     */
    public static BufferedImage generateQRImage(String data) throws WriterException {
        return generateQR(data, 150);
       
    }
}

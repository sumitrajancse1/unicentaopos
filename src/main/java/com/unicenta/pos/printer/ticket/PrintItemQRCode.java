package com.unicenta.pos.printer.ticket;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import java.awt.Color;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

class PrintItemQRCode implements PrintItem {
    private final String position;
    private final String data;

    public PrintItemQRCode(String position, String data) {
        this.position = position;
        this.data = data;
    }

    @Override
    public int getHeight() {
        return 140; // QR code height
    }

    @Override
    public void draw(Graphics2D g2d, int x, int y, int width) {
        try {
            QRCodeWriter qrWriter = new QRCodeWriter();
            BitMatrix matrix = qrWriter.encode(data, BarcodeFormat.QR_CODE, 140, 140);
            int qrWidth = matrix.getWidth();
            int qrHeight = matrix.getHeight();

            // Step 1: Create QR image
            BufferedImage qrImage = new BufferedImage(qrWidth, qrHeight, BufferedImage.TYPE_INT_RGB);
            for (int i = 0; i < qrWidth; i++) {
                for (int j = 0; j < qrHeight; j++) {
                    qrImage.setRGB(i, j, matrix.get(i, j) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }

            // Step 2: Create a full-width white canvas
            BufferedImage fullImage = new BufferedImage(width, qrHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D gFull = fullImage.createGraphics();
            gFull.setColor(Color.WHITE);
            gFull.fillRect(0, 0, width, qrHeight);

            // Step 3: Draw QR centered on white canvas
            int xCentered = (width - qrWidth) / 2;
            gFull.drawImage(qrImage, xCentered, 0, null);
            gFull.dispose();

            // Step 4: Draw fullImage to ticket
            g2d.drawImage(fullImage, 0, y, null);

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
}

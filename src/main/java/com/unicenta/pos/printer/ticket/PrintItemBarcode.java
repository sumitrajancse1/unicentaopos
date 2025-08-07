package com.unicenta.pos.printer.ticket;

import java.awt.Graphics2D;

public class PrintItemBarcode implements PrintItem {
    private final String type;
    private final String position;
    private final String code;
    private final double scale;

    public PrintItemBarcode(String type, String position, String code, double scale) {
        this.type = type;
        this.position = position;
        this.code = code;
        this.scale = scale;
    }

    @Override
    public int getHeight() {
        return 40; // Typical barcode height (You can customize)
    }

    @Override
    public void draw(Graphics2D g2d, int x, int y, int width) {
        // 🛠 Placeholder drawing text instead of real barcode for now
        g2d.drawString("[Barcode: " + code + "]", x, y + 20);
    }
}

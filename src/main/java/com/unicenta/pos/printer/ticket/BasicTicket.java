//    uniCenta oPOS  - Touch Friendly Point Of Sale
//    Copyright (c) 2009-2018 uniCenta & previous Openbravo POS works
//    https://unicenta.com
//
//    This file is part of uniCenta oPOS
//
//    uniCenta oPOS is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    uniCenta oPOS is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with uniCenta oPOS.  If not, see <http://www.gnu.org/licenses/>.

package com.unicenta.pos.printer.ticket;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

public abstract class BasicTicket implements PrintItem {

    protected java.util.List<PrintItem> m_aCommands;
    protected PrintItemLine pil;
    protected int m_iBodyHeight;

    public BasicTicket() {
        m_aCommands = new ArrayList<>();
        pil = null;
        m_iBodyHeight = 0;
    }

    protected abstract Font getBaseFont();

    protected abstract int getFontHeight();

    protected abstract double getImageScale();

    @Override
    public int getHeight() {
        return m_iBodyHeight;
    }

    @Override
    public void draw(Graphics2D g2d, int x, int y, int width) {
        int currenty = y;
        for (PrintItem pi : m_aCommands) {
            pi.draw(g2d, x, currenty, width);
            currenty += pi.getHeight();
        }
        saveTicket(g2d, x, y, width);
    }

    private void saveTicket(Graphics2D g2d, int x, int y, int width) {
        int receiptHeight = 5;
        for (PrintItem pi : m_aCommands) {
            receiptHeight += pi.getHeight();
        }

        BufferedImage image = new BufferedImage(width + 10, receiptHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D ig2d = image.createGraphics();
        int currenty = y;

        for (PrintItem pi : m_aCommands) {
            if (pi instanceof PrintItemImage) {
                ((PrintItemImage) pi).setImage(negative(((PrintItemImage) pi).getImage()));
            }
            pi.draw(ig2d, x, currenty, width);
            currenty += pi.getHeight();
        }

        try {
            ImageIO.write(negative(image), "png", new File(System.getProperty("user.home") + "/receipt.png"));
        } catch (Exception e) {
            System.out.println("Error serializing receipt image: " + e);
        }
    }

    private BufferedImage negative(BufferedImage img) {
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                int rgba = img.getRGB(x, y);
                Color col = new Color(rgba, true);
                col = new Color(255 - col.getRed(), 255 - col.getGreen(), 255 - col.getBlue());
                img.setRGB(x, y, col.getRGB());
            }
        }
        return img;
    }

    public java.util.List<PrintItem> getCommands() {
        return m_aCommands;
    }

    public void printImage(BufferedImage image) {
        PrintItem pi = new PrintItemImage(image, getImageScale());
        m_aCommands.add(pi);
        m_iBodyHeight += pi.getHeight();
    }

    public void printBarCode(String type, String position, String code) {
        PrintItem pi = new PrintItemBarcode(type, position, code, getImageScale());
        m_aCommands.add(pi);
        m_iBodyHeight += pi.getHeight();
    }

    public void beginLine(int iTextSize) {
        pil = new PrintItemLine(iTextSize, getBaseFont(), getFontHeight());
    }

    public void printText(int iStyle, String sText) {
        if (pil != null) {
            pil.addText(iStyle, sText);
        }
    }

    public void endLine() {
        if (pil != null) {
            m_aCommands.add(pil);
            m_iBodyHeight += pil.getHeight();
            pil = null;
        }
    }
}

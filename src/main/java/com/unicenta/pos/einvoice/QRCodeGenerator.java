package com.unicenta.pos.einvoice;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import java.nio.file.Path;
import java.nio.file.Paths;

public class QRCodeGenerator {

    public static String createQRCode(String encf, String outputDir) throws Exception {
        String baseUrl = ECFConfig.get("dgii.qr.base", "https://ecf.dgii.gov.do/consulta?eNCF=");
        String qrContent = baseUrl + encf;

        String fileName = "qr_" + encf + ".png";
        Path path = Paths.get(outputDir, fileName);

        BitMatrix matrix = new MultiFormatWriter().encode(qrContent, BarcodeFormat.QR_CODE, 200, 200);
        MatrixToImageWriter.writeToPath(matrix, "PNG", path);

        return path.toAbsolutePath().toString();
    }
}

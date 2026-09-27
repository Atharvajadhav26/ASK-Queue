package com.smartqueue.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class QrCodeService {

    private static final Logger log = LoggerFactory.getLogger(QrCodeService.class);
    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 300;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    /**
     * Generate a QR code image as PNG bytes for a queue.
     * The QR code encodes the queue details URL.
     */
    public byte[] generateQrCode(String queueId) {
        return generateQrCode(queueId, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Generate a QR code image with custom dimensions.
     */
    public byte[] generateQrCode(String queueId, int width, int height) {
        try {
            String queueUrl = getQueueLink(queueId);

            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 2);

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(queueUrl, BarcodeFormat.QR_CODE, width, height, hints);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            log.debug("Generated QR code for queue: {}", queueId);
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate QR code for queue: {}", queueId, e);
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

    /**
     * Get the public queue link for a given queue ID.
     */
    public String getQueueLink(String queueId) {
        return frontendUrl + "/queue/" + queueId;
    }
}

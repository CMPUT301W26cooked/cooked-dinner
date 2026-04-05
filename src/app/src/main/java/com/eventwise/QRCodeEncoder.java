package com.eventwise;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.HashMap;
import java.util.Map;

/**
 * QR Code encoder utility class
 * Author: Hao
 * Version: 1.0
 * Date: 2026-04-01
 */
public class QRCodeEncoder {

    private String contents;
    private int width;
    private int height;
    private ErrorCorrectionLevel errorCorrection;

    public QRCodeEncoder(String contents, Integer width, Integer height, ErrorCorrectionLevel errorCorrection) {
        this.contents = contents;
        this.width = width != null ? width : 400;
        this.height = height != null ? height : 400;
        this.errorCorrection = errorCorrection != null ? errorCorrection : ErrorCorrectionLevel.M;
    }

    public Bitmap encodeAsBitmap() throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, errorCorrection);
        hints.put(EncodeHintType.MARGIN, 1);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(contents, BarcodeFormat.QR_CODE, width, height, hints);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        return bitmap;
    }
}
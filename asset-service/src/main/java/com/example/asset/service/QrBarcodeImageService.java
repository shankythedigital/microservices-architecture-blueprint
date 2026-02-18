package com.example.asset.service;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.GenericMultipleBarcodeReader;
import com.google.zxing.multi.MultipleBarcodeReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.*;

/**
 * ✅ QrBarcodeImageService
 * Decodes QR codes and barcodes from images using ZXing.
 * Supports QR Code, EAN-13, UPC-A, Code 128, Code 39, Data Matrix, etc.
 */
@Service
public class QrBarcodeImageService {

    private static final Logger log = LoggerFactory.getLogger(QrBarcodeImageService.class);

    private static final Set<String> IMAGE_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif",
            "image/bmp", "image/webp"
    );

    /**
     * Decode QR code or barcode from image file.
     * Returns the first decoded text, or empty if none found.
     */
    public Optional<String> decodeFromImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("⚠️ Empty or null image file");
            return Optional.empty();
        }

        String contentType = file.getContentType();
        if (contentType != null && !IMAGE_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            log.warn("⚠️ Unsupported image type: {}", contentType);
        }

        try (InputStream is = file.getInputStream()) {
            BufferedImage image = ImageIO.read(is);
            if (image == null) {
                log.warn("⚠️ Could not read image - invalid format");
                return Optional.empty();
            }
            return decodeFromBufferedImage(image);
        } catch (Exception e) {
            log.error("❌ Failed to decode image: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Decode QR code or barcode from BufferedImage.
     * Tries multiple readers (QR first, then barcodes).
     */
    public Optional<String> decodeFromBufferedImage(BufferedImage image) {
        if (image == null) return Optional.empty();

        try {
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            MultiFormatReader reader = new MultiFormatReader();
            Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            hints.put(DecodeHintType.POSSIBLE_FORMATS, Arrays.asList(
                    BarcodeFormat.QR_CODE,
                    BarcodeFormat.EAN_13, BarcodeFormat.EAN_8,
                    BarcodeFormat.UPC_A, BarcodeFormat.UPC_E,
                    BarcodeFormat.CODE_128, BarcodeFormat.CODE_39, BarcodeFormat.CODE_93,
                    BarcodeFormat.DATA_MATRIX, BarcodeFormat.AZTEC, BarcodeFormat.PDF_417,
                    BarcodeFormat.ITF, BarcodeFormat.CODABAR
            ));

            Result result = reader.decode(bitmap, hints);
            if (result != null && result.getText() != null && !result.getText().isBlank()) {
                log.info("✅ Decoded {} from image: '{}'", result.getBarcodeFormat(), 
                        result.getText().length() > 50 ? result.getText().substring(0, 50) + "..." : result.getText());
                return Optional.of(result.getText().trim());
            }
        } catch (NotFoundException e) {
            log.debug("No QR/barcode found in image: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("⚠️ Decode failed: {}", e.getMessage());
        }

        return Optional.empty();
    }

    /**
     * Decode all QR codes / barcodes from image (if multiple).
     * Returns list of decoded texts. Empty if none found.
     */
    public List<String> decodeAllFromImage(MultipartFile file) {
        if (file == null || file.isEmpty()) return Collections.emptyList();

        try (InputStream is = file.getInputStream()) {
            BufferedImage image = ImageIO.read(is);
            if (image == null) return Collections.emptyList();
            return decodeAllFromBufferedImage(image);
        } catch (Exception e) {
            log.error("❌ Failed to decode image: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<String> decodeAllFromBufferedImage(BufferedImage image) {
        List<String> results = new ArrayList<>();
        try {
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            hints.put(DecodeHintType.POSSIBLE_FORMATS, Arrays.asList(
                    BarcodeFormat.QR_CODE,
                    BarcodeFormat.EAN_13, BarcodeFormat.EAN_8,
                    BarcodeFormat.UPC_A, BarcodeFormat.UPC_E,
                    BarcodeFormat.CODE_128, BarcodeFormat.CODE_39
            ));

            MultipleBarcodeReader multiReader = new GenericMultipleBarcodeReader(new MultiFormatReader());
            Result[] decoded = multiReader.decodeMultiple(bitmap, hints);
            if (decoded != null) {
                for (Result r : decoded) {
                    if (r != null && r.getText() != null && !r.getText().isBlank()) {
                        results.add(r.getText().trim());
                    }
                }
            }
        } catch (NotFoundException e) {
            log.debug("No barcodes found: {}", e.getMessage());
        } catch (Exception e) {
            log.debug("Multiple decode not available, falling back to single: {}", e.getMessage());
            decodeFromBufferedImage(image).ifPresent(results::add);
        }
        return results;
    }
}

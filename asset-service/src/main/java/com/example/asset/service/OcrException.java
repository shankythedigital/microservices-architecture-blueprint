package com.example.asset.service;

/**
 * Checked exception for image OCR failures (external {@code tesseract} process).
 */
public class OcrException extends Exception {

    public OcrException(String message) {
        super(message);
    }

    public OcrException(String message, Throwable cause) {
        super(message, cause);
    }
}

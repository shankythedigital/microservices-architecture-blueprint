package com.example.asset.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Tesseract OCR Configuration Properties
 * Reads from application.yml under 'tesseract' section
 */
@Configuration
@ConfigurationProperties(prefix = "tesseract")
public class TesseractConfig {
    
    private boolean enabled = true;
    /** When true, call tesseract as external process (no native lib). Recommended on macOS Ventura. */
    private boolean useProcess = true;
    private String dataPath;
    private String executablePath;
    private String language = "eng";
    private int pageSegMode = 1;
    private int ocrEngineMode = 1;

    public boolean isUseProcess() {
        return useProcess;
    }

    public void setUseProcess(boolean useProcess) {
        this.useProcess = useProcess;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public String getExecutablePath() {
        return executablePath;
    }

    public void setExecutablePath(String executablePath) {
        this.executablePath = executablePath;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getPageSegMode() {
        return pageSegMode;
    }

    public void setPageSegMode(int pageSegMode) {
        this.pageSegMode = pageSegMode;
    }

    public int getOcrEngineMode() {
        return ocrEngineMode;
    }

    public void setOcrEngineMode(int ocrEngineMode) {
        this.ocrEngineMode = ocrEngineMode;
    }
}


package com.example.asset.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * MultipartFile implementation backed by a byte array.
 * Used when document is received as base64 in raw JSON body.
 */
public class ByteArrayMultipartFile implements MultipartFile {

    private final byte[] content;
    private final String name;
    private final String originalFilename;
    private final String contentType;

    public ByteArrayMultipartFile(byte[] content, String name, String originalFilename) {
        this(content, name, originalFilename, "application/octet-stream");
    }

    public ByteArrayMultipartFile(byte[] content, String name, String originalFilename, String contentType) {
        this.content = content != null ? content : new byte[0];
        this.name = name != null ? name : "document";
        this.originalFilename = originalFilename != null ? originalFilename : "document";
        this.contentType = contentType != null ? contentType : "application/octet-stream";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getOriginalFilename() {
        return originalFilename;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        return content.length == 0;
    }

    @Override
    public long getSize() {
        return content.length;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return content;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(content);
    }

    @Override
    public void transferTo(File dest) throws IOException {
        Files.write(dest.toPath(), content);
    }
}

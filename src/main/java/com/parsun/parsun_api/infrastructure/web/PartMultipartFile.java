package com.parsun.parsun_api.infrastructure.web;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.Part;

public class PartMultipartFile implements MultipartFile {

    private final Part part;
    private final String filename;

    public PartMultipartFile(Part part) {
        this.part = part;
        this.filename = extractFilename(part);
    }

    private String extractFilename(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        if (contentDisposition == null) {
            return null;
        }
        for (String token : contentDisposition.split(";")) {
            token = token.trim();
            if (token.startsWith("filename=")) {
                String name = token.substring(9);
                if (name.startsWith("\"") && name.endsWith("\"")) {
                    name = name.substring(1, name.length() - 1);
                }
                return name;
            }
        }
        return null;
    }

    @Override
    public String getName() {
        return part.getName();
    }

    @Override
    public String getOriginalFilename() {
        return filename;
    }

    @Override
    public String getContentType() {
        return part.getContentType();
    }

    @Override
    public boolean isEmpty() {
        return part.getSize() == 0;
    }

    @Override
    public long getSize() {
        return part.getSize();
    }

    @Override
    public byte[] getBytes() throws IOException {
        try (InputStream is = part.getInputStream()) {
            return is.readAllBytes();
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return part.getInputStream();
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        part.write(dest.getAbsolutePath());
    }

    @Override
    public void transferTo(Path dest) throws IOException, IllegalStateException {
        try (InputStream is = part.getInputStream()) {
            Files.copy(is, dest, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}

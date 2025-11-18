package com.budgetwise.service;

import com.budgetwise.config.ExternalApiConfig;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class DropboxService {
    
    private final ExternalApiConfig apiConfig;
    
    private DbxClientV2 getDropboxClient() {
        DbxRequestConfig config = DbxRequestConfig.newBuilder("budgetwise/1.0").build();
        return new DbxClientV2(config, apiConfig.getDropboxApiKey());
    }
    
    /**
     * Upload file to Dropbox
     */
    public String uploadFile(byte[] fileContent, String fileName, String folder) {
        try {
            DbxClientV2 client = getDropboxClient();
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String dropboxPath = String.format("/%s/%s_%s", folder, timestamp, fileName);
            
            try (InputStream in = new ByteArrayInputStream(fileContent)) {
                FileMetadata metadata = client.files().uploadBuilder(dropboxPath)
                    .withMode(WriteMode.OVERWRITE)
                    .uploadAndFinish(in);
                
                log.info("File uploaded to Dropbox: {}", metadata.getPathDisplay());
                return metadata.getPathDisplay();
            }
        } catch (DbxException | IOException e) {
            log.error("Error uploading file to Dropbox", e);
            throw new RuntimeException("Failed to upload file to Dropbox", e);
        }
    }
    
    /**
     * Backup user data to Dropbox
     */
    public String backupUserData(Long userId, byte[] backupData) {
        String fileName = String.format("user_%d_backup.json", userId);
        return uploadFile(backupData, fileName, "backups");
    }
    
    /**
     * Upload transaction export to Dropbox
     */
    public String uploadTransactionExport(Long userId, byte[] exportData, String format) {
        String fileName = String.format("user_%d_transactions.%s", userId, format);
        return uploadFile(exportData, fileName, "exports");
    }
    
    /**
     * Upload report to Dropbox
     */
    public String uploadReport(Long userId, byte[] reportData, String reportName) {
        String fileName = String.format("user_%d_%s.pdf", userId, reportName);
        return uploadFile(reportData, fileName, "reports");
    }
}

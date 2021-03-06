package com.example.demo;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;

/**
 * @author treetory@gmail.com
 */
@Service
public class FileStorageService {

    private static final Logger LOG = LoggerFactory.getLogger(FileStorageService.class);

    private final Path consentFormDataLocation;

    private final Gson gson;

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties, Gson gson) {

        this.consentFormDataLocation = Paths.get(fileStorageProperties.getWriteDir())
                .toAbsolutePath().normalize();

        this.gson = new Gson();

        LOG.debug("{}{}{}{}", System.lineSeparator(), System.lineSeparator(), this.consentFormDataLocation, System.lineSeparator());

        try {

            if (!Files.isDirectory(this.consentFormDataLocation)) {
                Files.createDirectories(this.consentFormDataLocation);
            }

        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String writeJSONFile(Map<String, Object> consentFormData) {

        String fileName ="";
        try {
            fileName = String.format("%s_%d.json", (String)((Map)consentFormData.get("FrmInfo")).get("FrmCd"), System.currentTimeMillis());
            Path targetLocation = this.consentFormDataLocation.resolve(fileName);
            // gson 으로는 8KB 밖에 안 쓰여짐... 왜 그런지는 모르겟음
            //gson.toJson(consentFormData, new FileWriter(targetLocation.toAbsolutePath().toString()));
            Files.write(targetLocation, gson.toJson(consentFormData).getBytes("UTF-8"), StandardOpenOption.CREATE_NEW);
            return "Success to write the consent form data to server.";
        } catch (IOException ie) {
            FileStorageException fse = new FileStorageException("Could not write file " + fileName + " into the [consentFormData] folder. Please try again!", ie);
            LOGPrint.printException(ie, IOException.class);
            return fse.getMessage();
        } catch (NullPointerException npe) {
            LOGPrint.printException(npe, IOException.class);
            return "Can't find [FrmInfo] from the consentFormData.";
        }

    }
}
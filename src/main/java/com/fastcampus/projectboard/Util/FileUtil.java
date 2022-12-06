package com.fastcampus.projectboard.Util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
@Service
public class FileUtil {
    public  String getExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    public String getFileName(String fileName) {
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    public String getFileNameWithUUID(String fileName) {
        return UUID.randomUUID().toString() + "_" + fileName;
    }
    public  File  createFile(String uploadPath, String fileName) {
        return new File(uploadPath, fileName);
    }

    public File getMultipartFileToFile(MultipartFile multipartFile) throws IOException {
        File file = new File(getFileNameWithUUID(multipartFile.getOriginalFilename()));
        multipartFile.transferTo(file);
        return file;
    }

    public File getFileFromFileDomain(com.fastcampus.projectboard.domain.File.FileDto fileDto) {
        return new File(fileDto.filePath()+"/"+fileDto.fileName());
    }


}

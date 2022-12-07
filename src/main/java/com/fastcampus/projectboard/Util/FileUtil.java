package com.fastcampus.projectboard.Util;

import com.fastcampus.projectboard.domain.SaveFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class FileUtil {
    private FileUtil() {
    }
    @Value("${com.example.upload.path.profileImg}")
    public static String uploadPath;



    public  static String getExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    public static String getFileName(String fileName) {
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    public static String getFileNameWithUUID(String fileName) {
        return UUID.randomUUID().toString() + "_" + fileName;
    }
    public  static File  createFile(String uploadPath, String fileName) {
        return new File(uploadPath, fileName);
    }

    public static File getMultipartFileToFile(MultipartFile multipartFile) throws IOException {
        File file = new File(uploadPath,getFileNameWithUUID(multipartFile.getOriginalFilename()));
        multipartFile.transferTo(file);
        return file;
    }

    public static File getFileFromFileDomain(SaveFile.FileDto fileDto) {
        return new File(fileDto.filePath());
    }


    public static void deleteFile(SaveFile.FileDto profileImg) {
        File file = getFileFromFileDomain(profileImg);
        if (file.exists()) {
            file.delete();
        }
    }
    public static SaveFile.FileDto getFileDtoFromMultiPartFile(MultipartFile multipartFile, String uploadUser) throws IOException {
        File file = getMultipartFileToFile(multipartFile);
        String fileName = file.getName();
        System.out.println(file.getPath()+"\n"+file.getAbsolutePath()+"\n"+file.getCanonicalPath());
        String fileType = getExtension(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        Long fileSize = multipartFile.getSize();
        return SaveFile.FileDto.builder()
                .fileName(fileName)
                .filePath("/Users/brinc/Desktop/brincestudy/fastcampus-project-board/src/main/resources/static/images/"+fileName)
                .fileType(fileType)
                .fileSize(fileSize)
                .uploadUser(uploadUser)
                .build();// TODO: 경로가 자꾸 null 로 입력되기 때문에 해결 방안을 찾아야함.
    }
}

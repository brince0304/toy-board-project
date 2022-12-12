package com.fastcampus.projectboard.Util;

import com.fastcampus.projectboard.domain.SaveFile;
import org.springframework.beans.factory.annotation.Value;
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
    public static String getFileNameWithUUID(String fileName) {
        return UUID.randomUUID().toString() + "_" + fileName;
    }

    public static File getMultipartFileToFile(MultipartFile multipartFile) throws IOException {
        File file = new File(uploadPath,getFileNameWithUUID(multipartFile.getOriginalFilename()));
        multipartFile.transferTo(file);
        return file;
    }

    public static File getFileFromSaveFile(SaveFile.SaveFileDto saveFileDto) {
        return new File(saveFileDto.filePath());
    }


    public static void deleteFile(SaveFile.SaveFileDto profileImg) {
        File file = getFileFromSaveFile(profileImg);
        if (file.exists()) {
            file.delete();
        }
    }
    public static SaveFile.SaveFileDto getFileDtoFromMultiPartFile(MultipartFile multipartFile, String uploadUser) throws IOException {
        String fileName = getMultipartFileToFile(multipartFile).getName();
        String fileType = getExtension(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        Long fileSize = multipartFile.getSize();
        return SaveFile.SaveFileDto.builder()
                .fileName(fileName)
                .filePath("/Users/brinc/Desktop/brincestudy/fastcampus-project-board/src/main/resources/static/images/"+fileName)
                .fileType(fileType)
                .fileSize(fileSize)
                .uploadUser(uploadUser)
                .build();// TODO: 경로가 자꾸 null 로 입력되기 때문에 해결 방안을 찾아야함.
    }
}

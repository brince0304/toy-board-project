package com.fastcampus.projectboard.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@RequiredArgsConstructor
@Getter
@Builder
public class File {
    @Id
    private Long id;
    @Setter
    @Column(nullable = false)
    private String fileName;
    @Setter
    @Column(nullable = false)
    private String filePath;
    @Setter
    @Column(nullable = false)
    private String fileType;
    @Setter
    @Column(nullable = false)
    private Long fileSize;
    @Setter
    @JoinColumn(nullable = false, name = "userId")
    @ManyToOne(optional = false)
    private UserAccount userAccount;

    public FileDto toDto() {
        return FileDto.builder()
                .id(id)
                .fileName(fileName)
                .filePath(filePath)
                .fileType(fileType)
                .fileSize(fileSize)
                .build();
    }


    public static class FileDto  {
        private final Long id;
        private final String fileName;
        private final String filePath;
        private final String fileType;
        private final Long fileSize;
        private final UserAccount userAccount;
        @Builder
        public FileDto(Long id, String fileName, String filePath, String fileType, Long fileSize, UserAccount userAccount) {
            this.id = id;
            this.fileName = fileName;
            this.filePath = filePath;
            this.fileType = fileType;
            this.fileSize = fileSize;
            this.userAccount = userAccount;
        }

        public FileDto(File file) {
            this.id = file.getId();
            this.fileName = file.getFileName();
            this.filePath = file.getFilePath();
            this.fileType = file.getFileType();
            this.fileSize = file.getFileSize();
            this.userAccount = file.getUserAccount();
        }

        public File toEntity() {
            return File.builder()
                    .id(id)
                    .fileName(fileName)
                    .filePath(filePath)
                    .fileType(fileType)
                    .fileSize(fileSize)
                    .userAccount(userAccount)
                    .build();
        }

    }

    public static class FileRequestDto implements Serializable{
        private final String fileName;
        private final String filePath;
        private final String fileType;
        private final Long fileSize;
        private final UserAccount userAccount;
        @Builder
        public FileRequestDto(String fileName, String filePath, String fileType, Long fileSize, UserAccount userAccount) {
            this.fileName = fileName;
            this.filePath = filePath;
            this.fileType = fileType;
            this.fileSize = fileSize;
            this.userAccount = userAccount;
        }

        public File toEntity() {
            return File.builder()
                    .fileName(fileName)
                    .filePath(filePath)
                    .fileType(fileType)
                    .fileSize(fileSize)
                    .userAccount(userAccount)
                    .build();
        }
    }
    public static class FileResponseDto implements Serializable{
        private final Long id;
        private final String fileName;
        private final String filePath;
        private final String fileType;
        private final Long fileSize;
        private final UserAccount userAccount;
        @Builder
        public FileResponseDto(Long id, String fileName, String filePath, String fileType, Long fileSize, UserAccount userAccount) {
            this.id = id;
            this.fileName = fileName;
            this.filePath = filePath;
            this.fileType = fileType;
            this.fileSize = fileSize;
            this.userAccount = userAccount;
        }

        public FileResponseDto(File file) {
            this.id = file.getId();
            this.fileName = file.getFileName();
            this.filePath = file.getFilePath();
            this.fileType = file.getFileType();
            this.fileSize = file.getFileSize();
            this.userAccount = file.getUserAccount();
        }
    }
}



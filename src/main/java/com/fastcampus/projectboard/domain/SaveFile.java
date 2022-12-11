package com.fastcampus.projectboard.domain;

import lombok.*;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;

import javax.persistence.*;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class SaveFile extends AuditingFields{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Setter
    @Column(nullable = false)
    private String fileName;
    @Setter
    @Column(nullable = false)
    private String filePath ;
    @Setter
    @Column(nullable = false)
    private String fileType;
    @Setter
    @Column(nullable = false)
    private Long fileSize;
    @Setter
    @Column(nullable = false)
    private String uploadUser;

    public FileDto toDto() {
        return FileDto.builder()
                .id(id)
                .fileName(fileName)
                .filePath(filePath)
                .fileType(fileType)
                .fileSize(fileSize)
                .uploadUser(uploadUser)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SaveFile saveFile = (SaveFile) o;
        return Objects.equals(fileName, saveFile.fileName) && Objects.equals(filePath, saveFile.filePath) && Objects.equals(fileType, saveFile.fileType) && Objects.equals(fileSize, saveFile.fileSize);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Builder
    public  record FileDto (
            Long id,
            String fileName,
            String filePath,
            String fileType,
            Long fileSize,
            String uploadUser,
LocalDateTime createdAt,
            String createdBy,
            LocalDateTime modifiedAt,
            String modifiedBy

    )  {





        public static FileDto from(SaveFile saveFile) {
            return new FileDto(
                    saveFile.getId(),
                    saveFile.getFileName(),
                    saveFile.getFilePath(),
                    saveFile.getFileType(),
                    saveFile.getFileSize(),
                    saveFile.getUploadUser(),
                    saveFile.getCreatedAt(),
                    saveFile.getCreatedBy(),
                    saveFile.getModifiedAt(),
                    saveFile.getModifiedBy()
            );}

        public SaveFile toEntity() {
            return SaveFile.builder()
                    .id(id)
                    .fileName(fileName)
                    .filePath(filePath)
                    .fileType(fileType)
                    .fileSize(fileSize)
                    .uploadUser(uploadUser)
                    .build();
        }





    }

    public static class FileRequestDto implements Serializable{
        @Setter
        private String fileName;
        private final String filePath;
        private final String fileType;
        private final Long fileSize;
        private final String uploadUser;

        UUID uuid = UUID.randomUUID();
        String uuidString = uuid.toString();

        @Builder
        public FileRequestDto(String fileName, String filePath, String fileType, Long fileSize, String uploadUser) {
            this.fileName = fileName;
            this.filePath = filePath;
            this.fileType = fileType;
            this.fileSize = fileSize;
            this.uploadUser = uploadUser;
        }

        public SaveFile toEntity() {
            return SaveFile.builder()
                    .fileName(fileName)
                    .filePath(filePath)
                    .fileType(fileType)
                    .fileSize(fileSize)
                    .uploadUser(uploadUser)
                    .build();
        }
    }
    public record FileResponseDto(
            Long id,
            String fileName,
            String filePath,
            String fileType,
            Long fileSize,
            String uploadUser,
            LocalDateTime createdAt,
            String createdBy,
            LocalDateTime modifiedAt,
            String modifiedBy
    ) {
        public static FileResponseDto from(SaveFile saveFile) {
            return new FileResponseDto(
                    saveFile.getId(),
                    saveFile.getFileName(),
                    saveFile.getFilePath(),
                    saveFile.getFileType(),
                    saveFile.getFileSize(),
                    saveFile.getUploadUser(),
                    saveFile.getCreatedAt(),
                    saveFile.getCreatedBy(),
                    saveFile.getModifiedAt(),
                    saveFile.getModifiedBy()
            );
        }
    }


}



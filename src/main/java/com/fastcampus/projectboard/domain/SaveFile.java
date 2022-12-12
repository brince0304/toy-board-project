package com.fastcampus.projectboard.domain;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

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

    public SaveFileDto toDto() {
        return SaveFileDto.builder()
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
    public  record SaveFileDto(
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





        public static SaveFileDto from(SaveFile saveFile) {
            return new SaveFileDto(
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

    public static class SaveFileRequestDto implements Serializable{
        @Setter
        private String fileName;
        private final String filePath;
        private final String fileType;
        private final Long fileSize;
        private final String uploadUser;

        @Builder
        public SaveFileRequestDto(String fileName, String filePath, String fileType, Long fileSize, String uploadUser) {
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
    public record SaveFileResponseDto(

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
        public static SaveFileResponseDto from(SaveFile saveFile) {
            return new SaveFileResponseDto(
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



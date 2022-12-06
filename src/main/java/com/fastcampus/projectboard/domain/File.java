package com.fastcampus.projectboard.domain;

import lombok.*;
import org.springframework.beans.factory.annotation.Value;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@Getter
@Builder
@NoArgsConstructor
public class File extends AuditingFields{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Setter
    @Column(nullable = false)
    private String fileName;
    @Setter
    @Column(nullable = false)
    @Value("${com.example.upload.path.profileImg}")
    private String filePath ;
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

    @Builder
    public  record FileDto (
            Long id,
            String fileName,
            String filePath,
            String fileType,
            Long fileSize,
            UserAccount.UserAccountDto userAccountDto,
LocalDateTime createdAt,
            String createdBy,
            LocalDateTime modifiedAt,
            String modifiedBy

    )  {


        public static FileDto from(File file) {
            return new FileDto(
                    file.getId(),
                    file.getFileName(),
                    file.getFilePath(),
                    file.getFileType(),
                    file.getFileSize(),
                    UserAccount.UserAccountDto.from(file.getUserAccount()),
                    file.getCreatedAt(),
                    file.getCreatedBy(),
                    file.getModifiedAt(),
                    file.getModifiedBy()
            );}

        public File toEntity() {
            return File.builder()
                    .id(id)
                    .fileName(fileName)
                    .filePath(filePath)
                    .fileType(fileType)
                    .fileSize(fileSize)
                    .build();
        }



    }

    public static class FileRequestDto implements Serializable{
        @Setter
        private String fileName;
        private final String filePath;
        private final String fileType;
        private final Long fileSize;
        private final UserAccount userAccount;

        UUID uuid = UUID.randomUUID();
        String uuidString = uuid.toString();

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
    public record FileResponseDto(
            Long id,
            String fileName,
            String filePath,
            String fileType,
            Long fileSize,
            UserAccount.UserAccountDto userAccountDto,
            LocalDateTime createdAt,
            String createdBy,
            LocalDateTime modifiedAt,
            String modifiedBy
    ) {
        public static FileResponseDto from(File file) {
            return new FileResponseDto(
                    file.getId(),
                    file.getFileName(),
                    file.getFilePath(),
                    file.getFileType(),
                    file.getFileSize(),
                    UserAccount.UserAccountDto.from(file.getUserAccount()),
                    file.getCreatedAt(),
                    file.getCreatedBy(),
                    file.getModifiedAt(),
                    file.getModifiedBy()
            );
        }
    }


}



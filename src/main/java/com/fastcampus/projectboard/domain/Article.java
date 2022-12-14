package com.fastcampus.projectboard.domain;


import io.micrometer.core.lang.Nullable;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

@Getter //setter는 따로 생성 ( 자동으로 설정되는 값이 있기 때문 )
@ToString(callSuper = true)
@Table(indexes = {
        @Index(columnList= "title"),
        @Index(columnList= "createdAt"),
        @Index(columnList= "createdBy")
})  //테이블 컬럼 인덱스 설정
 // 인덱스 이름이 없으니 키 부여
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Article extends AuditingFields{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 프라이머리 키
    private Long id;
    //@setter 가 붙은 값이 입력값, 없으면 자동
    @Setter @Column(nullable = false) String title; //n
    // ull 이 아닌 값을 컬럼에 저장 함
    @Setter
    @JoinColumn(name = "userId")
    @ManyToOne(optional = false,fetch = FetchType.LAZY)
    private UserAccount userAccount; // 유저 정보 (ID)

    @Setter @Column(nullable = false,length = 10000) private String content;


    @OrderBy("createdAt")  //id 순서
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL) //양방향 관계 (article이 주체)
    @ToString.Exclude //과부하 발생 예방
    private final Set<ArticleComment> articleComments = new LinkedHashSet<>();

    @OneToMany(mappedBy = "article")
    @ToString.Exclude
    @Setter
    private Set<ArticleHashtag> hashtags = new HashSet<>();

    @Setter
    @Column(nullable = false)
    private String deleted = "N";


    @Setter
    @Column(nullable = false)
    private Integer viewCount = 0;

    @Setter
    @Column(nullable = false)
    private Integer likeCount =0;





    protected Article() { //기본 빈 생성자
    }

    private Article(UserAccount userAccount,String title, String content, Set<ArticleHashtag> hashtags) {
        this.userAccount = userAccount;
        this.title = title;
        this.content = content;
        this.hashtags = hashtags;
    }


    public static Article of(UserAccount userAccount,String title, String content) { //게시글 제목 내용 해시태그
        return new Article(userAccount,title,content,null);
    }

    public static Article of(UserAccount userAccount,String title, String content, Set<ArticleHashtag> hashtags) { //게시글 제목 내용 해시태그
        return new Article(userAccount,title,content,hashtags);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Article article)) return false;
        return id != null && id.equals(article.id); //id가 존재할때
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ArticleRequest implements Serializable {
        @Nullable
        private Long articleId;
        @Size(min = 5, message = "* 제목은 5자 이상 입력해주세요.")
        private String title;
        @Size(min = 5, message = "* 내용은 5자 이상 입력해주세요.")
        private String content;
        @Nullable
        private String hashtag;

        @Nullable
        private String fileIds;


        public ArticleRequest(String title, String content,String hashtag,String fileIds) {
            this.articleId = null;
            this.title = title;
            this.content = content;
            this.hashtag = hashtag;
            this.fileIds = fileIds;
        }
        public ArticleDto toDto(UserAccount.UserAccountDto userAccountDto) {
            return ArticleDto.builder()
                    .title(title)
                    .content(content)
                    .userAccountDto(userAccountDto)
                    .hashtags(Hashtag.HashtagDto.from(hashtag))
                    .build();
        }
    }

    @Builder
    public record ArticleWithCommentResponse(
            Long id,
            String title,
            String content,
            LocalDateTime createdAt,
            String email,
            String nickname,
            String userId,
            Set<Hashtag.HashtagDto> hashtags,
            String deleted,
            Integer viewCount,
            Integer likeCount
    ) implements Serializable {

        public static ArticleWithCommentResponse from(ArticleDtoWithSaveFiles dto) {
            String nickname = dto.getUserAccountDto().nickname();
            if (nickname == null || nickname.isBlank()) {
                nickname = dto.getUserAccountDto().userId();
            }

            return new ArticleWithCommentResponse(
                    dto.getId(),
                    dto.getTitle(),
                    dto.getContent(),
                    dto.getCreatedAt(),
                    dto.getUserAccountDto().email(),
                    nickname,
                    dto.getUserAccountDto().userId(),
                    dto.getHashtags(),
                    dto.getDeleted(),
                    dto.getViewCount(),
                    dto.getLikeCount());
        }

    }
    @Builder
    public record ArticleDto(
            Long id,
            UserAccount.UserAccountDto userAccountDto,
            String title,
            String content,
            LocalDateTime createdAt,
            String createdBy,
            LocalDateTime modifiedAt,
            String modifiedBy,
            String deleted,
            Integer viewCount,
            Integer likeCount,
            Set<Hashtag.HashtagDto> hashtags

    ) {

        public static ArticleDto from(Article entity) {
            return ArticleDto.builder()
                    .id(entity.getId())
                    .userAccountDto(UserAccount.UserAccountDto.from(entity.getUserAccount()))
                    .title(entity.getTitle())
                    .content(entity.getContent())
                    .createdAt(entity.getCreatedAt())
                    .createdBy(entity.getCreatedBy())
                    .modifiedAt(entity.getModifiedAt())
                    .modifiedBy(entity.getModifiedBy())
                    .deleted(entity.getDeleted())
                    .viewCount(entity.getViewCount())
                    .likeCount(entity.getLikeCount())
                    .build();
        }

        public Article toEntity() {
            return Article.of(
                    userAccountDto.toEntity(),
                    title,
                    content);
        }

    }
    @Builder
    @Getter
    @Setter
    public static class ArticleDtoWithSaveFiles {
        Long id ;
        UserAccount.UserAccountDto userAccountDto ;
        String title ;
        String content ;
        LocalDateTime createdAt ;
        String createdBy ;
        LocalDateTime modifiedAt ;
        String modifiedBy ;
        Set<Hashtag.HashtagDto> hashtags ;
        String deleted ;
        Integer viewCount ;

        Integer likeCount ;

        Set<SaveFile.SaveFileDto> saveFiles;
        public ArticleDtoWithSaveFiles(Long id,
                                       UserAccount.UserAccountDto userAccountDto,
                                       String title, String content, LocalDateTime createdAt, String createdBy, LocalDateTime modifiedAt, String modifiedBy, Set<Hashtag.HashtagDto> hashtags, String deleted, Integer viewCount, Integer likeCount, Set<SaveFile.SaveFileDto> saveFiles) {
            this.id = id;
            this.userAccountDto = userAccountDto;
            this.title = title;
            this.content = content;
            this.createdAt = createdAt;
            this.createdBy = createdBy;
            this.modifiedAt = modifiedAt;
            this.modifiedBy = modifiedBy;
            this.hashtags = hashtags;
            this.deleted = deleted;
            this.viewCount = viewCount;
            this.likeCount = likeCount;
            this.saveFiles = saveFiles;
        }

        public static ArticleDtoWithSaveFiles from(Article entity) {
            return ArticleDtoWithSaveFiles.builder()
                    .id(entity.getId())
                    .userAccountDto(UserAccount.UserAccountDto.from(entity.getUserAccount()))
                    .title(entity.getTitle())
                    .content(entity.getContent())
                    .createdAt(entity.getCreatedAt())
                    .createdBy(entity.getCreatedBy())
                    .modifiedAt(entity.getModifiedAt())
                    .modifiedBy(entity.getModifiedBy())
                    .deleted(entity.getDeleted())
                    .viewCount(entity.getViewCount())
                    .likeCount(entity.getLikeCount())
                    .build();
        }

    }

    @Builder
    public record ArticleResponse(
            Long id,
            String title,
            String content,
            LocalDateTime createdAt,
            String email,
            String nickname,
            String deleted,
            Integer viewCount,
            Integer likeCount
    ) implements Serializable {

        public static ArticleResponse from(Article.ArticleDto dto) {
            String nickname = dto.userAccountDto().nickname();
            if (nickname == null || nickname.isBlank()) {
                nickname = dto.userAccountDto().userId();
            }

            return new ArticleResponse(
                    dto.id(),
                    dto.title(),
                    dto.content(),
                    dto.createdAt(),
                    dto.userAccountDto().email(),
                    nickname,
                    dto.deleted(),
                    dto.viewCount(),
                    dto.likeCount()
            );
        }

    }

    public record ArticleRepsonseWithSaveFile(
            Long id,
            String title,
            String content,
            LocalDateTime createdAt,
            String email,
            String nickname,
            String deleted,
            Integer viewCount,
            Integer likeCount,
            Set<SaveFile.SaveFileDto> saveFiles,
            Set<Hashtag.HashtagDto> hashtags
    ) implements Serializable {

        public static ArticleRepsonseWithSaveFile from(ArticleDtoWithSaveFiles dto) {
            String nickname = dto.getUserAccountDto().nickname();
            if (nickname == null || nickname.isBlank()) {
                nickname = dto.getUserAccountDto().userId();
            }

            return new ArticleRepsonseWithSaveFile(
                    dto.getId(),
                    dto.getTitle(),
                    dto.getContent(),
                    dto.getCreatedAt(),
                    dto.getUserAccountDto().email(),
                    nickname,
                    dto.getDeleted(),
                    dto.getViewCount(),
                    dto.getLikeCount(),
                    dto.getSaveFiles(),
                    dto.getHashtags()
            );
        }

    }
}

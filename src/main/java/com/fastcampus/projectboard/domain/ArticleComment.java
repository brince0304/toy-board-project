package com.fastcampus.projectboard.domain;


import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Getter //게터
@ToString //
@Table(indexes = {
        @Index(columnList= "content"),
        @Index(columnList= "createdAt"),
        @Index(columnList= "createdBy")
})
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ArticleComment extends AuditingFields{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Setter
    @JoinColumn(name = "userId")
    @ManyToOne(optional = false)
    private UserAccount userAccount; // 유저 정보 (ID)
    @Setter @ManyToOne(optional = false) private Article article;
    @Setter @Column(nullable = false,length= 500) private String content;

    @Setter
    @Column(nullable = false)
    private String deleted = "N";
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @ToString.Exclude
    private ArticleComment parent;

    @Setter
    @OneToMany(mappedBy = "parent")
    @ToString.Exclude
    private Set<ArticleComment> children = new LinkedHashSet<>();

    @Setter
    private String isParent;


    protected ArticleComment() {
    }

    public ArticleComment(UserAccount userAccount, Article article, String content, ArticleComment parent) {
        this.userAccount = userAccount;
        this.article = article;
        this.content = content;
        this.parent = parent;
    }

    public static  ArticleComment of(Article article, UserAccount userAccount,String content) {
        return new ArticleComment(userAccount,article,content,null);
    }

    public static  ArticleComment of(Article article, UserAccount userAccount,String content,ArticleComment parent) {
        return new ArticleComment(userAccount,article,content,parent);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArticleComment that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    @Builder
    public record ArticleCommentRequest(
            Long articleId,
            @Size(min = 2, max= 100, message = "* 댓글은 2자 이상 100자 이하로 입력해주세요.")
            String content,
            Long parentId,
            Long articleCommentId
    ) implements Serializable {

        public static ArticleCommentRequest of(Long articleId, String content) {
            return new ArticleCommentRequest(articleId, content, null,null);
        }

        public static ArticleCommentRequest of(Long articleId, String content, Long parentId) {
            return new ArticleCommentRequest(articleId, content, parentId,null);
        }
        public static ArticleCommentRequest of(String content, Long articleCommentId) {
            return new ArticleCommentRequest(null, content, null,articleCommentId);
        }


        public ArticleCommentDto toDto(UserAccount.UserAccountDto userAccountDto) {
            return ArticleCommentDto.of(articleId, userAccountDto, content, null, null);
        }


    }


    @Builder
    public record ArticleCommentResponse(
            Long id,
            String content,
            LocalDateTime createdAt,
            String email,
            String nickname,
            String userId,
            String deleted,
            Set<ArticleCommentDto> children,
            String isParent

    ) {

        public static ArticleCommentResponse of(Long id, String content, LocalDateTime createdAt, String email, String nickname, String userId, String deleted,Set<ArticleCommentDto> children,String isParent ) {
            return new ArticleCommentResponse(id, content, createdAt, email, nickname, userId, deleted,children,isParent);
        }

        public static ArticleCommentResponse from(ArticleCommentDto dto) {
            String nickname = dto.userAccountDto().nickname();
            if (nickname == null || nickname.isBlank()) {
                nickname = dto.userAccountDto().userId();
            }

            return ArticleCommentResponse.builder()
                    .id(dto.id())
                    .content(dto.content())
                    .createdAt(dto.createdAt())
                    .email(dto.userAccountDto().email())
                    .nickname(nickname)
                    .userId(dto.userAccountDto().userId())
                    .deleted(dto.deleted())
                    .children(dto.children())
                    .isParent(dto.isParent())
                    .build();
        }
    }
    @Builder
    public record ArticleCommentDto(
            Long id,
            Long articleId,
            UserAccount.UserAccountDto userAccountDto,
            String content,
            LocalDateTime createdAt,
            String createdBy,
            LocalDateTime modifiedAt,
            String modifiedBy,
            String deleted,
            Set<ArticleCommentDto> children,
            String isParent
    ) {
        public static ArticleCommentDto of(Long articleId, UserAccount.UserAccountDto userAccountDto, String content, Set<ArticleCommentDto> children, String isParent) {
            return new ArticleCommentDto(null, articleId, userAccountDto, content, null, null, null, null, null,  children, isParent);
        }
        public static ArticleCommentDto of(Long id, Long articleId, UserAccount.UserAccountDto userAccountDto, String content, LocalDateTime createdAt, String createdBy, LocalDateTime modifiedAt, String modifiedBy, String deleted, Set<ArticleCommentDto> children, String isParent) {
            return new ArticleCommentDto(id, articleId, userAccountDto, content, createdAt, createdBy, modifiedAt, modifiedBy, deleted,  children, isParent);
        }

        public static ArticleCommentDto from(ArticleComment entity) {
            return ArticleCommentDto.builder()
                    .id(entity.getId())
                    .articleId(entity.getArticle().getId())
                    .userAccountDto(UserAccount.UserAccountDto.from(entity.getUserAccount()))
                    .content(entity.getContent())
                    .createdAt(entity.getCreatedAt())
                    .createdBy(entity.getCreatedBy())
                    .modifiedAt(entity.getModifiedAt())
                    .modifiedBy(entity.getModifiedBy())
                    .deleted(entity.getDeleted())
                    .children(entity.getChildren().stream().map(ArticleCommentDto::from).collect(Collectors.toSet()))
                    .isParent(entity.getParent() == null ? "Y" : "N")
                    .build();
        }

        public ArticleComment toEntity(Article article, UserAccount userAccount) {
            return ArticleComment.of(
                    article,
                    userAccount,
                    content
            );
        }

    }



}

package com.fastcampus.projectboard.dto;

import com.fastcampus.projectboard.domain.Article;

import java.time.LocalDateTime;

/**
 * A DTO for the {@link com.fastcampus.projectboard.domain.Article} entity
 */
public record ArticleDto(
        Long id,
        UserAccountDto userAccountDto,
        String title,
        String content,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime modifiedAt,
        String modifiedBy,
        String deleted,
        Integer viewCount,
        Integer likeCount

) {

    public static ArticleDto of(UserAccountDto userAccountDto, String title, String content) {
        return new ArticleDto(null, userAccountDto, title, content,  null, null,null, null,null,null,null);
    }
    public static ArticleDto of(Long id, UserAccountDto userAccountDto, String title, String content, LocalDateTime createdAt, String createdBy, LocalDateTime modifiedAt, String modifiedBy, String deleted,Integer viewCount, Integer likeCount) {
        return new ArticleDto(id, userAccountDto, title, content,  createdAt, createdBy, modifiedAt, modifiedBy,deleted, viewCount, likeCount);
    }

    public static ArticleDto from(Article entity) {
           return ArticleDto.of(
                    entity.getId(),
                    UserAccountDto.from(entity.getUserAccount()),
                    entity.getTitle(),
                    entity.getContent(),
                   entity.getCreatedAt(),
                    entity.getCreatedBy(),
                    entity.getModifiedAt(),
                    entity.getModifiedBy()
                   ,entity.getDeleted(),
                     entity.getViewCount(),
                        entity.getLikeCount()
            );
    }

    public Article toEntity() {
        return Article.of(
                userAccountDto.toEntity(),
                title,
                content);
    }

}
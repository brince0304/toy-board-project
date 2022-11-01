package com.fastcampus.projectboard.dto;

import com.fastcampus.projectboard.domain.Article;
import com.fastcampus.projectboard.domain.Hashtag;
import com.fastcampus.projectboard.domain.UserAccount;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

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
        String deleted
) {

    public static ArticleDto of(UserAccountDto userAccountDto, String title, String content) {
        return new ArticleDto(null, userAccountDto, title, content,  null, null,null, null,null);
    }
    public static ArticleDto of(Long id, UserAccountDto userAccountDto, String title, String content, LocalDateTime createdAt, String createdBy, LocalDateTime modifiedAt, String modifiedBy, String deleted) {
        return new ArticleDto(id, userAccountDto, title, content,  createdAt, createdBy, modifiedAt, modifiedBy,deleted);
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
                   ,entity.getDeleted()
            );
    }

    public Article toEntity() {
        return Article.of(
                userAccountDto.toEntity(),
                title,
                content);
    }

}
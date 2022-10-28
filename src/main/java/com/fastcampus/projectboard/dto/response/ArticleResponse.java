package com.fastcampus.projectboard.dto.response;

import com.fastcampus.projectboard.domain.Hashtag;
import com.fastcampus.projectboard.dto.ArticleDto;
import com.fastcampus.projectboard.dto.HashtagDto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * A DTO for the {@link com.fastcampus.projectboard.domain.Article} entity
 */
public record ArticleResponse(
        Long id,
        String title,
        String content,
        Set<HashtagDto> hashtags,
        LocalDateTime createdAt,
        String email,
        String nickname
) implements Serializable {

    public static ArticleResponse of(Long id, String title, String content, Set<HashtagDto>hashtags, LocalDateTime createdAt, String email, String nickname) {
        return new ArticleResponse(id, title, content, hashtags, createdAt, email, nickname);
    }

    public static ArticleResponse from(ArticleDto dto) {
        String nickname = dto.userAccountDto().nickname();
        if (nickname == null || nickname.isBlank()) {
            nickname = dto.userAccountDto().userId();
        }

        return new ArticleResponse(
                dto.id(),
                dto.title(),
                dto.content(),
                dto.hashtags(),
                dto.createdAt(),
                dto.userAccountDto().email(),
                nickname
        );
    }

}

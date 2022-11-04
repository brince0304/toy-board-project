package com.fastcampus.projectboard.dto.response;
import com.fastcampus.projectboard.dto.ArticleWithCommentDto;
import com.fastcampus.projectboard.dto.HashtagDto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public record ArticleWithCommentResponse(
        Long id,
        String title,
        String content,
        LocalDateTime createdAt,
        String email,
        String nickname,
        String userId,
        Set<ArticleCommentResponse> articleCommentsResponse,
        Set<HashtagDto> hashtags,
        String deleted,
        Integer viewCount,
        Integer likeCount
) implements Serializable {

    public static ArticleWithCommentResponse of(Long id, String title, String content,  LocalDateTime createdAt, String email, String nickname,String userId, Set<ArticleCommentResponse> articleCommentResponses, Set<HashtagDto> hashtags, String deleted, Integer viewCount, Integer likeCount) {
        return new ArticleWithCommentResponse(id, title, content, createdAt, email, nickname, userId,articleCommentResponses,hashtags, deleted,viewCount, likeCount);
    }

    public static ArticleWithCommentResponse from(ArticleWithCommentDto dto) {
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
                dto.getArticleCommentDtos().stream().map(ArticleCommentResponse::from).collect(Collectors.toCollection(LinkedHashSet::new)),
                dto.getHashtags(),
                dto.getDeleted(),
                dto.getViewCount(),
                dto.getLikeCount());
    }

}
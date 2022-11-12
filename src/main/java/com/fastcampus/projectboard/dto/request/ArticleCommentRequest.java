package com.fastcampus.projectboard.dto.request;

import com.fastcampus.projectboard.dto.ArticleCommentDto;
import com.fastcampus.projectboard.dto.UserAccountDto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * A DTO for the {@link com.fastcampus.projectboard.domain.ArticleComment} entity
 */
public record ArticleCommentRequest(
        Long articleId
        ,
        String content,
        Long parentId) implements Serializable {

    public static ArticleCommentRequest of(Long articleId, String content) {
        return new ArticleCommentRequest(articleId, content,null);
    }

    public static ArticleCommentRequest of(Long articleId, String content, Long parentId) {
        return new ArticleCommentRequest(articleId, content,parentId);
    }


    public ArticleCommentDto toDto(UserAccountDto userAccountDto) {
        return ArticleCommentDto.of(
                articleId,
                userAccountDto,
                content,
                null,
                null
        );
    }


}
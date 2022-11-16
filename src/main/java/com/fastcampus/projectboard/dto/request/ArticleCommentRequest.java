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
        Long articleId,
        @Size(min = 2, max= 100, message = "* 댓글은 2자 이상 100자 이하로 입력해주세요.")
        String content,
        Long parentId
) implements Serializable {

    public static ArticleCommentRequest of(Long articleId, String content) {
        return new ArticleCommentRequest(articleId, content, null);
    }

    public static ArticleCommentRequest of(Long articleId, String content, Long parentId) {
        return new ArticleCommentRequest(articleId, content, parentId);
    }


    public ArticleCommentDto toDto(UserAccountDto userAccountDto) {
        return ArticleCommentDto.of(articleId, userAccountDto, content, null, null);
    }


}
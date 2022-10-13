package dto;

import com.fastcampus.projectboard.domain.Article;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * A DTO for the {@link com.fastcampus.projectboard.domain.ArticleComment} entity
 */
public record ArticleCommentDto(LocalDateTime createdAt,
                                String createdBy,
                                String content){
    public static ArticleCommentDto of(LocalDateTime createdAt, String createdBy, String content) {
        return new ArticleCommentDto(createdAt, createdBy, content);
    }
}
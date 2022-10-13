package dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * A DTO for the {@link com.fastcampus.projectboard.domain.ArticleComment} entity
 */
public record ArticleWithCommentDto(LocalDateTime createdAt, String createdBy, ArticleDto article,
                                    String content) implements Serializable {
    public static ArticleWithCommentDto of(LocalDateTime createdAt, String createdBy, ArticleDto article, String content) {
    return new ArticleWithCommentDto(createdAt, createdBy, article, content);
    }
}
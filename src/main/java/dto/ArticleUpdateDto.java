package dto;

import java.time.LocalDateTime;

/**
 * A DTO for the {@link com.fastcampus.projectboard.domain.Article} entity
 */
public record ArticleUpdateDto(UserAccountDto dto,
        String title,
                               String content,
                               String hashtag){
    public static ArticleUpdateDto of(UserAccountDto dto,String title, String content, String hashtag) {
        return new ArticleUpdateDto(dto,title, content, hashtag);
    }
}
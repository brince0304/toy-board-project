package dto.response;

import dto.ArticleDto;

import java.time.LocalDateTime;

/**
 * A DTO for the {@link com.fastcampus.projectboard.domain.Article} entity
 */
public record ArticleResponse(
        Long id,
        String title,
        String content,
        LocalDateTime createdAt,
        String email,
        String nickname
) {

    public static ArticleResponse of(Long id, String title, String content, LocalDateTime createdAt, String email, String nickname) {
        return new ArticleResponse(id, title, content, createdAt, email, nickname);
    }

    public static ArticleResponse from(ArticleDto dto) {
        String nickname = dto.userAccountdto().nickname();
        if (nickname == null || nickname.isBlank()) {
            nickname = dto.userAccountdto().userId();
        }

        return new ArticleResponse(
                dto.id(),
                dto.title(),
                dto.content(),
                dto.createdAt(),
                dto.userAccountdto().email(),
                nickname
        );
    }

}
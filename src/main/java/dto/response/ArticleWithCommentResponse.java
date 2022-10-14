package dto.response;
import dto.ArticleWithCommentDto;

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
        Set<ArticleCommentResponse> articleCommentsResponse
) {

    public static ArticleWithCommentResponse of(Long id, String title, String content, LocalDateTime createdAt, String email, String nickname, String userId, Set<ArticleCommentResponse> articleCommentResponses) {
        return new ArticleWithCommentResponse(id, title, content, createdAt, email, nickname, userId, articleCommentResponses);
    }

    public static ArticleWithCommentResponse from(ArticleWithCommentDto dto) {
        String nickname = dto.userAccountDto().nickname();
        if (nickname == null || nickname.isBlank()) {
            nickname = dto.userAccountDto().userId();
        }

        return new ArticleWithCommentResponse(
                dto.id(),
                dto.title(),
                dto.content(),
                dto.createdAt(),
                dto.userAccountDto().email(),
                nickname,
                dto.userAccountDto().userId(),
                dto.articleCommentDtos().stream()
                        .map(ArticleCommentResponse::from)
                        .collect(Collectors.toCollection(LinkedHashSet::new))
        );
    }

}


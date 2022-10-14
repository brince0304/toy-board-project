package dto;

import com.fastcampus.projectboard.domain.Article;
import com.fastcampus.projectboard.domain.UserAccount;

import java.time.LocalDateTime;

/**
 * A DTO for the {@link com.fastcampus.projectboard.domain.Article} entity
 */
public record ArticleDto(Long id
        ,UserAccountDto userAccountdto,
                         String title,
                         String content,
                         String hashtag,
                         LocalDateTime createdAt,
                         String createdBy,
                         LocalDateTime modifiedAt,
                         String modifiedBy){


    public static  ArticleDto of(Long id, UserAccountDto userAccountdto, String title, String content, String hashtag, LocalDateTime createdAt, String createdBy, LocalDateTime modifiedAt, String modifiedBy) {
return new ArticleDto(id, userAccountdto, title, content, hashtag, createdAt, createdBy, modifiedAt, modifiedBy);
    }

    public static ArticleDto of( UserAccountDto userAccountdto, String title, String content, String hashtag) {
        return new ArticleDto(null,userAccountdto,title,content,hashtag,null,null,null,null);
    }

    public static ArticleDto from(Article entity) {
   return new ArticleDto(
           entity.getId(),
           UserAccountDto.from(entity.getUserAccount()),
           entity.getTitle(),
           entity.getContent(),
           entity.getHashtag(),
           entity.getCreatedAt(),
           entity.getCreatedBy(),
           entity.getModifiedAt(),
           entity.getModifiedBy()
   );
    }

    public Article toEntity(UserAccount userAccount){
        return Article.of(
                userAccount,
                title,
                content,
                hashtag
        );
    }
}
package com.fastcampus.projectboard.dto.request;

import com.fastcampus.projectboard.dto.ArticleCommentDto;
import com.fastcampus.projectboard.dto.ArticleDto;
import com.fastcampus.projectboard.dto.HashtagDto;
import com.fastcampus.projectboard.dto.UserAccountDto;
import io.micrometer.core.lang.Nullable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * A DTO for the {@link com.fastcampus.projectboard.domain.Article} entity
 */

@Getter
@Setter
public class ArticleRequest implements Serializable {
    @Size(min = 5, message = "* 제목은 5자 이상 입력해주세요.")
    private String title;
    @Size(min = 5, message = "* 내용은 5자 이상 입력해주세요.")
      private String content;
    @Nullable
      private String hashtag;
      private Set<HashtagDto> hashtags = new HashSet<>();


    public ArticleRequest(String title, String content,String hashtag) {
        Set<HashtagDto> hashtags = new HashSet<>();
        if(hashtag!=null) {
            if (hashtag.contains("#")) {
                String newHashtag = hashtag.replaceAll(" ", "");
                StringTokenizer st = new StringTokenizer(newHashtag, "#");
                while (st.hasMoreTokens()) {
                    hashtags.add(HashtagDto.of(st.nextToken()));
                }
            }
        }
        this.title = title;
        this.content = content;
        this.hashtags = hashtags;
    }

    public static ArticleRequest of(String title, String content, String hashtag) {
        return new ArticleRequest(title, content, hashtag);

    }



    public ArticleDto toDto(UserAccountDto userAccountDto) {
        return ArticleDto.of(
                userAccountDto,
                title,
                content
        );
    }
}
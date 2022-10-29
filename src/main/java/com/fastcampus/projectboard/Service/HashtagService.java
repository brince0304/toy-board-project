package com.fastcampus.projectboard.Service;

import com.fastcampus.projectboard.domain.Article;
import com.fastcampus.projectboard.domain.ArticleHashtag;
import com.fastcampus.projectboard.domain.Hashtag;
import com.fastcampus.projectboard.dto.ArticleDto;
import com.fastcampus.projectboard.dto.HashtagDto;
import com.fastcampus.projectboard.repository.ArticleHashtagRepository;
import com.fastcampus.projectboard.repository.HashtagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class HashtagService {

    private final HashtagRepository hashtagRepository;
    private final ArticleHashtagRepository articleHashtagRepository;

    public void saveHashtags(Set<HashtagDto> hashtags) {
        hashtags.stream()
                .map(HashtagDto::hashtag)
                .map(Hashtag::of)
                .forEach(t -> {
                    if (hashtagRepository.findByHashtag(t.getHashtag()).isEmpty()) {
                        hashtagRepository.save(t);
                    }
                });
    }
    public HashtagDto getHashtag(String hashtag) {
        return HashtagDto.from(hashtagRepository.findByHashtag(hashtag).orElseThrow());
    }

    public void saveHashtag(Hashtag hashtag) {
        hashtagRepository.save(hashtag);
    }

    public Set<ArticleDto> getArticlesByHashtag(String hashtag) {
        return hashtagRepository.findByHashtag(hashtag).get().getArticles().stream().map(t-> ArticleDto.from(t.getArticle())
        ).collect(Collectors.toSet());
    }
}

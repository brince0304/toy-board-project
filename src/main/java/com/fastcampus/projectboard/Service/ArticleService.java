package com.fastcampus.projectboard.Service;

import com.fastcampus.projectboard.Util.RedisUtil;
import com.fastcampus.projectboard.domain.Article;
import com.fastcampus.projectboard.domain.ArticleHashtag;
import com.fastcampus.projectboard.domain.Hashtag;
import com.fastcampus.projectboard.domain.type.SearchType;
import com.fastcampus.projectboard.repository.ArticleHashtagRepository;
import com.fastcampus.projectboard.repository.ArticleRepository;
import com.fastcampus.projectboard.repository.HashtagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class ArticleService {


   private final ArticleRepository articleRepository;
   private final HashtagRepository hashtagRepository;
   private final ArticleHashtagRepository articlehashtagrepository;
   private final RedisUtil redisUtil;

    @Transactional(readOnly = true)
    public Page<Article.ArticleDto> searchArticles(SearchType searchType, String searchKeyword, Pageable pageable) {
        if (searchKeyword == null || searchKeyword.isBlank()) {
            return articleRepository.findAll(pageable).map(Article.ArticleDto::from);
        }

        return switch (searchType) {
            case TITLE -> articleRepository.findByTitleContaining(searchKeyword, pageable).map(Article.ArticleDto::from);
            case CONTENT -> articleRepository.findByContentContaining(searchKeyword, pageable).map(Article.ArticleDto::from);
            case ID -> articleRepository.findByUserAccount_UserIdContaining(searchKeyword, pageable).map(Article.ArticleDto::from);
            case NICKNAME -> articleRepository.findByUserAccount_NicknameContaining(searchKeyword, pageable).map(Article.ArticleDto::from);
            case HASHTAG -> null;
        };
    }

    @Transactional(readOnly = true)
    public Set<Article.ArticleDto> getArticlesByHashtag(String hashtag) {
        return hashtagRepository.findByHashtag(hashtag).isPresent() ?
                hashtagRepository.findByHashtag(hashtag).get().getArticles().stream()
                        .map(ArticleHashtag::getArticle)
                        .map(Article.ArticleDto::from)
                        .collect(Collectors.toSet()) : null;
    }
    public void saveHashtags(Set<Hashtag.HashtagDto> hashtags) {
        hashtags.stream()
                .map(Hashtag.HashtagDto::hashtag)
                .map(Hashtag::of)
                .forEach(t -> {
                    if (hashtagRepository.findByHashtag(t.getHashtag()).isEmpty()) {
                        hashtagRepository.save(t);
                    }
                });
    }
    @Transactional(readOnly = true)
    public Hashtag.HashtagDto getHashtag(String hashtag) {
        return Hashtag.HashtagDto.from(hashtagRepository.findByHashtag(hashtag).orElseThrow(()-> new EntityNotFoundException("해시태그가 없습니다 - hashtag: " + hashtag)));
    }

    public void saveHashtag(Hashtag hashtag) {
        hashtagRepository.save(hashtag);
    }



    @Transactional(readOnly = true)
    public Article.ArticleWithCommentDto getArticle(Long articleId) {
        Set<Hashtag.HashtagDto> hashtags =  new HashSet<>();
        articlehashtagrepository.findByArticleId(articleId).forEach(articleHashtag -> {
            hashtags.add(Hashtag.HashtagDto.from(articleHashtag.getHashtag()));
        });
        Article.ArticleWithCommentDto article =articleRepository.findById(articleId)
                .map(Article.ArticleWithCommentDto::from)
                .orElseThrow(() -> new EntityNotFoundException("게시글이 없습니다 - articleId: " + articleId));
        article.setHashtags(hashtags);
        return article;
    }

    public void updateViewCount(String clientIp, Long articleId) {
        if(redisUtil.isFirstIpRequest(clientIp,articleId)){
            redisUtil.writeClientRequest(clientIp,articleId);
            Article article = articleRepository.findById(articleId).orElseThrow(EntityNotFoundException::new);
            article.setViewCount(article.getViewCount()+1);
        }
    }

    public Integer updateLikeCount(String clientIp, Long articleId) {
        Article article = articleRepository.findById(articleId).orElseThrow(EntityNotFoundException::new);

        if(redisUtil.isFirstIpRequest2(clientIp, articleId)) {
            redisUtil.writeClientRequest2(clientIp, articleId);
            article.setLikeCount(article.getLikeCount() + 1);
            return article.getLikeCount();
        }
        return article.getLikeCount();
    }




    public Article.ArticleDto saveArticle(Article.ArticleDto dto, List<Hashtag.HashtagDto> hashtagDtos) {
        Article article =articleRepository.save(dto.toEntity());
        for (Hashtag.HashtagDto hashtag : hashtagDtos) {
                Hashtag hashtag1= hashtagRepository.findByHashtag(hashtag.hashtag()).orElseGet(()-> hashtagRepository.save(hashtag.toEntity()));
                articlehashtagrepository.save(ArticleHashtag.of(article,hashtag1));
        }
        return Article.ArticleDto.from(article);
    }
        public void updateArticle (Long articleId, Article.ArticleRequest dto, List<Hashtag.HashtagDto> hashtagDtos){
            Article article = articleRepository.findById(articleId)
                    .orElseThrow(() -> new EntityNotFoundException("게시글이 없습니다 - articleId: " + articleId));
            articlehashtagrepository.findByArticleId(articleId).forEach(articleHashtag -> {
                articleHashtag.setArticle(null);
                articleHashtag.setHashtag(null);
            });
            for( Hashtag.HashtagDto hashtag: hashtagDtos){
                Hashtag hashtag1 = hashtagRepository.findByHashtag(hashtag.hashtag()).orElseGet(()-> hashtagRepository.save(hashtag.toEntity()));
                articlehashtagrepository.save(ArticleHashtag.of(article,hashtag1));
            }
            if(dto.getTitle() != null) article.setTitle(dto.getTitle());
            if(dto.getContent() != null) article.setContent(dto.getContent());

        }



    public Article.ArticleDto getArticleDto2(Long articleId) {
        return articleRepository.findById(articleId).map(Article.ArticleDto::from).orElseThrow(() -> new EntityNotFoundException("게시글이 없습니다 - articleId: " + articleId));

    }

    public void deleteArticle(long articleId) {
        for( ArticleHashtag articleHashtag : articlehashtagrepository.findByArticleId(articleId)){
            articleHashtag.setArticle(null);
            articleHashtag.setHashtag(null);
        }
        articleRepository.deleteById(articleId);
    }

    public void deleteArticleByAdmin(Long articleId) {
        articleRepository.findById(articleId).ifPresent(o->{
            o.setDeleted("Y");
            for( ArticleHashtag articleHashtag : articlehashtagrepository.findByArticleId(articleId)){
                articleHashtag.setArticle(null);
                articleHashtag.setHashtag(null);
            }
        });
    }
}

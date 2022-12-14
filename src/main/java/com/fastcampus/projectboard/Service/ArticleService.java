package com.fastcampus.projectboard.Service;

import com.fastcampus.projectboard.Util.RedisUtil;
import com.fastcampus.projectboard.domain.*;
import com.fastcampus.projectboard.domain.type.SearchType;
import com.fastcampus.projectboard.repository.ArticleHashtagRepository;
import com.fastcampus.projectboard.repository.ArticleRepository;
import com.fastcampus.projectboard.repository.ArticleSaveFileRepository;
import com.fastcampus.projectboard.repository.HashtagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
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

   private final ArticleSaveFileRepository articleSaveFileRepository;
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
    public String getWriterFromArticle(Long articleId){
        return articleRepository.getReferenceById(articleId).getUserAccount().getUserId();
    }

    @Transactional(readOnly = true)
    public Set<Article.ArticleDto> getArticlesByHashtag(String hashtag) {
        return hashtagRepository.findByHashtag(hashtag).isPresent() ?
                hashtagRepository.findByHashtag(hashtag).get().getArticles().stream()
                        .map(ArticleHashtag::getArticle)
                        .map(Article.ArticleDto::from)
                        .collect(Collectors.toSet()) : new HashSet<>();
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




    public void deleteArticleByArticleId(long articleId) {
        for( ArticleHashtag articleHashtag : articlehashtagrepository.findByArticleId(articleId)){
            articleHashtag.setArticle(null);
            articleHashtag.setHashtag(null);
        }
        articleRepository.findById(articleId).orElseThrow(EntityNotFoundException::new).setDeleted("Y");
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

    @Transactional(readOnly = true)
    public Article.ArticleWithCommentDto getArticle(Long articleId) {
        Set<Hashtag.HashtagDto> hashtags = articlehashtagrepository.findByArticleId(articleId).stream()
                .map(ArticleHashtag::getHashtag)
                .map(Hashtag.HashtagDto::from)
                .collect(Collectors.toSet());
        Article.ArticleWithCommentDto article =articleRepository.findById(articleId)
                .map(Article.ArticleWithCommentDto::from)
                .orElseThrow(EntityNotFoundException::new);
        Set<SaveFile.SaveFileDto>  saveFiles = articleSaveFileRepository.getSaveFileByArticleId(articleId).stream()
                        .map(ArticleSaveFile::getSaveFile)
                                .map(SaveFile.SaveFileDto::from)
                                .collect(Collectors.toSet());
        article.setHashtags(hashtags);
        article.setSaveFiles(saveFiles);
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


    public Article.ArticleDto saveArticle(Article.ArticleDto dto, Set<SaveFile.SaveFileDto> saveFileDtos) {
        Article article =articleRepository.save(dto.toEntity());
        articleHashtagSavefileMapper(article,dto.hashtags(),saveFileDtos);
        return Article.ArticleDto.from(article);
    }

        public void updateArticle (Long articleId, Article.ArticleDto dto, Set<SaveFile.SaveFileDto> saveFileDtos){
            Article article = articleHashtagUpdateNull(articleId);
            if(dto.title() != null) article.setTitle(dto.title());
            if(dto.content() != null) article.setContent(dto.content());
            articleHashtagSavefileMapper(article,dto.hashtags(),saveFileDtos);

        }
    public void articleHashtagSavefileMapper(Article article,Set<Hashtag.HashtagDto> hashtags , Set<SaveFile.SaveFileDto> saveFiles){
        for( Hashtag.HashtagDto hashtag: hashtags){
            Hashtag hashtag1 = hashtagRepository.findByHashtag(hashtag.hashtag()).orElseGet(()-> hashtagRepository.save(hashtag.toEntity()));
            articlehashtagrepository.save(ArticleHashtag.of(article,hashtag1));
        }
        for (SaveFile.SaveFileDto saveFileDto : saveFiles) {
            if(article.getContent().contains(saveFileDto.fileName())){
                articleSaveFileRepository.save(ArticleSaveFile.of(article,saveFileDto.toEntity()));
            }
        }
    }

    @Transactional(readOnly = true)
    public Hashtag.HashtagDto getHashtag(String hashtag) {
        return Hashtag.HashtagDto.from(hashtagRepository.findByHashtag(hashtag).orElseThrow(()-> new EntityNotFoundException("해시태그가 없습니다 - hashtag: " + hashtag)));
    }

    public Article articleHashtagUpdateNull(Long articleId){
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new EntityNotFoundException("게시글이 없습니다 - articleId: " + articleId));
        articlehashtagrepository.findByArticleId(articleId).forEach(articleHashtag -> {
            articleHashtag.setArticle(null);
            articleHashtag.setHashtag(null);
        });
        return article;
    }
}

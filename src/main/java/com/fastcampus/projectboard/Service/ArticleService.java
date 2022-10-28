package com.fastcampus.projectboard.Service;

import com.fastcampus.projectboard.domain.Article;
import com.fastcampus.projectboard.domain.ArticleHashtag;
import com.fastcampus.projectboard.domain.Hashtag;
import com.fastcampus.projectboard.domain.type.SearchType;
import com.fastcampus.projectboard.dto.ArticleDto;
import com.fastcampus.projectboard.dto.ArticleWithCommentDto;
import com.fastcampus.projectboard.dto.HashtagDto;
import com.fastcampus.projectboard.dto.request.ArticleRequest;
import com.fastcampus.projectboard.repository.ArticleHashtagRepository;
import com.fastcampus.projectboard.repository.ArticleRepository;
import com.fastcampus.projectboard.repository.HashtagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.HashSet;
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

    @Transactional(readOnly = true)
    public Page<ArticleDto> searchArticles(SearchType searchType, String searchKeyword, Pageable pageable) {
        if (searchKeyword == null || searchKeyword.isBlank()) {
            return articleRepository.findAll(pageable).map(ArticleDto::from);
        }

        return switch (searchType) {
            case TITLE -> articleRepository.findByTitleContaining(searchKeyword, pageable).map(ArticleDto::from);
            case CONTENT -> articleRepository.findByContentContaining(searchKeyword, pageable).map(ArticleDto::from);
            case ID -> articleRepository.findByUserAccount_UserIdContaining(searchKeyword, pageable).map(ArticleDto::from);
            case NICKNAME -> articleRepository.findByUserAccount_NicknameContaining(searchKeyword, pageable).map(ArticleDto::from);
            case HASHTAG -> null;
        };
    }

    @Transactional(readOnly = true)
    public ArticleWithCommentDto getArticle(Long articleId) {
        Set<HashtagDto> hashtags =  new HashSet<>();
        articlehashtagrepository.findByArticleId(articleId).stream().forEach(articleHashtag -> {
            hashtags.add(HashtagDto.from(articleHashtag.getHashtag()));
        });
        ArticleWithCommentDto article =articleRepository.findById(articleId)
                .map(ArticleWithCommentDto::from)
                .orElseThrow(() -> new EntityNotFoundException("게시글이 없습니다 - articleId: " + articleId));
        article.setHashtags(hashtags);
        return article;
    }

    public void saveArticle(ArticleDto dto, Set<HashtagDto> hashtagDto) {
        articleRepository.save(dto.toEntity());
        for (HashtagDto hashtag : hashtagDto) {
                ArticleHashtag articleHashtag = new ArticleHashtag();
                articleHashtag.setArticle(articleRepository.getReferenceById(articleRepository.count()));
                hashtagRepository.findByHashtag(hashtag.hashtag()).orElseGet(()-> hashtagRepository.save(hashtag.toEntity()));
                articleHashtag.setHashtag(hashtagRepository.findByHashtag(hashtag.hashtag()).get());
                articlehashtagrepository.save(articleHashtag);

        }
    }
        public void updateArticle (Long articleId, ArticleRequest dto){
            Article article = articleRepository.findById(articleId)
                    .orElseThrow(() -> new EntityNotFoundException("게시글이 없습니다 - articleId: " + articleId));
            article.setTitle(dto.getTitle());
            articlehashtagrepository.findByArticleId(articleId).stream().forEach(articleHashtag -> {
                articleHashtag.setArticle(null);
                articleHashtag.setHashtag(null);
            });
            article.setContent(dto.getContent());
            for( HashtagDto hashtag: dto.getHashtags()){
                ArticleHashtag articleHashtag = new ArticleHashtag();
                articleHashtag.setArticle(articleRepository.getReferenceById(articleId));
                hashtagRepository.findByHashtag(hashtag.hashtag()).orElseGet(()-> hashtagRepository.save(hashtag.toEntity()));
                articleHashtag.setHashtag(hashtagRepository.findByHashtag(hashtag.hashtag()).get());
                articlehashtagrepository.save(articleHashtag);
            }
        }



    public ArticleDto getArticleDto2(Long articleId) {
        return articleRepository.findById(articleId).map(ArticleDto::from).orElseThrow(() -> new EntityNotFoundException("게시글이 없습니다 - articleId: " + articleId));

    }

    public void deleteArticle(long articleId) {
        articleRepository.deleteById(articleId);
    }

}

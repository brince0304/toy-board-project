package com.fastcampus.projectboard.Util;

import com.fastcampus.projectboard.domain.Article;
import com.fastcampus.projectboard.domain.ArticleComment;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisUtil {


    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    private final String REDIS_KEY_PREFIX = "LOGOUT_";

    private final String EXPIRED_DURATION = "EXPIRE_DURATION";

    private final ObjectMapper mapper;

    public boolean isFirstIpRequestForView(String clientAddress, Long articleId) {
        String key = generateViewKey(clientAddress, articleId);
        return !Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    public void writeClientRequestForView(String clientAddress, Long articleId) {
        String key = generateViewKey(clientAddress, articleId);
        stringRedisTemplate.opsForValue().set("LIKE_COUNT:"+key, "id"+articleId);
        stringRedisTemplate.expire(key, 60*60*24 , TimeUnit.SECONDS);
    }
    public boolean isFirstIpRequestForLike(String clientAddress, Long articleId) {
        String key = generateLikeKey(clientAddress, articleId);
        return !Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    public void writeClientRequestForLike(String clientAddress, Long articleId) {
        String key = generateLikeKey(clientAddress, articleId);
        stringRedisTemplate.opsForValue().set("VIEW_COUNT:"+key, "id"+articleId);
        stringRedisTemplate.expire(key, 60*60*24 , TimeUnit.SECONDS);
    }

    public void saveDeletedArticle(Article.ArticleDto article) throws JsonProcessingException {
        stringRedisTemplate.opsForValue().set("DELETED_ARTICLE: "+article.id(),mapper.writeValueAsString(article),365,TimeUnit.DAYS);
    }

    public void saveDeletedArticleComments(Set<ArticleComment.ArticleCommentDto> articleComments) throws JsonProcessingException {
        articleComments.forEach(o->{
            try {
                stringRedisTemplate.opsForValue().set("DELETED_ARTICLE_COMMENT: "+o.id(), mapper.writeValueAsString(o),365,TimeUnit.DAYS);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void saveDeletedArticleComment(ArticleComment.ArticleCommentDto articleComment) throws JsonProcessingException {
        stringRedisTemplate.opsForValue().set("DELETED_ARTICLE_COMMENT: "+articleComment.id(),mapper.writeValueAsString(articleComment),365,TimeUnit.DAYS);
    }



    // key 형식 : 'client Address + postId' ->  '\xac\xed\x00\x05t\x00\x0f127.0.0.1 + 500'
    private String generateViewKey(String clientAddress, Long articleId) {
        return clientAddress + "view + " + articleId;
    }

    private String generateLikeKey(String clientAddress, Long articleId) {
        return clientAddress + "like + " + articleId;
    }

    public String getData(String key){
        ValueOperations<String,String> valueOperations = stringRedisTemplate.opsForValue();
        return valueOperations.get(key);
    }

    public void setDataExpire(String key,String value,long duration){
        ValueOperations<String,String> valueOperations = stringRedisTemplate.opsForValue();
        Duration expireDuration = Duration.ofMillis(duration);
        valueOperations.set(key,value,expireDuration);
    }

    public void deleteData(String key){
        stringRedisTemplate.delete(key);
    }
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    public void setBlackList(String key, Object o, Long second) {
          stringRedisTemplate.opsForValue().set(REDIS_KEY_PREFIX + key, o.toString(), Duration.ofMillis(second));
    }

    public boolean hasKeyBlackList(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(REDIS_KEY_PREFIX + key));
    }


}
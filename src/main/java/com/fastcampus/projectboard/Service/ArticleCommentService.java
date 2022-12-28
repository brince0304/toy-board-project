package com.fastcampus.projectboard.Service;

import com.fastcampus.projectboard.Util.RedisUtil;
import com.fastcampus.projectboard.domain.Article;
import com.fastcampus.projectboard.domain.ArticleComment;
import com.fastcampus.projectboard.repository.ArticleCommentRepository;
import com.fastcampus.projectboard.repository.ArticleRepository;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class ArticleCommentService {

    private final ArticleRepository articleRepository;
    private final ArticleCommentRepository articleCommentRepository;
    private final RedisUtil redisUtil;


    @Transactional(readOnly = true)
    public Set<ArticleComment.ArticleCommentDto> searchArticleCommentsByArticleId(Long articleId) {
        if(!articleRepository.existsById(articleId)){
            throw new EntityNotFoundException();
        }
        else {
            return articleCommentRepository.findByArticle_Id(articleId).stream().filter(o->o.getDeleted().equals("N")).map(ArticleComment.ArticleCommentDto::from).collect(Collectors.toSet());
        }
    }


    public void saveArticleComment(ArticleComment.ArticleCommentDto dto) {
        Article article = articleRepository.findById(dto.articleId()).orElseThrow(EntityNotFoundException::new);
        ArticleComment comment = articleCommentRepository.save( dto.toEntity(article,dto.userAccountDto().toEntity()));
        comment.setIsParent("Y");
    }


    public void updateArticleComment(Long id,String content) {
        ArticleComment articleComment = articleCommentRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        if (content != null) { articleComment.setContent(content);}
    }

    public void deleteArticleComment(Long Id) {
        ArticleComment articleComment = articleCommentRepository.findById(Id).orElseThrow(EntityNotFoundException::new);
        articleComment.setDeleted("Y");
        articleComment.setParent(null);
    }


    @Transactional(readOnly = true)
    public ArticleComment.ArticleCommentDto getArticleComment(Long articleCommentId) {
        ArticleComment articleComment = articleCommentRepository.findById(articleCommentId).orElseThrow(EntityNotFoundException::new);
        return ArticleComment.ArticleCommentDto.from(articleComment);
    }



    public void saveChildrenComment(Long parentId, ArticleComment.ArticleCommentDto children) {
            ArticleComment parent = articleCommentRepository.findById(parentId).orElseThrow(EntityNotFoundException::new);
            ArticleComment articleComment = children.toEntity(parent.getArticle(),children.userAccountDto().toEntity());
            articleCommentRepository.save(articleComment);
            articleComment.setIsParent("N");
            articleComment.setParent(parent);
            Set<ArticleComment> set = parent.getChildren();
            set.add(articleComment);
            parent.setChildren(set);
    }


}
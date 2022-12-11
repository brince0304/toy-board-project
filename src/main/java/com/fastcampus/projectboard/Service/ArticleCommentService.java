package com.fastcampus.projectboard.Service;

import com.fastcampus.projectboard.domain.Article;
import com.fastcampus.projectboard.domain.ArticleComment;
import com.fastcampus.projectboard.repository.ArticleCommentRepository;
import com.fastcampus.projectboard.repository.ArticleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class ArticleCommentService {

    private final ArticleRepository articleRepository;
    private final ArticleCommentRepository articleCommentRepository;


    @Transactional(readOnly = true)
    public List<ArticleComment.ArticleCommentDto> searchArticleComments(Long articleId) {
        List<ArticleComment.ArticleCommentDto> articleCommentDtos = new ArrayList<>();
        articleCommentDtos = articleCommentRepository.findByArticle_Id(articleId).stream().map(ArticleComment.ArticleCommentDto::from).toList();
        return articleCommentDtos;
    }


    public void saveArticleComment(ArticleComment.ArticleCommentDto dto) {
        try {
            Article article = articleRepository.findById(dto.articleId()).orElseThrow(() -> new EntityNotFoundException("게시글이 없습니다 - articleId: " + dto.articleId()));
                ArticleComment comment = articleCommentRepository.save( dto.toEntity(article,dto.userAccountDto().toEntity()));
                comment.setIsParent("Y");
        } catch (EntityNotFoundException e) {
            log.warn("댓글 저장 실패. 댓글 작성에 필요한 정보를 찾을 수 없습니다 - {}", e.getLocalizedMessage());
        }
    }


    public void updateArticleComment(Long id,String content) {
        try {
            ArticleComment articleComment = articleCommentRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다 - id: " + id));
            if (content != null) { articleComment.setContent(content);}
        } catch (EntityNotFoundException e) {
            log.warn("댓글 업데이트 실패. 게시글을 찾을 수 없습니다 - dto: {}", e.getLocalizedMessage());
        }
    }

    public void deleteArticleComment(Long Id) {
        articleCommentRepository.findById(Id).ifPresent(o->o.setDeleted("Y"));
    }


    @Transactional(readOnly = true)
    public ArticleComment.ArticleCommentDto getArticleComment(Long articleCommentId) {
        ArticleComment articleComment = articleCommentRepository.findById(articleCommentId).orElseThrow();
        return ArticleComment.ArticleCommentDto.from(articleComment);
    }



    public void saveChildrenComment(Long parentId, ArticleComment.ArticleCommentDto children) {
        try {
            ArticleComment parent = articleCommentRepository.findById(parentId).orElseThrow(()-> new EntityNotFoundException("부모 댓글이 없습니다 - parentId: " + parentId));
            ArticleComment articleComment = children.toEntity(parent.getArticle(),children.userAccountDto().toEntity());
            articleCommentRepository.save(articleComment);
            articleComment.setIsParent("N");
            articleComment.setParent(parent);
            Set<ArticleComment> set = parent.getChildren();
            set.add(articleComment);
            parent.setChildren(set);
        } catch (EntityNotFoundException e) {
            log.warn("댓글 저장 실패. 댓글 작성에 필요한 정보를 찾을 수 없습니다 - {}", e.getLocalizedMessage());
        }
    }


}
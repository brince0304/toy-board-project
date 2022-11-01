package com.fastcampus.projectboard.Service;

import com.fastcampus.projectboard.domain.Article;
import com.fastcampus.projectboard.domain.ArticleComment;
import com.fastcampus.projectboard.domain.UserAccount;
import com.fastcampus.projectboard.dto.ArticleDto;
import com.fastcampus.projectboard.repository.ArticleCommentRepository;
import com.fastcampus.projectboard.repository.ArticleRepository;
import com.fastcampus.projectboard.dto.ArticleCommentDto;

import com.fastcampus.projectboard.dto.ArticleWithCommentDto;
import com.fastcampus.projectboard.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class ArticleCommentService {

    private final ArticleRepository articleRepository;
    private final ArticleCommentRepository articleCommentRepository;
    private final UserAccountRepository userAccountRepository;

    @Transactional(readOnly = true)
    public List<ArticleCommentDto> searchArticleComments(Long articleId) {
        List<ArticleCommentDto> articleCommentDtos = new ArrayList<>();
        articleCommentDtos = articleCommentRepository.findByArticle_Id(articleId).stream().map(ArticleCommentDto::from).toList();
        return articleCommentDtos;
    }


    public void saveArticleComment(ArticleCommentDto dto) {
        try {
            Article article = articleRepository.getReferenceById(dto.articleId());
            UserAccount userAccount = userAccountRepository.findById(dto.userAccountDto().userId()).orElseThrow();
            ArticleComment articleComment = ArticleComment.of(article,userAccount,dto.content());
            articleCommentRepository.save(articleComment);
        } catch (EntityNotFoundException e) {
            log.warn("댓글 저장 실패. 댓글 작성에 필요한 정보를 찾을 수 없습니다 - {}", e.getLocalizedMessage());
        }
    }
    public void updateArticleComment(Long id,String content) {
        try {
            ArticleComment articleComment = articleCommentRepository.getReferenceById(id);
            if (content != null) { articleComment.setContent(content);}
        } catch (EntityNotFoundException e) {
            log.warn("댓글 업데이트 실패. 게시글을 찾을 수 없습니다 - dto: {}", e.getLocalizedMessage());
        }
    }

    public void deleteArticleComment(Long Id) {

        articleCommentRepository.getReferenceById(Id).setDeleted("Y");
    }

    public ArticleCommentDto getArticleComment(Long articleCommentId) {
        ArticleComment articleComment = articleCommentRepository.findById(articleCommentId).orElseThrow();
        return ArticleCommentDto.from(articleComment);
    }
}
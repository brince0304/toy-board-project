package com.fastcampus.projectboard.repository;

import antlr.collections.List;
import com.fastcampus.projectboard.domain.ArticleComment;
import com.fastcampus.projectboard.domain.QArticleComment;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import javax.persistence.EntityManager;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public class ArticleCommentRepositoryCustomImpl implements ArticleCommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;




    @Override
    public Set<ArticleComment> getChildrenCommentIsNotDeleted(Long id) {
        Set<ArticleComment> list = new HashSet<>();
        QArticleComment qArticleComment = QArticleComment.articleComment;
        for(ArticleComment articleComment : queryFactory.selectFrom(qArticleComment).where(qArticleComment.parent.id.eq(id)).fetch()){
            if(articleComment.getDeleted().equals("N")){
                list.add(articleComment);
            }
        }
        return list;
}
}

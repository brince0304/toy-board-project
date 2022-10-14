package com.fastcampus.projectboard.repository;

import com.fastcampus.projectboard.domain.Article;
import com.fastcampus.projectboard.domain.ArticleComment;
import com.fastcampus.projectboard.domain.QArticle;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringExpression;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
//jpa repository (setter 로 이뤄진 생성자 객체와 long id 로 구분)


@RepositoryRestResource
public interface ArticleRepository extends
        QuerydslPredicateExecutor<Article>  //entity 클래스 (모든 필드의 기본 검색기능을 추가해줌)
        ,QuerydslBinderCustomizer<QArticle> //Q클래스
        ,JpaRepository<Article, Long> {
    Page<Article> findByTitle(String title, Pageable pageable);
    @Override   //Querydsl 를 이용해서 상세 조건 설
    default void customize(QuerydslBindings bindings, QArticle root){
        bindings.excludeUnlistedProperties(true);
        bindings.including(root.title,root.hashtag,root.createdAt,root.createdBy,root.content);
        bindings.bind(root.title).first((StringExpression::containsIgnoreCase));
        bindings.bind(root.hashtag).first((StringExpression::containsIgnoreCase));
        bindings.bind(root.createdBy).first((StringExpression::containsIgnoreCase));
        bindings.bind(root.createdAt).first((DateTimeExpression::eq));
        bindings.bind(root.content).first((StringExpression::containsIgnoreCase));
    }
}
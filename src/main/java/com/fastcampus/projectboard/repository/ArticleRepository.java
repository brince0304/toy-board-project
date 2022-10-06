package com.fastcampus.projectboard.repository;

import com.fastcampus.projectboard.domain.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
//jpa repository (setter 로 이뤄진 생성자 객체와 long id 로 구분)

@RepositoryRestResource
public interface ArticleRepository extends JpaRepository<Article, Long> {
}
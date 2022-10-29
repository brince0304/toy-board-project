package com.fastcampus.projectboard.repository;

import com.fastcampus.projectboard.domain.ArticleHashtag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Set;

public interface ArticleHashtagRepository extends JpaRepository<ArticleHashtag, Long> {

    Set<ArticleHashtag> findByArticleId(Long articleId);
}


package com.fastcampus.projectboard.repository;

import com.fastcampus.projectboard.domain.ArticleComment;

import java.util.Set;

public interface ArticleCommentRepositoryCustom {
    Set<ArticleComment> getChildrenCommentIsNotDeleted(Long id);
}

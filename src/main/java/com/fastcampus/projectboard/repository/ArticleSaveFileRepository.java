package com.fastcampus.projectboard.repository;

import com.fastcampus.projectboard.domain.ArticleSaveFile;

import java.util.Optional;
import java.util.Set;

public interface ArticleSaveFileRepository extends org.springframework.data.jpa.repository.JpaRepository<com.fastcampus.projectboard.domain.ArticleSaveFile, java.lang.Long> {
    Set<ArticleSaveFile> getSaveFileByArticleId(Long articleId);

    void deleteBySaveFileId(Long deletedFileId);

    Optional<ArticleSaveFile> findBySaveFileId(Long saveFileId);

    void deleteByArticleId(long articleId);
}
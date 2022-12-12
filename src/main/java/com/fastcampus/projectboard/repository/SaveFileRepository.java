package com.fastcampus.projectboard.repository;

import com.fastcampus.projectboard.domain.SaveFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaveFileRepository extends JpaRepository<SaveFile, Long> {
    SaveFile findByFileName(String fileName);
}


package com.fastcampus.projectboard.repository;

import com.fastcampus.projectboard.domain.SaveFile;
import com.fastcampus.projectboard.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface SaveFileRepository extends JpaRepository<SaveFile, Long> {
    public SaveFile findByFileName(String fileName);
}


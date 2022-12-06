package com.fastcampus.projectboard.repository;

import com.fastcampus.projectboard.domain.File;
import com.fastcampus.projectboard.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface FileRepository extends JpaRepository<File, Long> {
    public File findByFileName(String fileName);
    public File findByUserAccount(UserAccount userAccount);
}


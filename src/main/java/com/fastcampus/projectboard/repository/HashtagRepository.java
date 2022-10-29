package com.fastcampus.projectboard.repository;

import com.fastcampus.projectboard.domain.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.Optional;

@RepositoryRestResource
public interface HashtagRepository extends JpaRepository<Hashtag, Long>{

    Optional<Hashtag> findByHashtag(String hashtag);
}


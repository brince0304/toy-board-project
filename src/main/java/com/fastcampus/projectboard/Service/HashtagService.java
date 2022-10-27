package com.fastcampus.projectboard.Service;

import com.fastcampus.projectboard.domain.Hashtag;
import com.fastcampus.projectboard.repository.HashtagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class HashtagService {

    private final HashtagRepository hashtagRepository;

    public void saveHashtags(Set<Hashtag> hashtags) {
        hashtags.stream().forEach(n-> {
            hashtagRepository.saveAll(hashtags);
        });
    }
    public void saveHashtag(Hashtag hashtag){
        hashtagRepository.save(hashtag);
    }
}

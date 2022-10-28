package com.fastcampus.projectboard.Service;

import com.fastcampus.projectboard.domain.Hashtag;
import com.fastcampus.projectboard.dto.HashtagDto;
import com.fastcampus.projectboard.repository.HashtagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class HashtagService {

    private final HashtagRepository hashtagRepository;

    public void saveHashtags(Set<HashtagDto> hashtags) {
        hashtags.stream()
                .map(HashtagDto::hashtag)
                .map(Hashtag::of)
                .forEach(t-> {
                    if(hashtagRepository.findByHashtag(t.getHashtag()).isEmpty()) {
                        hashtagRepository.save(t);
                    }
                });
    }
    public void saveHashtag(Hashtag hashtag){
        hashtagRepository.save(hashtag);
    }
}

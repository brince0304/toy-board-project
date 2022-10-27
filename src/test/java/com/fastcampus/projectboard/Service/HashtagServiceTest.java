package com.fastcampus.projectboard.Service;

import com.fastcampus.projectboard.domain.Hashtag;
import com.fastcampus.projectboard.repository.HashtagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
public class HashtagServiceTest {
    @InjectMocks
    private HashtagService sut;
    
    @Mock
    private HashtagRepository hashtagRepository;

    @Test
    void givenHashtagName_whenWritingHashtags_thenSavesHashtags() {
        //given
        String hashtagName = "test1, test2, test3";
        StringTokenizer st = new StringTokenizer(hashtagName, ",");
        Set<Hashtag> hashtags = new HashSet<>();
        while (st.hasMoreTokens()) {
            long id = 1L;
            hashtags.add(Hashtag.of(id,st.nextToken().trim()));
            id++;
        }
        given(hashtagRepository.saveAll(hashtags)).willReturn(hashtags.stream().collect(Collectors.toList()));

        //when
        sut.saveHashtags(hashtags);

        //then
        then(hashtagRepository).should().saveAll(hashtags);
    }
}

package com.fastcampus.projectboard.Service;

import com.fastcampus.projectboard.Util.RedisUtil;
import com.fastcampus.projectboard.domain.*;
import com.fastcampus.projectboard.domain.type.SearchType;
import com.fastcampus.projectboard.repository.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.persistence.EntityNotFoundException;
import javax.swing.text.html.parser.Entity;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;


@DisplayName("비즈니스 로직 - 게시글")
@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @InjectMocks private ArticleService sut;

    @Mock private ArticleRepository articleRepository;
    @Mock private HashtagRepository hashtagRepository;
    @Mock private ArticleHashtagRepository articleHashtagRepository;
    @Mock private ArticleSaveFileRepository articleSaveFileRepository;

    @Mock private SaveFileRepository saveFileRepository;
    @Mock private RedisUtil redisUtil;
    @Mock private StringRedisTemplate stringRedisTemplate;
    @DisplayName("게시글을 작성할 수 있다.")
    @Test
    void givenArticle_whenWritingArticle_thenWritesArticle() {
        //given
        Article article = createArticle();
        UserAccount userAccount = createUserAccount();
        given(articleRepository.save(any(Article.class))).willReturn(article);
        //when
        sut.saveArticle(createArticleRequest().toDto(UserAccount.UserAccountDto.from(userAccount)),createSaveFiles());

        //then
        then(articleRepository).should().save(any(Article.class));
    }
    @DisplayName("게시글을 조회할 수 있다.")
    @Test
    void givenArticle_whenReadingArticle_thenReadsArticle() {
        //given
        Article article = createArticle();
        given(articleRepository.findById(any(Long.class))).willReturn(Optional.of(article));
        //when
        sut.getArticle(1L);
        //then
        then(articleRepository).should().findById(any(Long.class));
    }

    @DisplayName("없는 게시글을 조회하면 예외가 발생한다.")
    @Test
    void givenNothing_whenReadingNonExistArticle_thenThrowsException() {
        //when
        Throwable throwable = catchThrowable(() -> sut.getArticle(1L));
        //then
        assertThat(throwable).isInstanceOf(EntityNotFoundException.class);
    }
    @DisplayName("없는 게시글을 삭제하면 예외가 발생한다.")
    @Test
    void givenNothing_whenDeletingNonExistArticle_thenThrowsException() {
        //when
        Throwable throwable = catchThrowable(() -> sut.deleteArticleByArticleId(1L));
        //then
        assertThat(throwable).isInstanceOf(EntityNotFoundException.class);
    }

    @DisplayName("게시글을 조회할 수 있으나 게시글에는 민감한 유저 정보가 포함되어 있지 않다.")
    @Test
    void givenArticle_whenReadingArticle_thenReadsArticleButThereIsNotContainedUserDetails() {
        //given
        Article article = createArticle();
        given(articleRepository.findById(any(Long.class))).willReturn(Optional.of(article));

        //when
        sut.getArticle(1L);
        //then
        then(articleRepository).should().findById(any(Long.class));
        assertThat(sut.getArticle(1L)).isNotInstanceOf(Article.ArticleDto.class);
    }
    @DisplayName("게시글을 수정할 수 있다.")
    @Test
    void givenArticleAndUpdateDetails_whenUpdatingArticle_thenUpdatesArticle() {
        //given
        Article article = createArticle();
        given(articleRepository.findById(any(Long.class))).willReturn(Optional.of(article));

        //when
        sut.updateArticle(1L,createArticleRequest().toDto(UserAccount.UserAccountDto.from(createUserAccount())),createSaveFiles());

        //then
        assertThat(article.getTitle()).isEqualTo(createArticleRequest().getTitle());

    }

    @DisplayName("게시글을 삭제할 수 있다.")
    @Test
    void givenArticleId_whenDeletingArticle_thenDeletesArticle() {
        //given
        Article article = createArticle();
        given(articleRepository.findById(any(Long.class))).willReturn(Optional.of(article));

        //when
        sut.deleteArticleByArticleId(1L);

        //then
        assertThat(article.getDeleted()).isEqualTo("Y");
    }

    @DisplayName("게시글을 검색할 수 있다.")
    @Test
    void givenSearchTypeAndKeyword_whenSearchingArticle_thenSearchesArticle() {
        //given
        Article article = createArticle();
        given(articleRepository.findByTitleContaining(any(String.class),any(Pageable.class))).willReturn(Page.empty());

        //when
        sut.searchArticles(SearchType.TITLE,"test",Pageable.unpaged());

        //then
        then(articleRepository).should().findByTitleContaining(any(String.class),any(Pageable.class));
    }

    @DisplayName("게시글을 검색할 수 있으나 민감한 유저의 정보가 포함되어 있지 않다.")
    @Test
    void givenSearchTypeAndKeyword_whenSearchingArticle_thenSearchesArticleButNotContainedUserDetails() {
        //given
        Article article = createArticle();
        given(articleRepository.findByTitleContaining(any(String.class),any(Pageable.class))).willReturn(Page.empty());

        //when
        sut.searchArticles(SearchType.TITLE,"test",Pageable.unpaged());

        //then
        then(articleRepository).should().findByTitleContaining(any(String.class),any(Pageable.class));
        assertThat(sut.searchArticles(SearchType.TITLE,"test",Pageable.unpaged())).doesNotHaveSameClassAs(Article.ArticleDto.class);
    }
    @DisplayName("해시태그를 등록할 수 있다.")
    @Test
    void givenArticleRequest_whenInsertingHashtag_thenSavesHashtagAndMapWithArticle() {
        //given
        Article article = createArticle();
        given(articleRepository.save(any(Article.class))).willReturn(article);
        given(hashtagRepository.save(any(Hashtag.class))).willReturn(createHashtag());
        given(articleHashtagRepository.save(any(ArticleHashtag.class))).willReturn(ArticleHashtag.of(article,createHashtag()));

        //when
        sut.saveArticle(createArticleRequest().toDto(UserAccount.UserAccountDto.from(createUserAccount())),createSaveFiles());

        //then
        then(hashtagRepository).should().save(any(Hashtag.class));
        then(articleHashtagRepository).should().save(any(ArticleHashtag.class));
    }


    @DisplayName("해시태그 연결 관계를 끊을 수 있다.")
    @Test
    void givenArticleId_whenDeletingHashtag_thenDeletesHashtagAndMapWithArticle() {
        //given
        Article article = createArticle();
        Long countHashtags = hashtagRepository.count();
        given(articleRepository.findById(any(Long.class))).willReturn(Optional.of(article));
        //when
        sut.updateArticle(any(Long.class),createArticleRequestForUpdating().toDto(UserAccount.UserAccountDto.from(createUserAccount())),createSaveFiles());

        //then
        then(articleHashtagRepository).should().findByArticleId(any(Long.class));
        assertThat(countHashtags).isEqualTo(hashtagRepository.count());
    }

    @Test
    @Disabled("컨트롤러 테스트에서 작성예정")
    void givenArticleId_whenViewingArticle_thenArticleViewCountIsUpdated() {
        //given
        Article article = createArticle();
        given(articleRepository.findById(any(Long.class))).willReturn(Optional.of(article));
        given(redisUtil.isFirstIpRequest(any(String.class),any(Long.class))).willReturn(false);

        //when
        sut.getArticle(1L);
        sut.updateViewCount("192.168.1.0",1L);

        //then
        then(articleRepository).should().findById(any(Long.class));
        then(redisUtil).should().isFirstIpRequest(any(String.class),any(Long.class));
    }

    @Test
    @Disabled("컨트롤러 테스트에서 작성예정")
    void givenArticleId_whenUpdateLikeCount_thenArticleLikeCountIsUpdated() {
        //given
        Article article = createArticle();
        given(articleRepository.findById(any(Long.class))).willReturn(Optional.of(article));
        given(redisUtil.isFirstIpRequest2(any(String.class),any(Long.class))).willReturn(false);

        //when
        sut.updateLikeCount("1",1L);

        //then
        then(articleRepository).should().findById(any(Long.class));
        then(redisUtil).should().isFirstIpRequest2(any(String.class),any(Long.class));
        assertThat(article.getLikeCount()).isEqualTo(1);
    }

    private SaveFile createSaveFile() {
        return SaveFile.builder()
                .id(1L)
                .fileName("test")
                .filePath("test")
                .fileSize(1L)
                .fileType("test")
                .build();

    }

    private Hashtag createHashtag(){
        return Hashtag.builder()
                .id(1L)
                .hashtag("test")
                .build();
    }

    private UserAccount createUserAccount() {
        return UserAccount.builder()
                .userId("userId")
                .userPassword("Tjrgus97!@")
                .nickname("nickname")
                .email("email@email.com")
                .build();
    }

    private Article createArticle() {
        return Article.of(createUserAccount(), "제목", "내용 test", null);
    }

    private Article.ArticleDto createArticleDto() {
        return createArticleDto("title", "content", "#java");
    }

    private Article.ArticleDto createArticleDto(String title, String content, String hashtag) {

        return Article.ArticleDto.builder()
                .userAccountDto(UserAccount.UserAccountDto.from(createUserAccount()))
                .title(title)
                .content(content)
                .hashtags(Hashtag.HashtagDto.from(hashtag))
                .build();
    }

    private Article.ArticleRequest createArticleRequest() {
        return Article.ArticleRequest.builder()
                .title("title")
                .content("content")
                .hashtag("#java")
                .build();
    }

    private Article.ArticleRequest createArticleRequestForUpdating() {
        return Article.ArticleRequest.builder()
                .title("title")
                .content("content")
                .hashtag("#java")
                .build();
    }

    private UserAccount.UserAccountDto createUserAccountDto() {
        return UserAccount.UserAccountDto.from(createUserAccount());
    }

    private Article.ArticleDtoWithSaveFiles createArticleWithCommentDto() {
        return Article.ArticleDtoWithSaveFiles.builder()
                .id(1L)
                .userAccountDto(createUserAccountDto())
                .title("title")
                .content("content")
                .createdAt(LocalDateTime.now())
                .createdBy("Uno")
                .modifiedAt(LocalDateTime.now())
                .modifiedBy("Uno")
                .deleted("N")
                .likeCount(0)
                .build();
    }

    private Set<SaveFile.SaveFileDto> createSaveFiles(){
        Set<SaveFile.SaveFileDto> saveFileDtos = new HashSet<>();
        saveFileDtos.add(SaveFile.SaveFileDto.builder()
                .id(1L)
                .fileName("fileName")
                .filePath("filePath")
                .fileSize(100L)
                .build());
        return saveFileDtos;

    }

}
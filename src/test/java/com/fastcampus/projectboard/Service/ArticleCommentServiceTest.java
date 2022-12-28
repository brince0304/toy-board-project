package com.fastcampus.projectboard.Service;

import com.fastcampus.projectboard.domain.*;
import com.fastcampus.projectboard.repository.ArticleCommentRepository;
import com.fastcampus.projectboard.repository.ArticleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;



@DisplayName("비즈니스 로직 - 댓글")
@ExtendWith(MockitoExtension.class)
class ArticleCommentServiceTest {

    @InjectMocks private ArticleCommentService sut;

    @Mock private ArticleRepository articleRepository;
    @Mock private ArticleCommentRepository articleCommentRepository;

    @Test
    @DisplayName("saveArticleComment() - 댓글 저장")
    void givenArticleId_whenSavingArticleComment_thenSavesArticleComment() {
        //given
        UserAccount account = createUserAccount();
        Article article = createArticle(account);
        ArticleComment articleComment = createArticleComment(article, account);
        ArticleComment.ArticleCommentDto dto = ArticleComment.ArticleCommentDto.from(articleComment);
        given(articleRepository.findById(any())).willReturn(Optional.of(article));
        given(articleCommentRepository.save(any())).willReturn(articleComment);
        //when
        sut.saveArticleComment(dto);

        //then
        then(articleCommentRepository).should().save(any(ArticleComment.class));
    }

    @Test
    @DisplayName("updateArticleComment() - 댓글 수정")
    void givenUpdatingDetailsandArticleCommentId_whenUpdatingArticleComment_thenUpdatesArticleComment() {
        //given
        UserAccount account = createUserAccount();
        Article article = createArticle(account);
        ArticleComment articleComment = createArticleComment(article, account);
        ArticleComment.ArticleCommentDto dto = ArticleComment.ArticleCommentDto.from(articleComment);
        given(articleCommentRepository.findById(any())).willReturn(Optional.of(articleComment));
        String content = articleComment.getContent();
        //when
        sut.updateArticleComment(articleComment.getId(),"updated content");

        //then
        then(articleCommentRepository).should().findById(any());
        assertThat(articleComment.getContent()).isNotEqualTo(content);
    }

    @Test
    @DisplayName("deleteArticleComment() - 댓글 삭제")
    void givenArticleCommentId_whenDeletingArticleComment_thenDeletesArticleComment() {
        //given
        UserAccount account = createUserAccount();
        Article article = createArticle(account);
        ArticleComment articleComment = createArticleComment(article, account);
        given(articleCommentRepository.findById(any())).willReturn(Optional.of(articleComment));
        //when
        sut.deleteArticleComment(articleComment.getId());

        //then
        then(articleCommentRepository).should().findById(any());
        assertThat(articleComment.getDeleted()).isEqualTo("Y");
    }


    @Test
    @DisplayName("searchArticleCommentsByArticleId() - 게시글 아이디로 댓글 조회시에 댓글 셋을 반환한다.")
    void givenArticleId_whenGettingArticleCommentByArticleId_thenGetsArticleComments() {
        //given
        UserAccount account = createUserAccount();
        Article article = createArticle(account);
        ArticleComment articleComment = createArticleComment(article, account);
        given(articleRepository.existsById(any())).willReturn(true);
        given(articleCommentRepository.findByArticle_Id(any())).willReturn(Set.of(articleComment));
        //when
        Set<ArticleComment.ArticleCommentDto> articleComments = sut.searchArticleCommentsByArticleId(article.getId());

        //then
        then(articleCommentRepository).should().findByArticle_Id(any());
        assertThat(articleComments.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("saveChildrenComment() - 대댓글을 저장한다.")
    void givenArticleCommentId_whenSavingChildrenComment_thenSavesChildrenComment() {
        //given
        UserAccount account = createUserAccount();
        Article article = createArticle(account);
        ArticleComment articleComment = createArticleComment(article, account);
        ArticleComment.ArticleCommentDto dto = ArticleComment.ArticleCommentDto.from(articleComment);
        given(articleCommentRepository.findById(any())).willReturn(Optional.of(articleComment));
        given(articleCommentRepository.save(any())).willReturn(articleComment);
        //when
        sut.saveChildrenComment(articleComment.getId(), dto);

        //then
        then(articleCommentRepository).should().findById(any());
        then(articleCommentRepository).should().save(any(ArticleComment.class));
    }

    @Test
    @DisplayName("saveChildrenComment() - 없는 댓글에 대댓글을 달 시에 예외를 발생시킨다.")
    void givenNotExistArticleCommentId_whenSavingChildrenComment_thenThrowsException() {
        //given
        UserAccount account = createUserAccount();
        Article article = createArticle(account);
        ArticleComment articleComment = createArticleComment(article, account);
        ArticleComment.ArticleCommentDto dto = ArticleComment.ArticleCommentDto.from(articleComment);
        given(articleCommentRepository.findById(any())).willReturn(Optional.empty());
        //when
        Throwable throwable = catchThrowable(() -> sut.saveChildrenComment(articleComment.getId(), dto));

        //then
        then(articleCommentRepository).should().findById(any());
        assertThat(throwable).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("searchArticleCommentsByArticleId() - 없는 게시글의 댓글을 조회 시에 예외를 발생시킨다.")
    void givenNothing_whenSearchingArticleCommentByArticleId_thenThrowsException() {
          //when&then
        Throwable throwable = catchThrowable(() -> sut.searchArticleCommentsByArticleId(null));
        assertThat(throwable).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("updateArticleComment() - 없는 댓글을 수정 시에 예외를 발생시킨다.")
    void givenNotExistArticleCommentId_whUpdateArticleCommentId_thenThrowsException() {
        //given
        given(articleCommentRepository.findById(any())).willReturn(Optional.empty());
        //when
        Throwable throwable = catchThrowable(() -> sut.updateArticleComment(1L, "updated content"));

        //then
        then(articleCommentRepository).should().findById(any());
        assertThat(throwable).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("deleteArticleComment() - 없는 댓글을 삭제 시에 예외를 발생시킨다.")
    void givenNotExistArticleCommentId_whenDeletesArticleCommentId_thenThrowsException() {
        //given
        given(articleCommentRepository.findById(any())).willReturn(Optional.empty());
        //when
        Throwable throwable = catchThrowable(() -> sut.deleteArticleComment(1L));

        //then
        then(articleCommentRepository).should().findById(any());
        assertThat(throwable).isInstanceOf(EntityNotFoundException.class);
    }

    private ArticleComment.ArticleCommentDto createArticleCommentDto(Article article, UserAccount userAccount) {
        return ArticleComment.ArticleCommentDto.builder()
                .articleId(article.getId())
                .content("content")
                .userAccountDto(UserAccount.UserAccountDto.from(userAccount))
                .build();
    }

    @Test
    @DisplayName("saveArticleComment() - 존재하지 않는 게시글에 댓글을 작성할 시에 예외를 던진다.")
    void givenNotExistArticleId_whenSavingArticleComment_thenThrowsException() {
        //given
        UserAccount account = createUserAccount();
        Article article = createArticle(account);
        ArticleComment articleComment = createArticleComment(article, account);
        given(articleRepository.findById(articleComment.getArticle().getId())).willReturn(Optional.empty());

        //when
        Throwable throwable = catchThrowable(() -> sut.saveArticleComment(ArticleComment.ArticleCommentDto.from(articleComment)));

        //then
        assertThat(throwable).isInstanceOf(EntityNotFoundException.class);

    }

    @Test
    @DisplayName("getArticleComment() - 댓글 단건 조회")
    void givenArticleCommentId_whenGetAnArticleComment_thenGetsArticleComment() {
        //given
        ArticleComment articleComment = createArticleComment(createArticle(createUserAccount()),createUserAccount());
        given(articleCommentRepository.findById(any())).willReturn(Optional.of(articleComment));

        //when
        ArticleComment.ArticleCommentDto articleCommentDto = sut.getArticleComment(articleComment.getId());

        //then
        then(articleCommentRepository).should().findById(any());
        assertThat(articleCommentDto).isNotNull();
    }

    @Test
    @DisplayName("getArticleComment() - 댓글 단건 조회시 없는 댓글을 조회하면 예외가 발생한다$")
    void givenNotExistArticleCommentId_whenGetAnArticleComment_thenThrowsException() {
        //given
        given(articleCommentRepository.findById(any())).willReturn(Optional.empty());

        //when
        Throwable throwable = catchThrowable(() -> sut.getArticleComment(1L));

        //then
        then(articleCommentRepository).should().findById(any());
        assertThat(throwable).isInstanceOf(EntityNotFoundException.class);
    }

    private ArticleComment.ArticleCommentRequest createArticleCommentRequest() {
        return ArticleComment.ArticleCommentRequest.builder()
                .articleId(1L)
                .content("content")
                .build();
    }


    private ArticleComment createArticleComment(Article article, UserAccount userAccount) {
        return ArticleComment.builder()
                .article(article)
                .content("content")
                .userAccount(userAccount)
                .children(new HashSet<>())
                .deleted("N")
                .build();
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

    private Article createArticle(UserAccount account) {
        return Article.builder()
                .id(1L)
                .title("title")
                .content("content")
                .userAccount(account)
                .build();
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
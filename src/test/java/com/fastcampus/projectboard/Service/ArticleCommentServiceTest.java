package com.fastcampus.projectboard.Service;

import com.fastcampus.projectboard.domain.*;
import com.fastcampus.projectboard.repository.ArticleCommentRepository;
import com.fastcampus.projectboard.repository.ArticleRepository;
import com.fastcampus.projectboard.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
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

    @Mock private UserAccountRepository userAccountRepository;

    @Mock private ArticleRepository articleRepository;
    @Mock private ArticleCommentRepository articleCommentRepository;

    @DisplayName("존재하지 않는 게시글에 댓글을 작성하면 예외가 발생한다.")
    @Test
    void givenArticleComment_whenWritingArticleComment_thenThrowException() {
        //given
        ArticleComment articleComment = createArticleComment();
        given(articleRepository.findById(any(Long.class))).willReturn(Optional.empty());

        //when
        Throwable throwable = catchThrowable(() -> sut.saveArticleComment(ArticleComment.ArticleCommentDto.from(articleComment)));

        //then
        assertThat(throwable).isInstanceOf(EntityNotFoundException.class);
    }



    private ArticleComment.ArticleCommentRequest createArticleCommentRequest() {
        return ArticleComment.ArticleCommentRequest.builder()
                .articleId(1L)
                .content("content")
                .build();
    }


    private ArticleComment createArticleComment() {
        return ArticleComment.builder()
                .id(1L)
                .content("content")
                .userAccount(createUserAccount())
                .article(createArticle())
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
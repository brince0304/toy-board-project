package Service;

import com.fastcampus.projectboard.domain.Article;
import com.fastcampus.projectboard.domain.UserAccount;
import com.fastcampus.projectboard.domain.type.SearchType;
import com.fastcampus.projectboard.repository.ArticleRepository;
import dto.ArticleDto;
import dto.ArticleUpdateDto;
import dto.UserAccountDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;


@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {


    @InjectMocks private ArticleService sut;

    @Mock private ArticleRepository articleRepository;


    @DisplayName("게시글 리스트를 반환한다")
    @Test
    void givenSearchParameters_whenSearchingArticles_thenReturnsArticlesList() {
        //Given

        //When
        Page<ArticleDto> articles = sut.searchArticles(SearchType.TITLE,"search keyword"); //제목,본문,id,닉네임,해시태그


        //Then
        assertThat(articles).isNotNull();
    }
    @DisplayName("게시글을 조회하면 게시글을 반환한다")
    @Test
    void givenId_whenSearchingArticle_thenReturnsArticle() {
        //Given

        //When
        ArticleDto articles = sut.searchArticles(1L); //제목,본문,id,닉네임,해시태그


        //Then
        assertThat(articles).isNull();
    }

    @DisplayName("없는 게시글을 조회하면, 예외를 던진다.")
    @Test
    void givenNonexistentArticleId_whenSearchingArticle_thenThrowsException() {
        // Given
        Long articleId = 0L;
        given(articleRepository.findById(articleId)).willReturn(Optional.empty());

        // When
        ArticleDto articles = sut.searchArticle(1L);
        Throwable t = catchThrowable(() -> sut.getArticle(articleId));

        // Then
        assertThat(articles).isNotNull();
        assertThat(t)
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("게시글이 없습니다 - articleId: " + articleId);
        then(articleRepository).should().findById(articleId);
    }
    @DisplayName("게시글 정보를 입력하면 게시글을 생성한다")
    @Test
    void givenArticleInfo_whenSavingArticle_thenSavesArticle() {
        //given
        given(articleRepository.save(any(Article.class))).willReturn(null);
        //when
        sut.saveArticle(ArticleDto.of(userAccountDto(),"haha","1234","hash"));

        //then
        then(articleRepository).should().save(any(Article.class));


    }
    @DisplayName("게시글의 ID와 수정정보를 입력하면 게시글을 수정한다")
    @Test
    void givenArticleIdAndModifyContent_whenModifyingArticle_thenUpdatesArticle() {
        //given
        Article article = createArticle();
        ArticleDto dto = createArticleDto("새 타이틀", "새 내용", "#springboot");
        given(articleRepository.getReferenceById(dto.id())).willReturn(article);
        //when
        sut.updateArticle(dto);

        //then
        then(articleRepository).should().save(any(Article.class));


    }

    private Article createArticle() {
        return Article.of(createUserAccount(),
                "haha","haha","hash");
    }

    private UserAccount createUserAccount() {
        return UserAccount.of(
                "brince2",
                "password",
                "brince@email.com",
                "brince",
                null
        );
    }
    private ArticleDto createArticleDto() {
        return createArticleDto("title", "content", "#java");
    }

    private ArticleDto createArticleDto(String title, String content, String hashtag) {
        return ArticleDto.of(1L,
                createUserAccountDto(),
                title,
                content,
                hashtag,
                LocalDateTime.now(),
                "Uno",
                LocalDateTime.now(),
                "Uno");
    }


    private UserAccountDto createUserAccountDto() {
        return UserAccountDto.of(
                "brince2",
                "1234",
                "brince@email.com",
                "brince",
                "haha"
        );
    }

    @DisplayName("게시글의 Id를 입력하면 게시글을 삭제한다.")
    @Test
    void givenArticleId_whenDeletingArticle_thenDeletesArticle() {
        //given
        willDoNothing().given(articleRepository).delete(any(Article.class));
        //when
        sut.deleteArticle(1L); //메소드 또 만들기

        //then
        then(articleRepository).should().delete(any(Article.class));
    }
    private UserAccountDto userAccountDto(){
        return UserAccountDto.of(
                "brince2",
                "1234",
                "brince@naver.com",
                "brince",
                "haha"
        );
    }
}
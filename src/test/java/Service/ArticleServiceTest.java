package Service;

import com.fastcampus.projectboard.domain.Article;
import com.fastcampus.projectboard.domain.type.SearchType;
import com.fastcampus.projectboard.repository.ArticleRepository;
import dto.ArticleDto;
import dto.ArticleUpdateDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
        assertThat(articles).isNotNull();
    }
    @DisplayName("게시글 정보를 입력하면 게시글을 생성한다")
    @Test
    void givenArticleInfo_whenSavingArticle_thenSavesArticle() {
        //given
        given(articleRepository.save(any(Article.class))).willReturn(null);
        //when
        sut.saveArticle(ArticleDto.of(LocalDateTime.now(),"brince","title","content","hashtag"));

        //then
        then(articleRepository).should().save(any(Article.class));


    }
    @DisplayName("게시글의 ID와 수정정보를 입력하면 게시글을 수정한다")
    @Test
    void givenArticleIdAndModifyContent_whenModifyingArticle_thenUpdatesArticle() {
        //given
        given(articleRepository.save(any(Article.class))).willReturn(null);
        //when
        sut.updateArticle(1L, ArticleUpdateDto.of("title","content","hashtag")); //메소드 또 만들기

        //then
        then(articleRepository).should().save(any(Article.class));


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
}
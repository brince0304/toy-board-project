package Service;

import com.fastcampus.projectboard.domain.Article;
import com.fastcampus.projectboard.domain.ArticleComment;
import com.fastcampus.projectboard.domain.UserAccount;
import com.fastcampus.projectboard.repository.ArticleCommentRepository;
import com.fastcampus.projectboard.repository.ArticleRepository;
import dto.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;


@ExtendWith(MockitoExtension.class)
class ArticleCommentServiceTest {
    @InjectMocks private ArticleCommentService sut;

    @Mock
    private ArticleCommentRepository articleCommentRepository;

    @Mock
    private ArticleRepository articleRepository;
    @DisplayName("게시글ID를 조회하면 해당하는 댓글 리스트를 받아온다")
    @Test
    void givenArticleId_whenSearchingComments_thenReturnsComments() {
        //given
        UserAccount account = UserAccount.of("brince","1234","brince@naver.com","brince","hhaa");
        Long articleId = 1L;
        given(articleRepository.findById(articleId))
                .willReturn(Optional.of(Article.of(account,"title","content","hashtag")));
        //when
        List<ArticleCommentDto> articleComments = sut.searchArticleComment();
        //then
        assertThat(articleComments).isNotNull();
        then(articleRepository).should().findById(articleId);
    }
    @DisplayName("댓글을 입력하면 댓글을 저장한다.")
    @Test
    void givenCommentInfo_whenSavingComment_thenSavesComment() {
        //given
        given(articleCommentRepository.save(any(ArticleComment.class))).willReturn(null);
        ArticleDto articleDto = ArticleDto.of(userAccountDto(),"haha","content","test");
        //when



        //then
        then(articleCommentRepository).should().save(any(ArticleComment.class));

    }

    @DisplayName("댓글의 ID와 수정정보를 입력하면 댓글을 수정한다")
    @Test
    void givenArticleCommentIdAndModifyContent_whenModifyingArticleComment_thenUpdatesArticleComment() {
        //given
        ArticleDto articleDto = ArticleDto.of(userAccountDto(),"haha","content","test");
        given(articleCommentRepository.save(any(ArticleComment.class))).willReturn(null);
        //when
        String update ="haha";
        sut.updateArticleComment(articleDto,1L, update); //메소드 또 만들기

        //then
        then(articleCommentRepository).should().save(any(ArticleComment.class));
    }

    @DisplayName("댓글의 ID를 입력하면 삭제한다")
    @Test
    void givenArticleCommentId_whenDeletingArticleComment_thenDeletesArticleComment() {
        //given
        willDoNothing().given(articleCommentRepository).delete(any(ArticleComment.class));
        //when
        sut.deleteArticleComment(1L); //메소드 또 만들기

        //then
        then(articleCommentRepository).should().delete(any(ArticleComment.class));
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
package com.fastcampus.projectboard.Controller;

import com.fastcampus.projectboard.Service.ArticleCommentService;
import com.fastcampus.projectboard.Service.ArticleService;
import com.fastcampus.projectboard.Service.UserService;
import com.fastcampus.projectboard.config.SecurityConfig;
import com.fastcampus.projectboard.domain.Article;
import com.fastcampus.projectboard.domain.ArticleComment;
import com.fastcampus.projectboard.domain.UserAccount;
import com.fastcampus.projectboard.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@DisplayName("view 컨트롤러 - 댓글")
@AutoConfigureMockMvc
@SpringBootTest
@Import(SecurityConfig.class)
public class ArticleCommentControllerTest {
    private final MockMvc mvc;



    @MockBean
    private final UserService userService;





    public ArticleCommentControllerTest(@Autowired MockMvc mvc, @Autowired UserService userService) {
        this.mvc = mvc;
        this.userService = userService;
    }

    @BeforeEach
    void setUp() {
        UserAccount userAccount = UserAccount.of(
                "test",
                "12341234",
                "test@email.com",
                "test",
                "test",
                null);

        Article article = Article.of(
                userAccount,"haha","haha"
        );

        ArticleComment articleComment = ArticleComment.
                of(article,userAccount,"haha");

        userService.saveUserAccount(UserAccountDto.from(userAccount));
    }

    @Test
    @WithUserDetails("test")
    void givenDetails_whenSavingArticleComment_thenSavesArticleComment() throws Exception {
        // given
        ArticleCommentDto dto = createArticleCommentDto("haha");
        //when&then

        mvc .perform(post("/articles/comments/1")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("content", dto.content())
                .param("articleId", dto.articleId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/articles/1"));
    }

    @Test
    @WithUserDetails("test")
    void givenDetails_whenSavingCommentReply_thenSavingCommentReply() throws Exception {
        // given
        ArticleCommentDto dto = createArticleCommentDto2("haha");
        //when&then

        mvc.perform(post("/articles/comments/1/reply")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("content", dto.content())
                        .param("articleId", dto.articleId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/articles/1"));
    }
    private ArticleCommentDto createArticleCommentDto(String content) {
        return ArticleCommentDto.of(
                1L,
                1L,
                createUserAccountDto(),
                content,
                LocalDateTime.now(),
                "uno",
                LocalDateTime.now(),
                "uno",
                "N",
                null,
                null
        );
    }

    private ArticleCommentDto createArticleCommentDto2(String content) {
        return ArticleCommentDto.of(
                2L,
                1L,
                createUserAccountDto(),
                content,
                LocalDateTime.now(),
                "uno",
                LocalDateTime.now(),
                "uno",
                "N",
                null,
                "N"
        );
    }




    private UserAccountDto createUserAccountDto() {
        return UserAccountDto.of(
                "uno",
                "password",
                "uno@mail.com",
                "Uno",
                "This is memo",
                LocalDateTime.now(),
                "uno",
                LocalDateTime.now(),
                "uno",
                Set.of()
        );
    }

    private ArticleComment createArticleComment(String content) {
        return ArticleComment.of(
                Article.of(createUserAccount(), "title", "content"),
                createUserAccount(),
                content
        );
    }

    private ArticleComment createArticleComment2(String content) {
        return ArticleComment.of(
                Article.of(createUserAccount(), "title", "content"),
                createUserAccount(),
                content,
                createArticleComment("댓글")
        );
    }

    private UserAccount createUserAccount() {
        return UserAccount.of(
                "uno",
                "password",
                "uno@email.com",
                "Uno",
                null,
                Set.of()
        );
    }

    private Article createArticle() {
        return Article.of(
                createUserAccount(),
                "title",
                "content"
        );
    }

}

package com.fastcampus.projectboard.Controller;

import com.fastcampus.projectboard.Service.ArticleCommentService;
import com.fastcampus.projectboard.Service.ArticleService;
import com.fastcampus.projectboard.Service.UserService;
import com.fastcampus.projectboard.config.SecurityConfig;
import com.fastcampus.projectboard.domain.Article;
import com.fastcampus.projectboard.domain.ArticleComment;
import com.fastcampus.projectboard.domain.UserAccount;
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
import java.io.IOException;
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
    void setUp() throws IOException {
        UserAccount.SignupDto signupDto = UserAccount.SignupDto.builder()
                .userId("test")
                .password1("Tjrgus97!@")
                .password2("Tjrgus97!@")
                .nickname("brince")
                .email("brince@email.com")
                .build();

        Article article = Article.of(
                signupDto.toEntity(),"haha","haha"
        );

        ArticleComment articleComment = ArticleComment.
                of(article,signupDto.toEntity(),"haha");

        userService.saveUserAccountWithoutProfile(signupDto);
    }


    @Test
    @WithUserDetails("test")
    void givenDetails_whenSavingArticleComment_thenSavesArticleComment() throws Exception {
        // given
        ArticleComment.ArticleCommentDto dto = ArticleComment.ArticleCommentDto.builder()
                .content("haha")
                .articleId(1L)
                .build();
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
        ArticleComment.ArticleCommentDto dto = ArticleComment.ArticleCommentDto.builder()
                .content("haha")
                .articleId(1L)
                .build();
        //when&then

        mvc.perform(post("/articles/comments/1/reply")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("content", dto.content())
                        .param("articleId", dto.articleId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/articles/1"));
    }















}

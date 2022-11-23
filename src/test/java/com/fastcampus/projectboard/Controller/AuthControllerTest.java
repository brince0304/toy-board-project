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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("View 컨트롤러 - 인증")
@AutoConfigureMockMvc
@SpringBootTest
@Import(SecurityConfig.class)
public class AuthControllerTest {

    private final MockMvc mvc;
    @MockBean
    private final UserService userService;
    @MockBean
    private final ArticleService articleService;
    @MockBean
    private final ArticleCommentService articleCommmentService;

    public AuthControllerTest(@Autowired MockMvc mvc, @Autowired UserService userService, @Autowired ArticleService articleService, @Autowired ArticleCommentService articleCommmentService) {
        this.mvc = mvc;
        this.userService = userService;
        this.articleService = articleService;
        this.articleCommmentService = articleCommmentService;
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

        userService.saveUserAccount(UserAccount.UserAccountDto.from(userAccount));
        articleService.saveArticle(Article.ArticleDto.from(article),null);
        articleCommmentService.saveArticleComment(ArticleComment.ArticleCommentDto.from(articleComment));
    }

    @DisplayName("로그인 페이지 - 정상호출")
    @Test
    public void givenNothing_whenTryingToLogIn_thenReturnsLogInView() throws Exception {
        //given

        //when & then
        mvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));

    }
    @WithUserDetails("test")
    @DisplayName("계정 마이페이지")
    @Test
    void givenNothing_whenViewingAccountDetails_thenReturnsAccountDetails() throws Exception {
        //given

        //when & then

        mvc.perform(get("/accounts")).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("user/my_page"))
                .andExpect(model().attribute("userAccount",userService.getUserAccount("test")));

    }

    @WithUserDetails("test")
    @DisplayName("계정 마이페이지 - 게시글 ")
    @Test
    void givenUserId_whenGettingArticlesByUserId_thenReturnsArticles() throws Exception {
        //given

        //when & then

        mvc.perform(get("/accounts/articles")).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }
}

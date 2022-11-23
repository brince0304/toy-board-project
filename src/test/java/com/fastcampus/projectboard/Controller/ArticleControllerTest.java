package com.fastcampus.projectboard.Controller;


import com.fastcampus.projectboard.Service.ArticleService;
import com.fastcampus.projectboard.Service.HashtagService;
import com.fastcampus.projectboard.Service.UserService;
import com.fastcampus.projectboard.config.SecurityConfig;
import com.fastcampus.projectboard.domain.Article;
import com.fastcampus.projectboard.domain.ArticleComment;
import com.fastcampus.projectboard.domain.Hashtag;
import com.fastcampus.projectboard.domain.UserAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("view 컨트롤러 - 게시글")
@AutoConfigureMockMvc
@SpringBootTest
@Import(SecurityConfig.class)
class ArticleControllerTest {

    private final MockMvc mvc;


    @MockBean
    private final ArticleService articleService;

    @MockBean
    private final HashtagService hashtagService;

    @MockBean
    private final UserService userService;

    ArticleControllerTest(
            @Autowired MockMvc mvc,
            @Autowired ArticleService articleService,
            @Autowired HashtagService hashtagService,
            @Autowired UserService userService) {
        this.mvc = mvc;
        this.articleService = articleService;
        this.hashtagService = hashtagService;
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

        userService.saveUserAccount(UserAccount.UserAccountDto.from(userAccount));
    }

    @DisplayName("[view][GET] 게시글 페이지 ")
    @Test
    public void givenNothing_whenRequestingArticlesView_thenReturnsArticlesView() throws Exception {
        //given
        given(articleService.searchArticles(eq(null),eq(null), any(Pageable.class))).willReturn(Page.empty());

        //when & then
        mvc.perform(get("/articles")).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("articles/index"))
                .andExpect(model().attributeExists("articles"));
        then(articleService).should().searchArticles(eq(null),eq(null), any(Pageable.class));

    }
    @DisplayName("[view][GET] 게시글 등록 페이지 ")
    @Test
    public void givenNothing_whenRequestingSavingArticleView_thenReturnsSavingArticleView() throws Exception {
        //given

        //when & then
        mvc.perform(get("/articles/post")).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("articles/post/article_form"));
    }

    @DisplayName("[view][POST] 게시글 등록 ")
    @Test
    @WithUserDetails("test")
    public void givenArticleInfo_whenSavingArticle_thenSavesArticle() throws Exception {
        //given
       Article.ArticleDto articleDto = Article.ArticleDto.from(createArticle());
        //when & then
        mvc.perform(post("/articles/post")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", articleDto.title())
                .param("content", articleDto.content())
                .param("hashtag", "haha"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/articles"));
    }



    @DisplayName("[view][GET] 게시글 상세 페이지")
    @Test
    public void givenNothing_whenRequestingArticleView_thenReturnsArticleView() throws Exception {
        //given
        Long articleId = 1L;
        given(articleService.getArticle(articleId)).willReturn(createArticleWithCommentsDto());
        //when & then
        mvc.perform(get("/articles/"+articleId)).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("articles/detail"))
                .andExpect(model().attributeExists("article"))
                .andExpect(model().attributeExists("articleComments"));
        then(articleService).should().getArticle(articleId);


    }
    @Disabled
    @Test
    void givenNothing_whenUpdatingArticle_thenUpdatesArticle() throws Exception {
        //given
        Article article = createArticle();
        Article newArticle = createArticle();
        Article.ArticleDto articleDto = Article.ArticleDto.from(newArticle);
        Long articleId = article.getId();


        //when

        //then
        mvc.perform(post("/articles/put/" +articleId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/articles/"+articleId));



    }

    @Disabled
    @DisplayName("[view][GET] 게시글 검색 페이지")
    @Test
    public void givenNothing_whenRequestingArticleSearchView_thenReturnsArticleSearchView() throws Exception {
        //given

        //when & then
        mvc.perform(get("/articles/search")).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_HTML))
                .andExpect(model().attributeExists("articles/search"));
    }
    @Disabled
    @DisplayName("[view][GET] 게시글 해시태그 검색 페이지")
    @Test
    public void givenNothing_whenRequestingArticleHashtagSearchView_thenReturnsArticleHashtagSearchView() throws Exception {
        //given

        //when & then
        mvc.perform(get("/articles/search-hashtag")).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_HTML))
                .andExpect(model().attributeExists("articles/search-hashtag"));

    }

    @Test
    void givenHashtag_whenSearchingArticlesByHashtag_thenGetsArticles() throws Exception {
        //given
        Hashtag hashtag = Hashtag.of(1L, "test");
        Set<Hashtag.HashtagDto> dto = new HashSet<>();
        dto.add(Hashtag.HashtagDto.from(hashtag));
        Article article = createArticle();
        userService.saveUserAccount(createUserAccountDto());
        articleService.saveArticle(Article.ArticleDto.from(article),dto);

        //when & then
        mvc.perform(get("/articles/search-hashtag/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_HTML))
                .andExpect(model().attributeExists("articles"))
                .andExpect(model().attributeExists("hashtag"));

    }


    private Article.ArticleWithCommentDto createArticleWithCommentsDto() {
        return Article.ArticleWithCommentDto.of(
                1L,
                createUserAccountDto(),
                Set.of(),
                "title",
                "content",
                LocalDateTime.now(),
                "uno",
                LocalDateTime.now(),
                String.valueOf(LocalDateTime.now()),
                "N",
                0,
                0


        );
    }
    private UserAccount createUserAccount() {
        return UserAccount.of(
                "uno",
                "password",
                "uno@email.com",
                "Uno",
                null,
                null
        );
    }

    private Article createArticle() {
        return Article.of(
                createUserAccount(),
                "title",
                "content");
    }

    private UserAccount.UserAccountDto createUserAccountDto() {
        return UserAccount.UserAccountDto.of(
                "uno",
                "pw",
                "uno@mail.com",
                "Uno",
                "memo",
                LocalDateTime.now(),
                "uno",
                LocalDateTime.now(),
                "uno",
                null
        );
    }







}
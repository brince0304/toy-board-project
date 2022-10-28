package com.fastcampus.projectboard.Controller;


import com.fastcampus.projectboard.Service.ArticleCommentService;
import com.fastcampus.projectboard.Service.ArticleService;
import com.fastcampus.projectboard.Service.HashtagService;
import com.fastcampus.projectboard.config.SecurityConfig;
import com.fastcampus.projectboard.domain.Article;
import com.fastcampus.projectboard.domain.Hashtag;
import com.fastcampus.projectboard.domain.UserAccount;
import com.fastcampus.projectboard.dto.ArticleDto;
import com.fastcampus.projectboard.dto.ArticleWithCommentDto;
import com.fastcampus.projectboard.dto.UserAccountDto;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@DisplayName("view 컨트롤러 - 게시글")
@WebMvcTest(ArticleController.class)
@Import(SecurityConfig.class)
class ArticleControllerTest {

    private final MockMvc mvc;
    @Autowired
    @MockBean private final ArticleCommentService articleCommentService;
    @Autowired
    @MockBean private final ArticleService articleService;

    @Autowired
    @MockBean private final HashtagService hashtagService;



    @Autowired
    ArticleControllerTest (MockMvc mvc, ArticleCommentService articleCommentService, ArticleService articleService, HashtagService hashtagService) {
        this.mvc = mvc;
        this.articleCommentService = articleCommentService;
        this.articleService = articleService;
        this.hashtagService = hashtagService;
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
        mvc.perform(get("/articles/create")).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("articles/create"));
    }

    @DisplayName("[view][POST] 게시글 등록 ")
    @Test
    public void givenArticleInfo_whenSavingArticle_thenSavesArticle() throws Exception {
        //given
       ArticleDto articleDto = ArticleDto.from(createArticle());
        //when & then
        mvc.perform(post("/articles/create")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", articleDto.title())
                .param("content", articleDto.content())
                .param("hashtags", articleDto.hashtags().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/articles"));
        then(articleService).should().saveArticle(any(ArticleDto.class));
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

    @Test
    void givenNothing_whenUpdatingArticle_thenUpdatesArticle() throws Exception {
        //given
        Article article = createArticle();
        Article newArticle = createArticle();
        newArticle.setHashtag("new hashtag"); //update hashtag
        ArticleDto articleDto = ArticleDto.from(newArticle);
        Long articleId = article.getId();


        //when
        articleService.updateArticle(articleId ,articleDto);

        //then
        mvc.perform(post("/articles/update/"+articleId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/articles/"+articleId));
        then(articleService).should().updateArticle(articleId,articleDto);



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

    private ArticleWithCommentDto createArticleWithCommentsDto() {
        return ArticleWithCommentDto.of(
                1L,
                createUserAccountDto(),
                Set.of(),
                "title",
                "content",
                "#java",
                LocalDateTime.now(),
                "uno",
                LocalDateTime.now(),
                "uno"
        );
    }
    private UserAccount createUserAccount() {
        return UserAccount.of(
                "uno",
                "password",
                "uno@email.com",
                "Uno",
                null
        );
    }

    private Article createArticle() {
        Set<Hashtag> hashtags = new HashSet<>();
        return Article.of(
                createUserAccount(),
                "title",
                "content",
                hashtags
        );
    }

    private UserAccountDto createUserAccountDto() {
        return UserAccountDto.of(
                "uno",
                "pw",
                "uno@mail.com",
                "Uno",
                "memo",
                LocalDateTime.now(),
                "uno",
                LocalDateTime.now(),
                "uno"
        );
    }







}
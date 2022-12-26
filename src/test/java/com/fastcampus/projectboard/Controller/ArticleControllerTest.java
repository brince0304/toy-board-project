package com.fastcampus.projectboard.Controller;


import com.fastcampus.projectboard.Annotation.WithPrincipal;
import com.fastcampus.projectboard.Service.ArticleService;
import com.fastcampus.projectboard.Service.SaveFileService;
import com.fastcampus.projectboard.Service.UserService;
import com.fastcampus.projectboard.Util.RedisUtil;
import com.fastcampus.projectboard.config.SecurityConfig;
import com.fastcampus.projectboard.domain.*;
import com.fastcampus.projectboard.repository.ArticleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
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
    private final SaveFileService saveFileService;
    private final ObjectMapper mapper;

    @MockBean
    private final RedisUtil redisUtil;

    @MockBean
    private final ArticleRepository articleRepository;

    @MockBean
    private final UserService userService;

    ArticleControllerTest(
            @Autowired MockMvc mvc,
            @Autowired ArticleService articleService,
            @Autowired SaveFileService saveFileService, @Autowired ObjectMapper mapper, @Autowired RedisUtil redisUtil, @Autowired ArticleRepository articleRepository, @Autowired UserService userService) {
        this.mvc = mvc;
        this.articleService = articleService;
        this.saveFileService = saveFileService;
        this.mapper = mapper;
        this.redisUtil = redisUtil;
        this.articleRepository = articleRepository;
        this.userService = userService;
    }


    @BeforeEach
    void setUp() throws IOException {
        userService.saveUserAccountWithoutProfile(UserAccount.SignupDto.from(createUserAccount()), SaveFile.SaveFileDto.builder().build());
        articleService.saveArticle(createArticleDto(), createSaveFiles());
    }

    @DisplayName("[view][GET] 게시글 페이지 ")
    @Test
    public void givenNothing_whenRequestingArticlesView_thenReturnsArticlesView() throws Exception {
        //given
        given(articleService.searchArticles(eq(null), eq(null), any(Pageable.class))).willReturn(Page.empty());
        //when & then
        mvc.perform(get("/articles")).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("articles/index"))
                .andExpect(model().attributeExists("articles"));
        then(articleService).should().searchArticles(eq(null), eq(null), any(Pageable.class));
    }

    @DisplayName("[view][GET] 게시글 등록 페이지 ")
    @Test
    @WithPrincipal
    public void givenNothing_whenRequestingSavingArticleView_thenReturnsSavingArticleView() throws Exception {
        //given

        //when & then
        mvc.perform(get("/articles/post")).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("articles/post/article_form"));
    }

    @DisplayName("[view][GET] 게시글 수정 페이지 ")
    @Test
    @WithPrincipal
    public void givenExistArticleDetails_whenRequestingUpdatingArticleView_thenReturnsUpdatingArticleView() throws Exception {
        //given
        given(articleService.getArticle(any(Long.class))).willReturn(createArticleDtoWithSaveFiles());
        //when & then
        mvc.perform(get("/articles/post/1")).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("articles/put/article_form"));
        then(articleService).should().getArticle(any(Long.class));
    }

    @DisplayName("[view][POST] 게시글 등록 ")
    @Test
    @WithUserDetails("test")
    public void givenArticleInfo_whenSavingArticle_thenSavesArticle() throws Exception {
        //given
        Article.ArticleRequest articleRequest = Article.ArticleRequest.builder()
                .title("haha421")
                .content("haha412")
                .fileIds("")
                .build();
        Article article = createArticle(createUserAccount());
        given(saveFileService.getFileDtosFromRequestsFileIds(any())).willReturn(new HashSet<>());
        given(articleService.saveArticle(any(), any())).willReturn(Article.ArticleDto.from(article));

        //when & then
        mvc.perform(post("/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(articleRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN));
        then(saveFileService).should().getFileDtosFromRequestsFileIds(any());
    }


    @DisplayName("[view][GET] 로그인이 되어있지 않은 상태로 포스트 페이지에 접속하면 리다이렉트 된다.")
    @Test
    public void givenNothing_whenTryingToGETArticlePostPage_thenRedirected() throws Exception {
        //given

        //when & then
        mvc.perform(get("/articles/post"))
                .andExpect(status().is3xxRedirection());
    }


    @DisplayName("[view][PUT] 로그인이 되어있지 않은 상태로 게시글을 수정하면 BAD_REQUEST를 반환한다.")
    @Test
    public void givenArticleDetails_whenTryingToPUTArticle_thenRedirected() throws Exception {
        Long articleId = 1L;
        Article article = articleRepository.getReferenceById(articleId);
        Article.ArticleRequest articleRequest = Article.ArticleRequest.builder()
                .articleId(articleId)
                .title("haha421")
                .content("haha412421412")
                .fileIds("")
                .build();
        given(saveFileService.getFileDtosFromRequestsFileIds(any())).willReturn(new HashSet<>());
        given(articleService.articleHashtagUpdateNull(any())).willReturn(article);
        given(articleService.getWriterFromArticle(any())).willReturn("test");
        //when & then
        mvc.perform(put("/articles").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(articleRequest)))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("[view][DELETE] 로그인이 되어있지 않은 상태로 게시글을 삭제하면 BAD_REQUEST를 반환한다.")
    @Test
    public void givenArticleId_whenTryingToDELETEArticle_thenReturnBadRequest() throws Exception {
        Map<String, String> articleId = Map.of("articleId", "1");
        //when&then
        mvc.perform(delete("/articles").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(articleId)))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("[view][GET] 게시글 상세 페이지")
    @Test
    @WithUserDetails("test")
    public void givenArticleId_whenRequestingArticleView_thenReturnsArticleView() throws Exception {
        //given
        Long articleId = 1L;
        given(articleService.getArticle(any())).willReturn(Article.ArticleDtoWithSaveFiles.from(createArticle(createUserAccount())));

        //when & then
        mvc.perform(get("/articles/" + articleId)).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("articles/detail"))
                .andExpect(model().attributeExists("article"));
        then(articleService).should().getArticle(articleId);
    }

    @DisplayName("[view][GET] 게시글을 조회하면 조회수를 업데이트한다.")
    @Test
    @WithUserDetails("test")
    public void givenArticleId_whenRequestingArticleView_thenArticleViewCountIsUpdated() throws Exception {
        //given
        Long articleId = 1L;
        given(articleService.getArticle(any())).willReturn(Article.ArticleDtoWithSaveFiles.from(createArticle(createUserAccount())));
        given(redisUtil.isFirstIpRequest(any(), any())).willReturn(true);

        //when & then
        mvc.perform(get("/articles/" + articleId)).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("articles/detail"))
                .andExpect(model().attributeExists("article"));
        then(articleService).should().getArticle(articleId);
        then(articleService).should().updateViewCount(any(), any());
    }

    @DisplayName("[view][GET] 좋아요 버튼을 누르면 좋아요를 추가한다.")
    @Test
    @WithUserDetails("test")
    public void givenArticleId_whenUpdatingLikeCount_thenArticleLikeCountIsUpdated() throws Exception {
        //given
        long articleId = 1L;
        given(articleService.updateLikeCount(any(), any())).willReturn(1);
        given(redisUtil.isFirstIpRequest2(any(), any())).willReturn(true);

        //when & then
        mvc.perform(post("/articles/" + articleId)).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN));
        then(articleService).should().updateLikeCount(any(), any());
    }

    @Test
    @WithUserDetails("test")
    @DisplayName("[view][PUT] 게시글 수정 ")
    void givenNothing_whenUpdatingArticle_thenUpdatesArticle() throws Exception {
        //given
        Long articleId = 1L;
        Article article = articleRepository.getReferenceById(articleId);
        Article.ArticleRequest articleRequest = Article.ArticleRequest.builder()
                .articleId(articleId)
                .title("haha421")
                .content("haha412421412")
                .fileIds("")
                .build();
        given(saveFileService.getFileDtosFromRequestsFileIds(any())).willReturn(new HashSet<>());
        given(articleService.articleHashtagUpdateNull(any())).willReturn(article);
        given(articleService.getWriterFromArticle(any())).willReturn("test");
        //when&then
        mvc.perform(put("/articles").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(articleRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN));
        then(saveFileService).should().getFileDtosFromRequestsFileIds(any());
        then(articleService).should().updateArticle(anyLong(), any(), any());
        then(articleService).should().getWriterFromArticle(any());
    }


    @Test
    @WithUserDetails("test")
    @DisplayName("[view][DELETE] 게시글 삭제")
    void givenArticleId_whenDeletingArticle_thenDeletesArticle() throws Exception {
        //given
        Map<String, String> articleId = Map.of("articleId", "1");
        given(articleService.getWriterFromArticle(any())).willReturn("test");

        //when&then
        mvc.perform(delete("/articles").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(articleId)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN));
        then(articleService).should().deleteArticleByArticleId(anyLong());
        then(saveFileService).should().deleteSaveFilesFromArticleId(anyLong());
        then(articleService).should().getWriterFromArticle(any());
    }


    @DisplayName("[view][GET] 게시글 검색 페이지")
    @Test
    @WithPrincipal
    public void givenNothing_whenRequestingArticleSearchView_thenReturnsArticleSearchView() throws Exception {
        //given
        String keyword = "haha";
        given(articleService.searchArticles(any(), anyString(), any(Pageable.class))).willReturn(Page.empty());
        //when & then
        mvc.perform(get("/articles").param("searchType", "TITLE").param("searchValue", keyword)).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("articles/index"))
                .andExpect(model().attributeExists("articles"));
        then(articleService).should().searchArticles(any(), anyString(), any(Pageable.class));
    }

    @DisplayName("[view][GET] 게시글 해시태그 검색 페이지")
    @Test
    public void givenNothing_whenRequestingArticleHashtagSearchView_thenReturnsArticleHashtagSearchView() throws Exception {
        //given
        given(articleService.getHashtag(anyString())).willReturn(Hashtag.HashtagDto.builder().build());
        //when & then
        mvc.perform(get("/articles/search-hashtag/123")).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(model().attributeExists("hashtag"))
                .andExpect(model().attributeExists("articles"))
                .andExpect(view().name("articles/tag/result"));
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

    private Hashtag createHashtag() {
        return Hashtag.builder()
                .id(1L)
                .hashtag("test")
                .build();
    }

    private UserAccount createUserAccount() {
        return UserAccount.builder()
                .userId("test")
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
        return Article.ArticleDto.builder()
                .userAccountDto(UserAccount.UserAccountDto.builder()
                        .nickname("nickname")
                        .userId("test")
                        .userPassword("Tjrgus97!@").build())
                .title("title")
                .content("content42421421")
                .build();
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
        return UserAccount.UserAccountDto.builder()
                .userId("test")
                .userPassword("Tjrgus97!@")
                .nickname("nickname")
                .build();

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

    private Set<SaveFile.SaveFileDto> createSaveFiles() {
        Set<SaveFile.SaveFileDto> saveFileDtos = new HashSet<>();
        saveFileDtos.add(SaveFile.SaveFileDto.builder()
                .id(1L)
                .fileName("fileName")
                .filePath("filePath")
                .fileSize(100L)
                .build());
        return saveFileDtos;
    }

    public Article.ArticleDtoWithSaveFiles createArticleDtoWithSaveFiles() {
        return Article.ArticleDtoWithSaveFiles.builder()
                .id(1L)
                .userAccountDto(createUserAccountDto())
                .title("title")
                .content("content")
                .hashtags(Hashtag.HashtagDto.from("#java"))
                .saveFiles(createSaveFiles())
                .build();
    }

}
package com.fastcampus.projectboard.Controller;

import com.fastcampus.projectboard.Annotation.WithPrincipal;
import com.fastcampus.projectboard.Service.ArticleCommentService;
import com.fastcampus.projectboard.Service.ArticleService;
import com.fastcampus.projectboard.Service.UserService;
import com.fastcampus.projectboard.config.SecurityConfig;
import com.fastcampus.projectboard.domain.Article;
import com.fastcampus.projectboard.domain.ArticleComment;
import com.fastcampus.projectboard.domain.SaveFile;
import com.fastcampus.projectboard.domain.UserAccount;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("view 컨트롤러 - 댓글")
@AutoConfigureMockMvc
@SpringBootTest
@Import(SecurityConfig.class)
public class ArticleCommentControllerTest {
    private final MockMvc mvc;
    @MockBean
    private final ArticleCommentService articleCommentService;

    private final ObjectMapper objectMapper;

    @MockBean
    private final UserService userService;


    public ArticleCommentControllerTest(@Autowired MockMvc mvc, @Autowired ArticleCommentService articleCommentService, @Autowired ObjectMapper objectMapper, @Autowired UserService userService) {
        this.mvc = mvc;
        this.articleCommentService = articleCommentService;
        this.objectMapper = objectMapper;
        this.userService = userService;
    }

    @BeforeEach
    void setUp() throws IOException {
        UserAccount.SignupDto signupDto = UserAccount.SignupDto.builder()
                .userId("testtest")
                .password1("Tjrgus97!@")
                .password2("Tjrgus97!@")
                .nickname("brincebrince")
                .email("brince@email.com")
                .build();

        Article article = Article.of(
                signupDto.toEntity(), "haha", "haha"
        );

        ArticleComment articleComment = ArticleComment.
                of(article, signupDto.toEntity(), "haha");

        userService.saveUserAccountWithoutProfile(signupDto, SaveFile.SaveFileDto.builder().build());
    }

    @Test
    @DisplayName("[view][GET] 댓글 아이디를 입력하면 댓글을 전달받는다.")
    void givenArticleCommentId_whenGettingArticleComment_thenGettingArticleCommentJson() throws Exception {
        //given
        given(articleCommentService.getArticleComment(any())).willReturn(ArticleComment.ArticleCommentDto.builder().build());

        //when&then
        mvc.perform(get("/articles/comments/1")).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("[view][GET] 없는 댓글 아이디를 입력하면 비어있는 결과를 리턴받는다.")
    void givenArticleCommentId_whenGettingArticleCommentButNotExist_thenGetsEmpty() throws Exception {
        //given
        given(articleCommentService.getArticleComment(any())).willReturn(null);
        //when&then
        mvc.perform(get("/articles/comments/1")).andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }
    @Test
    @WithPrincipal
    @DisplayName("[view][POST]댓글 작성")
    void givenDetails_whenSavingArticleComment_thenSavesArticleComment() throws Exception {
        // given
        ArticleComment.ArticleCommentDto dto = ArticleComment.ArticleCommentDto.builder()
                .content("haha")
                .articleId(1L)
                .build();
        //when&then

        mvc.perform(post("/articles/comments/1")
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
        then(articleCommentService).should().saveArticleComment(any());
    }

    @Test
    @DisplayName("[view][POST] 로그인이 되어있지 않은 상태에서 댓글을 작성하면 에러가 발생한다.")
    void givenDetails_whenSavingArticleCommentButNotAuthorized_thenGetsError() throws Exception {
        // given
        ArticleComment.ArticleCommentDto dto = ArticleComment.ArticleCommentDto.builder()
                .content("haha")
                .articleId(1L)
                .build();
        //when&then
        mvc.perform(post("/articles/comments/1")
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithPrincipal
    @DisplayName("[view][POST]대댓글 작성")
    void givenDetails_whenSavingCommentReply_thenSavingCommentReply() throws Exception {
        // given
        ArticleComment.ArticleCommentRequest dto = ArticleComment.ArticleCommentRequest.builder()
                .content("haha")
                .articleId(1L)
                .articleCommentId(1L)
                .build();
        //when&then
        mvc.perform(post("/articles/comments/reply").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
        then(articleCommentService).should().saveChildrenComment(any(), any());
    }

    @Test
    @WithPrincipal
    @DisplayName("[view][PUT] 댓글수정")
    void givenArticleCommentId_whenUpdatingArticleComment_thenUpdatesArticleComment() throws Exception {
        //given
        given(articleCommentService.getArticleComment(any())).willReturn(ArticleComment.ArticleCommentDto.builder().userAccountDto(UserAccount.UserAccountDto.builder()
                .userId("test").userPassword("test").build()).build());
        ArticleComment.ArticleCommentRequest dto = ArticleComment.ArticleCommentRequest.builder()
                .content("haha421421")
                .articleId(1L)
                .articleCommentId(1L)
                .build();
        //when&then
        mvc.perform(put("/articles/comments").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
        then(articleCommentService).should().updateArticleComment(any(), any());
    }

    @Test
    @DisplayName("[view][PUT] 내 댓글이 아닌 댓글을 수정하려면 오류가 발생한다.")
    void givenArticleCommentId_whenUpdatingArticleCommentButNotMine_thenGetsError() throws Exception {
        //given
        given(articleCommentService.getArticleComment(any())).willReturn(ArticleComment.ArticleCommentDto.builder().userAccountDto(UserAccount.UserAccountDto.builder()
                .userId("test").userPassword("test").build()).build());
        ArticleComment.ArticleCommentRequest dto = ArticleComment.ArticleCommentRequest.builder()
                .content("haha421421")
                .articleId(1L)
                .articleCommentId(1L)
                .build();
        //when&then
        mvc.perform(put("/articles/comments").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithPrincipal
    @DisplayName("[view][PUT] 없는 댓글을 수정하려면 오류가 발생한다.")
    void givenArticleCommentId_whenUpdatingArticleCommentButNotExist_thenGetsError() throws Exception {
        //given
        given(articleCommentService.getArticleComment(any())).willThrow(new EntityNotFoundException());
        ArticleComment.ArticleCommentRequest dto = ArticleComment.ArticleCommentRequest.builder()
                .content("haha421421")
                .articleId(1L)
                .articleCommentId(1L)
                .build();
        //when&then
        mvc.perform(put("/articles/comments").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithPrincipal
    @DisplayName("[view][DELETE] 댓글 삭제")
    void givenArticleCommentId_whenDeletesArticleComment_thenDeletesArticleComment() throws Exception {
        //given
        given(articleCommentService.getArticleComment(any())).willReturn(ArticleComment.ArticleCommentDto.builder().userAccountDto(UserAccount.UserAccountDto.builder().userId("test").userPassword("test").build()).build());
        Map<String, String> articleCommentId = Map.of("articleCommentId", "1");
        //when&then
        mvc.perform(delete("/articles/comments").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(articleCommentId)))
                .andExpect(status().isOk());
        then(articleCommentService).should().deleteArticleComment(any());
    }

    @Test
    @DisplayName("[view][DELETE] 인증받지 않거나 다른 사용자가 댓글을 삭제할때 에러를 발생시킨다.")
    void givenArticleCommentId_whenDeletesArticleCommentButNotAuthorized_thenGetsError() throws Exception {
        //given
        given(articleCommentService.getArticleComment(any())).willReturn(ArticleComment.ArticleCommentDto.builder().userAccountDto(UserAccount.UserAccountDto.builder().userId("test").userPassword("test").build()).build());
        Map<String, String> articleCommentId = Map.of("articleCommentId", "1");
        //when&then
        mvc.perform(delete("/articles/comments").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(articleCommentId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[view][DELETE] 없는 댓글을 삭제할때 에러가 발생한다.")
    void givenArticleCommentId_whenDeletesArticleCommentButNotExist_thenGetsError() throws Exception {
        //given
        given(articleCommentService.getArticleComment(any())).willReturn(null);
        Map<String, String> articleCommentId = Map.of("articleCommentId", "1");

        //when&then
        mvc.perform(delete("/articles/comments").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(articleCommentId)))
                .andExpect(status().isBadRequest());
    }
}

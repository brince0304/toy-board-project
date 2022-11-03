package com.fastcampus.projectboard.Controller;

import com.fastcampus.projectboard.Service.ArticleCommentService;
import com.fastcampus.projectboard.Service.UserService;
import com.fastcampus.projectboard.Util.CookieUtil;
import com.fastcampus.projectboard.Util.TokenProvider;
import com.fastcampus.projectboard.domain.forms.CommentForm;
import com.fastcampus.projectboard.dto.request.ArticleCommentRequest;
import com.fastcampus.projectboard.dto.security.BoardPrincipal;
import com.fastcampus.projectboard.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RequiredArgsConstructor
@Controller
@RequestMapping("/articles/comments")
public class ArticleCommentController {
    private final ArticleRepository articleRepository;
    private final ArticleCommentService articleCommentService;
    private final UserService userService;
    private final CookieUtil cookieUtil;
    private final TokenProvider tokenProvider;
    private final UserDetailsService userDetailsSErvice;
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{articleId}")
    public String writeArticleComment(@PathVariable Long articleId, ArticleCommentRequest dto,
                                      @Valid CommentForm commentForm,
                                      BindingResult bindingResult,
                                      HttpServletRequest req){
        if (bindingResult.hasErrors()) {
            return "redirect:/articles/"+articleId;
        }
        if(cookieUtil.getCookie(req,"refreshToken")==null){
            return "redirect:/login";
        }
        else if(cookieUtil.getCookie(req,"refreshToken")!=null){
            String username = tokenProvider.getUsername(cookieUtil.getCookie(req,"refreshToken").getValue());
            BoardPrincipal principal = (BoardPrincipal) userDetailsSErvice.loadUserByUsername(username);
            articleCommentService.saveArticleComment(dto.toDto(principal.toDto()));
            return "redirect:/articles/"+articleId;
        }
        return "redirect:/articles/"+articleId;
    } //현재 접속한 사용자의 정보를 받아와 DTO로 넘겨주고 , 댓글을 작성함


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{articleId}/{articleCommentId}")
    public String deleteArticleComment(@PathVariable Long articleCommentId
            ,@PathVariable Long articleId,
                                      HttpServletResponse res,HttpServletRequest req) {
        if (cookieUtil.getCookie(req, "refreshToken") == null) {
            return "redirect:/login";
        } else if (cookieUtil.getCookie(req, "refreshToken") != null) {
            String username = tokenProvider.getUsername(cookieUtil.getCookie(req, "refreshToken").getValue());
            if (articleCommentService.getArticleComment(articleCommentId).userAccountDto().userId().equals(username)) {
                articleCommentService.deleteArticleComment(articleCommentId);
                return "redirect:/articles/" + articleId;

            }
        }
        return "redirect:/articles/" + articleId;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/put/{articleId}/{articleCommentId}")
    public String updateArticleComment(@PathVariable Long articleCommentId
            ,@PathVariable Long articleId
            ,@Valid CommentForm commentForm, BindingResult bindingResult,ArticleCommentRequest request,
                                      HttpServletRequest req) {
        if (bindingResult.hasErrors()) {
            return "redirect:/articles/"+articleId;
        }
        if (cookieUtil.getCookie(req, "refreshToken") == null) {
            return "redirect:/login";
        } else if (cookieUtil.getCookie(req, "refreshToken") != null) {
            String username = tokenProvider.getUsername(cookieUtil.getCookie(req, "refreshToken").getValue());
            if (articleCommentService.getArticleComment(articleCommentId).userAccountDto().userId().equals(username)) {
                articleCommentService.updateArticleComment(articleCommentId,request.content());
                return "redirect:/articles/" + articleId;

            }
        }
        return "redirect:/articles/" + articleId;
    }


}

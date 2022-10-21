package com.fastcampus.projectboard.Controller;

import com.fastcampus.projectboard.Service.ArticleCommentService;
import com.fastcampus.projectboard.Service.UserService;
import com.fastcampus.projectboard.domain.ArticleComment;
import com.fastcampus.projectboard.domain.UserAccount;
import com.fastcampus.projectboard.domain.forms.ArticleForm;
import com.fastcampus.projectboard.domain.forms.CommentForm;
import com.fastcampus.projectboard.dto.ArticleCommentDto;
import com.fastcampus.projectboard.dto.request.ArticleCommentRequest;
import com.fastcampus.projectboard.dto.security.BoardPrincipal;
import com.fastcampus.projectboard.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

@RequiredArgsConstructor
@Controller
@RequestMapping("/articles/comments")
public class ArticleCommentController {
    private final ArticleRepository articleRepository;
    private final ArticleCommentService articleCommentService;
    private final UserService userService;
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{articleId}")
    public String writeArticleComment(@PathVariable Long articleId,ArticleCommentRequest dto,
                                      @Valid CommentForm commentForm,
                                      BindingResult bindingResult,
                                      @AuthenticationPrincipal BoardPrincipal principal){
        if (bindingResult.hasErrors()) {
            return "redirect:/articles/"+articleId;
        }
        articleCommentService.saveArticleComment(dto.toDto(principal.toDto()));
        return "redirect:/articles/"+articleId;
    } //현재 접속한 사용자의 정보를 받아와 DTO로 넘겨주고 , 댓글을 작성함


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{articleId}/{articleCommentId}")
    public String deleteArticleComment(@PathVariable Long articleCommentId
            ,@PathVariable Long articleId){
        articleCommentService.deleteArticleComment(articleCommentId);
        return "redirect:/articles/"+articleId;
    } //로그인 유무를 확인하지만 어차피 본인 댓글이 아닐시에 버튼이 활성화 되지 않으므로 직접적인 접속은 막아놓지 않는다

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/update/{articleId}/{articleCommentId}")
    public String updateArticleComment(@PathVariable Long articleCommentId
            ,@PathVariable Long articleId
            ,@Valid CommentForm commentForm, BindingResult bindingResult,ArticleCommentRequest request){
        if (bindingResult.hasErrors()) {
            return "redirect:/articles/"+articleId;
        }
        articleCommentService.updateArticleComment(articleCommentId,request.content());
        return "redirect:/articles/"+articleId;
    }


}

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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.logging.Logger;

@RequiredArgsConstructor
@Controller
@RequestMapping("/articles/comments")
public class ArticleCommentController {
    private final UserController userController;
    private final Logger log = Logger.getLogger(this.getClass().getName());
    private final ArticleCommentService articleCommentService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{articleId}")
    public String writeArticleComment(@PathVariable Long articleId, @ModelAttribute("dto") @Valid ArticleCommentRequest dto,
                                      BindingResult bindingResult,
                                      @AuthenticationPrincipal BoardPrincipal principal) {
        if (bindingResult.hasErrors()) {
            log.info("bindingResult.hasErrors()");
            return "redirect:/articles/" + articleId;
        }

        if (principal == null) {
            return "redirect:/login";
        }
        articleCommentService.saveArticleComment(dto.toDto(principal.toDto()));
        return "redirect:/articles/" + articleId;
    }
    //현재 접속한 사용자의 정보를 받아와 DTO로 넘겨주고 , 댓글을 작성함

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{articleId}/reply")
    public String writeChildrenComment(@PathVariable Long articleId, ArticleCommentRequest dto, @AuthenticationPrincipal BoardPrincipal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        articleCommentService.saveChildrenComment(dto.parentId(), dto.toDto(principal.toDto()));
        return "redirect:/articles/" + articleId;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{articleId}/{articleCommentId}")
    public String deleteArticleComment(@PathVariable Long articleCommentId
            , @PathVariable Long articleId,
                                       @AuthenticationPrincipal BoardPrincipal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        if (articleCommentService.getArticleComment(articleCommentId).userAccountDto().userId().equals(principal.getUsername())) {
            articleCommentService.deleteArticleComment(articleCommentId);
            return "redirect:/articles/" + articleId;
        } else if (principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            articleCommentService.deleteArticleComment(articleCommentId);
            return "redirect:/articles/" + articleId;
        }
        return "redirect:/articles/" + articleId;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/put/{articleId}/{articleCommentId}")
    public String updateArticleComment(@PathVariable Long articleCommentId
            , @PathVariable Long articleId
            , @Valid CommentForm commentForm, BindingResult bindingResult, ArticleCommentRequest request,
                                       @AuthenticationPrincipal BoardPrincipal principal) {
        if (bindingResult.hasErrors()) {
            return "redirect:/articles/" + articleId;
        }
        if (principal == null) {
            return "redirect:/login?redirectURL=/articles/" + articleId;
        } else {
            if (articleCommentService.getArticleComment(articleCommentId).userAccountDto().userId().equals(principal.getUsername())) {
                articleCommentService.updateArticleComment(articleCommentId, request.content());
                return "redirect:/articles/" + articleId;

            }
        }
        return "redirect:/articles/" + articleId;
    }


}

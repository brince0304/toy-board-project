package com.fastcampus.projectboard.Controller;

import com.fastcampus.projectboard.Service.ArticleCommentService;
import com.fastcampus.projectboard.Service.UserService;
import com.fastcampus.projectboard.domain.ArticleComment;
import com.fastcampus.projectboard.domain.UserAccount;
import com.fastcampus.projectboard.domain.forms.ArticleForm;
import com.fastcampus.projectboard.domain.forms.CommentForm;
import com.fastcampus.projectboard.dto.ArticleCommentDto;
import com.fastcampus.projectboard.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public String writeArticleComment(@PathVariable Long articleId,
                                      @Valid CommentForm commentForm, BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            return "redirect:/articles/"+articleId;
        }
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserAccount account = userService.getUserAccount(userDetails.getUsername()).toEntity();
        ArticleComment articleComment = ArticleComment.of(articleRepository.getReferenceById(articleId),account,commentForm.getContent());
        ArticleCommentDto articleCommentDto = ArticleCommentDto.from(articleComment);
        articleCommentService.saveArticleComment(articleCommentDto);
        return "redirect:/articles/"+articleId;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{articleId}/{articleCommentId}")
    public String deleteArticleComment(@PathVariable Long articleCommentId
            ,@PathVariable Long articleId){
        articleCommentService.deleteArticleComment(articleCommentId);
        return "redirect:/articles/"+articleId;
    }

}

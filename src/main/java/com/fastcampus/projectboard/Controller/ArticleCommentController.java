package com.fastcampus.projectboard.Controller;

import com.fastcampus.projectboard.Service.ArticleCommentService;
import com.fastcampus.projectboard.Util.ControllerUtil;
import com.fastcampus.projectboard.domain.ArticleComment;
import com.fastcampus.projectboard.domain.UserAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;
import java.util.logging.Logger;

@RequiredArgsConstructor
@RestController
@RequestMapping("/articles/comments")
public class ArticleCommentController {
    private final Logger log = Logger.getLogger(this.getClass().getName());

    private final ArticleCommentService articleCommentService;

    @GetMapping("/{articleId}")
    public ResponseEntity<?> getComments(@PathVariable Long articleId) {
        return new ResponseEntity<>(articleCommentService.searchArticleComments(articleId), HttpStatus.OK);
    }


    @GetMapping("/c/{articleCommentId}")
    public ResponseEntity<?> getArticleComment(@PathVariable Long articleCommentId) {
        return new ResponseEntity<>(articleCommentService.getArticleComment(articleCommentId), HttpStatus.OK);
    }


    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{articleId}")
    public ResponseEntity<?> writeArticleComment(@PathVariable Long articleId, @RequestBody @Valid ArticleComment.ArticleCommentRequest dto,
                                              BindingResult bindingResult,
                                              @AuthenticationPrincipal UserAccount.BoardPrincipal principal) {
        if (bindingResult.hasErrors()) {
            log.info("bindingResult.hasErrors()");
            System.out.println(new ResponseEntity<>(ControllerUtil.getErrors(bindingResult), HttpStatus.BAD_REQUEST));

            return new ResponseEntity<>(ControllerUtil.getErrors(bindingResult), HttpStatus.BAD_REQUEST);
        }

        if (principal == null) {
            return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.BAD_REQUEST);
        }
        articleCommentService.saveArticleComment(dto.toDto(principal.toDto()));
        return new ResponseEntity<>("등록되었습니다.", HttpStatus.OK);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/reply")
    public ResponseEntity<?> writeChildrenComment(@RequestBody @Valid ArticleComment.ArticleCommentRequest dto,BindingResult bindingResult,  @AuthenticationPrincipal UserAccount.BoardPrincipal principal) {
        if(bindingResult.hasErrors()) {
            return new ResponseEntity<>(ControllerUtil.getErrors(bindingResult),HttpStatus.BAD_REQUEST);
        }
        if (principal == null) {
            return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.BAD_REQUEST);
        }
        articleCommentService.saveChildrenComment(dto.parentId(), dto.toDto(principal.toDto()));
        return new ResponseEntity<>("등록되었습니다.", HttpStatus.OK);
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping
    public ResponseEntity<String> deleteArticleComment(@RequestBody Map<String,String> articleCommentId,
                                       @AuthenticationPrincipal UserAccount.BoardPrincipal principal) {
        Long id = Long.parseLong(articleCommentId.get("articleCommentId"));
        if (principal == null) {
            return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.BAD_REQUEST);
        }
        if (articleCommentService.getArticleComment(id).userAccountDto().userId().equals(principal.getUsername())) {
            articleCommentService.deleteArticleComment(id);
            return new ResponseEntity<>("articleCommentDeleting Success", HttpStatus.OK);
        } else if (principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            articleCommentService.deleteArticleComment(id);
            return new ResponseEntity<>("articleCommentDeleting Success", HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>("삭제 권한이 없습니다.", HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping
    public ResponseEntity<?> updateArticleComment(@RequestBody @Valid ArticleComment.ArticleCommentRequest dto,BindingResult result,
                                       @AuthenticationPrincipal UserAccount.BoardPrincipal principal) {
        Long articleCommentId = dto.articleCommentId();
        String content = dto.content();
        if (result.hasErrors()) {
            return new ResponseEntity<>(ControllerUtil.getErrors(result), HttpStatus.BAD_REQUEST);
        }
        if (principal == null) {
            return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.BAD_REQUEST);
        } else {
            if (articleCommentService.getArticleComment(articleCommentId).userAccountDto().userId().equals(principal.getUsername())) {
                articleCommentService.updateArticleComment(articleCommentId, content);
                return new ResponseEntity<>("수정되었습니다.", HttpStatus.OK);

            }
        }
        return new ResponseEntity<>("수정에 실패하였습니다.", HttpStatus.BAD_REQUEST);
    }


}

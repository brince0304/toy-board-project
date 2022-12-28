package com.fastcampus.projectboard.Controller;

import com.fastcampus.projectboard.Messages.ErrorMessages;
import com.fastcampus.projectboard.Service.ArticleCommentService;
import com.fastcampus.projectboard.Util.ControllerUtil;
import com.fastcampus.projectboard.domain.ArticleComment;
import com.fastcampus.projectboard.domain.UserAccount;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.persistence.EntityNotFoundException;
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
        try{
        return new ResponseEntity<>(ArticleComment.ArticleCommentResponse.from(articleCommentService.searchArticleCommentsByArticleId(articleId)), HttpStatus.OK);
    }
        catch (EntityNotFoundException e){
            ModelAndView mav = new ModelAndView("redirect:/");
            return new ResponseEntity<>(mav, HttpStatus.NOT_FOUND);
        }
    }


    @GetMapping("/c/{articleCommentId}")
    public ResponseEntity<?> getArticleComment(@PathVariable Long articleCommentId) {
        try {
            return new ResponseEntity<>(ArticleComment.ArticleCommentResponse.from(articleCommentService.getArticleComment(articleCommentId)), HttpStatus.OK);
        }
        catch (EntityNotFoundException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


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
            return new ResponseEntity<>(ErrorMessages.ACCESS_TOKEN_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }
        articleCommentService.saveArticleComment(dto.toDto(principal.toDto()));
        return new ResponseEntity<>("등록되었습니다.", HttpStatus.OK);
    }

    @PostMapping("/reply")
    public ResponseEntity<?> writeChildrenComment(@RequestBody @Valid ArticleComment.ArticleCommentRequest dto,BindingResult bindingResult,  @AuthenticationPrincipal UserAccount.BoardPrincipal principal) {
        try {
            if (bindingResult.hasErrors()) {
                return new ResponseEntity<>(ControllerUtil.getErrors(bindingResult), HttpStatus.BAD_REQUEST);
            }
            if (principal == null) {
                return new ResponseEntity<>(ErrorMessages.ACCESS_TOKEN_NOT_FOUND, HttpStatus.BAD_REQUEST);
            }
            articleCommentService.saveChildrenComment(dto.parentId(), dto.toDto(principal.toDto()));
            return new ResponseEntity<>("등록되었습니다.", HttpStatus.OK);
        }
        catch (EntityNotFoundException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping
    public ResponseEntity<String> deleteArticleComment(@RequestBody Map<String,String> articleCommentId,
                                       @AuthenticationPrincipal UserAccount.BoardPrincipal principal) {
        try {
            Long id = Long.parseLong(articleCommentId.get("articleCommentId"));
            if (principal == null) {
                return new ResponseEntity<>(ErrorMessages.ACCESS_TOKEN_NOT_FOUND, HttpStatus.BAD_REQUEST);
            }
            if (articleCommentService.getArticleComment(id).userAccountDto().userId().equals(principal.getUsername())||
                    principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                articleCommentService.deleteArticleComment(id);
                return new ResponseEntity<>("articleCommentDeleting Success", HttpStatus.OK);
            }
            else {
                return new ResponseEntity<>(ErrorMessages.NOT_ACCEPTABLE, HttpStatus.BAD_REQUEST);
            }
        }
        catch (EntityNotFoundException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @PutMapping
    public ResponseEntity<?> updateArticleComment(@RequestBody @Valid ArticleComment.ArticleCommentRequest dto,BindingResult result,
                                       @AuthenticationPrincipal UserAccount.BoardPrincipal principal) {
        try {
            Long articleCommentId = dto.articleCommentId();
            String content = dto.content();
            if (result.hasErrors()) {
                return new ResponseEntity<>(ControllerUtil.getErrors(result), HttpStatus.BAD_REQUEST);
            }
            if (principal == null) {
                return new ResponseEntity<>(ErrorMessages.ACCESS_TOKEN_NOT_FOUND, HttpStatus.BAD_REQUEST);
            } else {
                if (articleCommentService.getArticleComment(articleCommentId).userAccountDto().userId().equals(principal.getUsername())) {
                    articleCommentService.updateArticleComment(articleCommentId, content);
                    return new ResponseEntity<>("수정되었습니다.", HttpStatus.OK);

                }
            }
            return new ResponseEntity<>(ErrorMessages.ENTITY_NOT_VALID, HttpStatus.BAD_REQUEST);
        }
        catch (EntityNotFoundException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


}

package com.fastcampus.projectboard.Controller;

import com.fastcampus.projectboard.Messages.ErrorMessages;
import com.fastcampus.projectboard.Service.*;
import com.fastcampus.projectboard.Util.ControllerUtil;
import com.fastcampus.projectboard.domain.*;
import com.fastcampus.projectboard.domain.type.SearchType;
import io.github.furstenheim.CopyDown;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/articles")
public class ArticleController {

    private final ArticleService articleService;

    private final SaveFileService saveFileService;


    @GetMapping
    public String getArticles(
            @RequestParam(required = false) SearchType searchType,
            @RequestParam(required = false) String searchValue,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
            , ModelMap map) {

        map.addAttribute("articles", articleService.searchArticles(searchType, searchValue, pageable).map(Article.ArticleResponse::from));
        return "articles/index";
    }

    @GetMapping("/{articleId}")
    public String getArticleByArticleId(@PathVariable Long articleId, ModelMap map, ArticleComment.ArticleCommentRequest articleCommentRequest,
                                        HttpServletRequest req) {
        try{
        Article.ArticleWithCommentResponse articleResponse = Article.ArticleWithCommentResponse.from(articleService.getArticle(articleId));
        articleService.updateViewCount(req.getRemoteAddr(), articleId);
        map.addAttribute("article", articleResponse);
        map.addAttribute("articleComments", articleResponse.articleCommentsResponse());
        map.addAttribute("dto", articleCommentRequest);}
        catch (EntityNotFoundException e){
            return "redirect:/articles";
        }

        return "articles/detail";
    }
    @ResponseBody
    @PostMapping("/{articleId}")
    public ResponseEntity<String> updateLikeCount(@PathVariable String articleId, HttpServletRequest req) {
        try {
            Integer count = articleService.updateLikeCount(req.getRemoteAddr(), Long.parseLong(articleId));
            return new ResponseEntity<>(String.valueOf(count), HttpStatus.OK);

        }
        catch (EntityNotFoundException e){
            return new ResponseEntity<>(ErrorMessages.ENTITY_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
    }


    @GetMapping("/search-hashtag/{hashtag}")
    public String searchArticlesByHashtag(@PathVariable String hashtag, ModelMap map) {
        map.addAttribute("articles", articleService.getArticlesByHashtag(hashtag));
        map.addAttribute("hashtag", articleService.getHashtag(hashtag));
        return "articles/tag/result";
    }

    @GetMapping("/post")
    public String getPostPage(Model model, Article.ArticleRequest articleRequest, @AuthenticationPrincipal UserAccount.BoardPrincipal principal) {
        model.addAttribute("dto", articleRequest);
        return principal !=null ? "articles/post/article_form" : "redirect:/login";
    }
    @ResponseBody
    @PostMapping
    public ResponseEntity<String> saveArticle(@RequestBody @Valid Article.ArticleRequest dto, BindingResult bindingResult,
                                              @AuthenticationPrincipal UserAccount.BoardPrincipal boardPrincipal) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(ErrorMessages.ENTITY_NOT_VALID, HttpStatus.BAD_REQUEST);
        }
        if (boardPrincipal == null) {
            return new ResponseEntity<>(ErrorMessages.ACCESS_TOKEN_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }
        Set<SaveFile.SaveFileDto> saveFileDtos = saveFileService.getFileDtosFromRequestsFileIds(dto);
        saveFileService.deleteUnuploadedFilesFromArticleContent(dto.getContent(), dto.getFileIds());
        return new ResponseEntity<>(articleService.saveArticle(dto.toDto(boardPrincipal.toDto()), saveFileDtos).id().toString(), HttpStatus.OK);
    }




    @PreAuthorize("isAuthenticated()")
    @GetMapping("/post/{articleId}")
    public String updateArticle(@PathVariable Long articleId, ModelMap map,
                                Article.ArticleRequest dto,
                                @AuthenticationPrincipal UserAccount.BoardPrincipal boardPrincipal) {
        try {
            Article.ArticleWithCommentDto articleDto = articleService.getArticle(articleId);
            dto.setContent(articleDto.getContent());
            dto.setTitle(articleDto.getTitle());
            if (!articleDto.getUserAccountDto().userId().equals(boardPrincipal.username())) {
                return "redirect:/articles";
            } else {
                dto.setFileIds(ControllerUtil.fileIdsToString(articleDto.getSaveFiles()));
                dto.setHashtag(ControllerUtil.hashtagsToString(articleDto.getHashtags()));
                CopyDown converter = new CopyDown();
                dto.setContent(converter.convert(dto.getContent()));
                map.addAttribute("dto", dto);
            }
            map.addAttribute("articleId", articleId);
            return "articles/put/article_form";
        }
        catch (EntityNotFoundException e){
            return "redirect:/articles";
        }
    }

    @ResponseBody
    @PutMapping
    public ResponseEntity<?> updateArticle(
            @RequestBody @Valid Article.ArticleRequest articleRequest,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserAccount.BoardPrincipal boardPrincipal) {
        try {
            if (bindingResult.hasErrors()) {
                return new ResponseEntity<>(ControllerUtil.getErrors(bindingResult), HttpStatus.BAD_REQUEST);
            }
            if (!Objects.equals(articleService.getWriterFromArticle(articleRequest.getArticleId()), boardPrincipal.username())) {
                return new ResponseEntity<>(ErrorMessages.ACCOUNT_NOT_MATCH, HttpStatus.BAD_REQUEST);
            }
            Set<SaveFile.SaveFileDto> saveFileDtos = saveFileService.getFileDtosFromRequestsFileIds(articleRequest);
            saveFileService.deleteUnuploadedFilesFromArticleContent(articleRequest.getContent(), Objects.requireNonNull(articleRequest.getFileIds()));
            articleService.updateArticle(articleRequest.getArticleId(), articleRequest.toDto(boardPrincipal.toDto()), saveFileDtos);
            return new ResponseEntity<>("articleUpdating Success", HttpStatus.OK);
        }
        catch (EntityNotFoundException e){
            return new ResponseEntity<>(ErrorMessages.ENTITY_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
    }

    @ResponseBody
    @DeleteMapping
    public ResponseEntity<?> deleteArticleByArticleId(@RequestBody Map<String,String> articleId, @AuthenticationPrincipal UserAccount.BoardPrincipal boardPrincipal) {
        try {
            Long aId = Long.parseLong(articleId.get("articleId"));
            if (articleService.getWriterFromArticle(aId).equals(boardPrincipal.getUsername())) {
                articleService.deleteArticleByArticleId(aId);
            } else if (boardPrincipal.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
                articleService.deleteArticleByAdmin(aId);
            } else {
                return new ResponseEntity<>(ErrorMessages.ACCOUNT_NOT_MATCH, HttpStatus.BAD_REQUEST);
            }
            saveFileService.deleteSaveFilesFromArticleId(aId);
            return new ResponseEntity<>("articleDeleting Success", HttpStatus.OK);
        }
        catch (EntityNotFoundException e){
            return new ResponseEntity<>(ErrorMessages.ENTITY_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
    }


}

package com.fastcampus.projectboard.Controller;

import com.fastcampus.projectboard.Service.*;
import com.fastcampus.projectboard.Util.ControllerUtil;
import com.fastcampus.projectboard.domain.Article;
import com.fastcampus.projectboard.domain.ArticleComment;
import com.fastcampus.projectboard.domain.Hashtag;
import com.fastcampus.projectboard.domain.UserAccount;
import com.fastcampus.projectboard.domain.type.SearchType;
import lombok.RequiredArgsConstructor;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Map;
import java.util.Set;


@RequiredArgsConstructor
@Controller
@RequestMapping("/articles")
public class ArticleController {

    private final ArticleService articleService;
    private final HashtagService hashtagService;


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
    public String getArticleByArticleId(@PathVariable Long articleId, ModelMap map, ArticleComment.ArticleCommentRequest dto,
                                        @AuthenticationPrincipal UserAccount.BoardPrincipal principal, HttpServletRequest req, HttpServletResponse res) {
        try{
        Article.ArticleWithCommentResponse article = Article.ArticleWithCommentResponse.from(articleService.getArticle(articleId));
        articleService.updateViewCount(req.getRemoteAddr(), articleId);
        map.addAttribute("article", article);
        map.addAttribute("articleComments", article.articleCommentsResponse());
        map.addAttribute("dto", dto);}
        catch (Exception e){
            return "redirect:/articles";
        }

        return "articles/detail";
    }
    @ResponseBody
    @PostMapping("/{articleId}")
    public ResponseEntity<String> updateLikeCount(@PathVariable String articleId, HttpServletRequest req) {
        Long id = Long.parseLong(articleId);
        Integer count = articleService.updateLikeCount(req.getRemoteAddr(), id);
        return new ResponseEntity<>(String.valueOf(count), HttpStatus.OK);
    }


    @GetMapping("/search-hashtag/{hashtag}")
    public String searchArticlesByHashtag(@PathVariable String hashtag, ModelMap map) {
        Set<Article.ArticleDto> set = hashtagService.getArticlesByHashtag(hashtag);
        Hashtag.HashtagDto dto = hashtagService.getHashtag(hashtag);
        map.addAttribute("articles", set);
        map.addAttribute("hashtag", dto);
        return "articles/tag/result";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/post")
    public String getPostPage(Model model, Article.ArticleRequest dto, @AuthenticationPrincipal UserAccount.BoardPrincipal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        model.addAttribute("dto", dto);
        return "articles/post/article_form";
    }
    @ResponseBody
    @PostMapping
    public ResponseEntity<String> saveArticle(@RequestBody @Valid Article.ArticleRequest dto, BindingResult bindingResult,
                                              @AuthenticationPrincipal UserAccount.BoardPrincipal boardPrincipal) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>("내용을 확인해주세요.", HttpStatus.BAD_REQUEST);
        }
        if (boardPrincipal == null) {
            return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.BAD_REQUEST);
        }
        Article.ArticleDto dto1 = articleService.saveArticle(dto.toDto(boardPrincipal.toDto()),dto.getHashtags());
        Long articleId = dto1.id();
        return new ResponseEntity<>(articleId.toString(), HttpStatus.OK);
    }
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/post/{articleId}")
    public String updateArticle(@PathVariable Long articleId, ModelMap map,
                                Article.ArticleRequest dto,
                                @AuthenticationPrincipal UserAccount.BoardPrincipal boardPrincipal) {
        Article.ArticleWithCommentDto articleDto = articleService.getArticle(articleId);
        dto.setContent(articleDto.getContent());
        dto.setTitle(articleDto.getTitle());
        if (!articleDto.getUserAccountDto().userId().equals(boardPrincipal.username())) {
            return "redirect:/articles";
        } else {
            dto.setHashtag(String.valueOf(ControllerUtil.hashtagsToString(articleDto.getHashtags())));
            map.addAttribute("dto", dto);
        }
        map.addAttribute("articleId", articleId);
        return "articles/put/article_form";
    }

    @ResponseBody
    @PutMapping
    public ResponseEntity<Object> updateArticle(
            @RequestBody @Valid Article.ArticleRequest dto,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserAccount.BoardPrincipal boardPrincipal) {
        if (boardPrincipal == null) {
            return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.BAD_REQUEST);
        }
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(ControllerUtil.getErrors(bindingResult), HttpStatus.BAD_REQUEST);
        }
        articleService.updateArticle(dto.getArticleId(), dto);
        return new ResponseEntity<>("articleUpdating Success", HttpStatus.OK);
    }

    @ResponseBody
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping
    public ResponseEntity<String> deleteArticleByArticleId(@RequestBody Map<String,String> articleId, @AuthenticationPrincipal UserAccount.BoardPrincipal boardPrincipal) {
        String id = boardPrincipal.getUsername();
        Long aId = Long.parseLong(articleId.get("articleId"));
        if (articleService.getArticle(aId).getUserAccountDto().userId().equals(id)) {
            articleService.deleteArticle(aId);
        } else if (boardPrincipal.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            articleService.deleteArticleByAdmin(aId);
        } else {
            return new ResponseEntity<>("게시글 삭제에 실패하였습니다.", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("articleDeleting Success", HttpStatus.OK);
    }

    public boolean isLoginUser(UserAccount.BoardPrincipal boardPrincipal) {
        return boardPrincipal.isEnabled();
    }

}

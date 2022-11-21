package com.fastcampus.projectboard.Controller;

import com.fastcampus.projectboard.Service.*;
import com.fastcampus.projectboard.Util.CookieUtil;
import com.fastcampus.projectboard.Util.TokenProvider;
import com.fastcampus.projectboard.domain.forms.CommentForm;
import com.fastcampus.projectboard.domain.type.SearchType;
import com.fastcampus.projectboard.domain.type.StatusEnum;
import com.fastcampus.projectboard.dto.ArticleDto;
import com.fastcampus.projectboard.dto.ArticleWithCommentDto;
import com.fastcampus.projectboard.dto.HashtagDto;
import com.fastcampus.projectboard.dto.MessageDto;
import com.fastcampus.projectboard.dto.request.ArticleCommentRequest;
import com.fastcampus.projectboard.dto.request.ArticleRequest;
import com.fastcampus.projectboard.dto.response.ArticleResponse;
import com.fastcampus.projectboard.dto.response.ArticleWithCommentResponse;
import com.fastcampus.projectboard.dto.security.BoardPrincipal;
import com.fastcampus.projectboard.repository.ArticleRepository;
import com.fastcampus.projectboard.repository.UserAccountRepository;
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
    public String articles(
            @RequestParam(required = false) SearchType searchType,
            @RequestParam(required = false) String searchValue,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
            , ModelMap map) {
        map.addAttribute("articles", articleService.searchArticles(searchType, searchValue, pageable).map(ArticleResponse::from));
        return "articles/index";
    }

    @GetMapping("/{articleId}")
    public String article(@PathVariable Long articleId, ModelMap map, ArticleCommentRequest dto,
                          CommentForm commentForm, @AuthenticationPrincipal BoardPrincipal principal, HttpServletRequest req, HttpServletResponse res) {
        ArticleWithCommentResponse article = ArticleWithCommentResponse.from(articleService.getArticle(articleId));
        articleService.updateViewCount(req.getRemoteAddr(), articleId);
        map.addAttribute("article", article);
        map.addAttribute("articleComments", article.articleCommentsResponse());
        map.addAttribute("dto", dto);
        return "articles/detail";
    }
    @ResponseBody
    @PostMapping("/{articleId}")
    public ResponseEntity<String> likeArticle(@PathVariable String articleId, HttpServletRequest req) {
        Long id = Long.parseLong(articleId);
        Integer count = articleService.updateLike(req.getRemoteAddr(), id);
        return new ResponseEntity<>(String.valueOf(count), HttpStatus.OK);
    }


    @GetMapping("/search-hashtag/{hashtag}")
    public String hashtag(@PathVariable String hashtag, ModelMap map) {
        Set<ArticleDto> set = hashtagService.getArticlesByHashtag(hashtag);
        HashtagDto dto = hashtagService.getHashtag(hashtag);
        map.addAttribute("articles", set);
        map.addAttribute("hashtag", dto);
        return "articles/tag/result";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/post")
    public String articleCreate(Model model, ArticleRequest dto, @AuthenticationPrincipal BoardPrincipal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        model.addAttribute("dto", dto);
        return "articles/post/article_form";
    }
    @ResponseBody
    @PostMapping
    public ResponseEntity<String> articleSave(@RequestBody @Valid ArticleRequest dto, BindingResult bindingResult,
                              @AuthenticationPrincipal BoardPrincipal boardPrincipal) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>("내용을 확인해주세요.", HttpStatus.BAD_REQUEST);
        }
        if (boardPrincipal == null) {
            return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.BAD_REQUEST);
        }
        ArticleDto dto1 = articleService.saveArticle(dto.toDto(boardPrincipal.toDto()),dto.getHashtags());
        Long articleId = dto1.id();
        return new ResponseEntity<>(articleId.toString(), HttpStatus.OK);
    }
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/post/{articleId}")
    public String articleUpdate(@PathVariable Long articleId, ModelMap map,
                                ArticleRequest dto,
                                @AuthenticationPrincipal BoardPrincipal boardPrincipal) {
        ArticleWithCommentDto articleDto = articleService.getArticle(articleId);
        dto.setContent(articleDto.getContent());
        dto.setTitle(articleDto.getTitle());
        if (!articleDto.getUserAccountDto().userId().equals(boardPrincipal.username())) {
            return "redirect:/articles";
        } else {
            Set<HashtagDto> hashtagDto = articleDto.getHashtags();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < hashtagDto.size(); i++) {
                if (i != hashtagDto.size() - 1) {
                    sb.append("#").append(hashtagDto.stream().toList().get(i).hashtag()).append(" ");
                } else {
                    sb.append("#").append(hashtagDto.stream().toList().get(i).hashtag());
                }
            }
            dto.setHashtag(String.valueOf(sb));
            map.addAttribute("dto", dto);
        }
        map.addAttribute("articleId", articleId);
        return "articles/put/article_form";
    }

    @ResponseBody
    @PutMapping
    public ResponseEntity<Object> updateArticle(
            @RequestBody @Valid ArticleRequest dto,
            BindingResult bindingResult,
            @AuthenticationPrincipal BoardPrincipal boardPrincipal) {
        if (boardPrincipal == null) {
          MessageDto mDto = new MessageDto();
            mDto.setMessage("로그인이 필요합니다.");
            return new ResponseEntity<>(mDto, HttpStatus.BAD_REQUEST);
        }
        if (bindingResult.hasErrors()) {
            MessageDto mDto = new MessageDto();
            mDto.setData(bindingResult.getAllErrors());
            mDto.setMessage("내용을 확인해주세요.");
            return new ResponseEntity<>(mDto, HttpStatus.BAD_REQUEST);
        }
        articleService.updateArticle(dto.getArticleId(), dto);
        MessageDto mDto = new MessageDto();
        mDto.setMessage("articleUpdating Success");
        mDto.setStatus(StatusEnum.OK);
        return new ResponseEntity<>(mDto, HttpStatus.OK);
    }

    @ResponseBody
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping
    public ResponseEntity<String> articleDelete(@RequestBody Map<String,String> articleId, @AuthenticationPrincipal BoardPrincipal boardPrincipal) {
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

    public boolean isLoginUser(BoardPrincipal boardPrincipal) {
        return boardPrincipal.isEnabled();
    }

}

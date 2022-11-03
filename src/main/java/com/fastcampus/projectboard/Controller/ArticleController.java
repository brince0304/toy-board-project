package com.fastcampus.projectboard.Controller;

import com.fastcampus.projectboard.Service.*;
import com.fastcampus.projectboard.Util.CookieUtil;
import com.fastcampus.projectboard.Util.TokenProvider;
import com.fastcampus.projectboard.domain.UserAccount;
import com.fastcampus.projectboard.domain.forms.ArticleForm;
import com.fastcampus.projectboard.domain.forms.CommentForm;
import com.fastcampus.projectboard.domain.type.SearchType;
import com.fastcampus.projectboard.dto.ArticleDto;
import com.fastcampus.projectboard.dto.ArticleWithCommentDto;
import com.fastcampus.projectboard.dto.HashtagDto;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Arrays;
import java.util.Set;


@RequiredArgsConstructor
@Controller
@RequestMapping("/articles")
public class ArticleController {
    private final ArticleService articleService;
    private final ArticleCommentService articleCommentService;
    private final UserAccountRepository userAccountRepository;
    private final ArticleRepository articleRepository;
    private final UserService userService;
    private final UserSecurityService userSecurityService;
    private final HashtagService hashtagService;
    private final CookieUtil cookieUtil;
    private final TokenProvider tokenProvider;

    
    @GetMapping
    public String articles(
            @RequestParam(required = false) SearchType searchType,
            @RequestParam(required = false) String searchValue,
            @PageableDefault(size=10,sort = "createdAt",direction = Sort.Direction.DESC) Pageable pageable
            , ModelMap map){
        map.addAttribute("articles", articleService.searchArticles(searchType,searchValue,pageable).map(ArticleResponse::from));
        return "articles/index";
    }
    @GetMapping("/{articleId}")
    public String article(@PathVariable Long articleId, ModelMap map , ArticleCommentRequest dto,
                          CommentForm commentForm, HttpServletRequest req, HttpServletResponse res){
        ArticleWithCommentResponse article =  ArticleWithCommentResponse.from(articleService.getArticle(articleId));
        if(cookieUtil.getCookie(req,"refreshToken") != null){
            String refreshToken = cookieUtil.getCookie(req,"refreshToken").getValue();
            map.addAttribute("principal",tokenProvider.getUsername(refreshToken));
        }
        else{
            map.addAttribute("principal",null);
        }
        map.addAttribute("article",article);
        map.addAttribute("articleComments", article.articleCommentsResponse());
        map.addAttribute("dto",dto);
        return "articles/detail";
    }

    @GetMapping("/search-hashtag/{hashtag}")
    public String hashtag(@PathVariable String hashtag, ModelMap map){
        Set<ArticleDto> set=hashtagService.getArticlesByHashtag(hashtag);
        HashtagDto dto = hashtagService.getHashtag(hashtag);
        map.addAttribute("articles",set);
        map.addAttribute("hashtag",dto);
        return "articles/tag/result";
    }

    @GetMapping("/post")
    public String articleCreate(ArticleForm articleForm){
        return "articles/post/article_form";
    }

    @PostMapping("/post")
    public String articleSave(@Valid ArticleForm articleForm,BindingResult bindingResult,
        ArticleRequest dto,
        @AuthenticationPrincipal BoardPrincipal boardPrincipal) {
            if(bindingResult.hasErrors()){
                return "articles/post/article_form";
            }
        BoardPrincipal principal = (BoardPrincipal) userSecurityService.loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        articleService.saveArticle(dto.toDto(principal.toDto()),dto.getHashtags());
        return "redirect:/articles";
    }


    @GetMapping("/put/{articleId}")
    public String articleUpdate(@PathVariable Long articleId, ModelMap map, ArticleForm articleForm,
                                HttpServletResponse res, HttpServletRequest req){
        String refreshToken;
        if(cookieUtil.getCookie(req,"refreshToken") != null){
            refreshToken = cookieUtil.getCookie(req,"refreshToken").getValue();
            ArticleWithCommentDto articleDto = articleService.getArticle(articleId);
            if(!articleDto.getUserAccountDto().userId().equals(tokenProvider.getUsername(refreshToken))){
                return "redirect:/articles";
            }
            Set<HashtagDto> hashtagDto = articleDto.getHashtags();
            StringBuffer sb = new StringBuffer();
            for(int i=0; i<hashtagDto.size(); i++){
                if(i != hashtagDto.size()-1){sb.append("#").append(hashtagDto.stream().toList().get(i).hashtag()).append(" ");}
                else{sb.append("#").append(hashtagDto.stream().toList().get(i).hashtag());}}
            map.addAttribute("hashtag",sb);
            map.addAttribute("dto",articleDto);
        }
        return "articles/put/article_form";
    }


    @PostMapping("/put/{articleId}")
    public String updateArticle(
            @PathVariable Long articleId,ArticleRequest dto,
            @Valid ArticleForm articleForm,
            BindingResult bindingResult,
            @AuthenticationPrincipal BoardPrincipal boardPrincipal){
        if (bindingResult.hasErrors()) {
            return "articles/put/article_form";
        }

        articleService.updateArticle(articleId,dto);
        return "redirect:/articles/"+articleId;
    }





    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{articleId}")
    public String articleDelete(@PathVariable Long articleId ,@AuthenticationPrincipal BoardPrincipal boardPrincipal){
        UserDetails userDetails = userSecurityService.loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        String id = userDetails.getUsername();
        String role = userDetails.getAuthorities().toString();
        if(articleService.getArticle(articleId).getUserAccountDto().userId().equals(id)){
            articleService.deleteArticle(articleId);
        }
        else if(role.equals("[ROLE_ADMIN]")){
            articleService.deleteArticleByAdmin(articleId);
        }
        else{
            return "redirect:/articles";
        }
        return "redirect:/articles";
    }

    private UserAccount createUserAccount() {
        return UserAccount.of(
                "brince",
                "1234",
                "brince@naver.com",
                "brince",
                null
        );

}}

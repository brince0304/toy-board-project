package com.fastcampus.projectboard.Controller;

import com.fastcampus.projectboard.Service.ArticleCommentService;
import com.fastcampus.projectboard.Service.ArticleService;
import com.fastcampus.projectboard.Service.UserService;
import com.fastcampus.projectboard.domain.Article;
import com.fastcampus.projectboard.domain.ArticleComment;
import com.fastcampus.projectboard.domain.UserAccount;
import com.fastcampus.projectboard.domain.forms.ArticleForm;
import com.fastcampus.projectboard.domain.forms.CommentForm;
import com.fastcampus.projectboard.domain.forms.DeleteForm;
import com.fastcampus.projectboard.domain.type.SearchType;
import com.fastcampus.projectboard.dto.ArticleCommentDto;
import com.fastcampus.projectboard.dto.ArticleDto;
import com.fastcampus.projectboard.dto.ArticleWithCommentDto;
import com.fastcampus.projectboard.dto.UserAccountDto;
import com.fastcampus.projectboard.dto.response.ArticleCommentResponse;
import com.fastcampus.projectboard.dto.response.ArticleResponse;
import com.fastcampus.projectboard.dto.response.ArticleWithCommentResponse;
import com.fastcampus.projectboard.repository.ArticleCommentRepository;
import com.fastcampus.projectboard.repository.ArticleRepository;
import com.fastcampus.projectboard.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
@RequiredArgsConstructor
@Controller
@RequestMapping("/articles")
public class ArticleController {
    private final ArticleService articleService;
    private final ArticleCommentService articleCommentService;
    private final UserAccountRepository userAccountRepository;
    private final ArticleRepository articleRepository;
    private final UserService userService;
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
    public String article(@PathVariable Long articleId, ModelMap map , CommentForm commentForm){
        ArticleWithCommentResponse article =  ArticleWithCommentResponse.from(articleService.getArticle(articleId));
        map.addAttribute("article",article);
        map.addAttribute("articleComments", article.articleCommentsResponse());
        commentForm.setArticleId(articleId);
        map.addAttribute("commentForm",commentForm);
        return "articles/detail";
    }

    @GetMapping("/create")
    public String articleCreate(ArticleForm articleForm){
        return "articles/create/article_form";
    }

    @PostMapping("/create")
    public String articleSave(@Valid ArticleForm articleForm , BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "articles/create/article_form";
        }
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String id = userDetails.getUsername();
        UserAccount userAccount = userAccountRepository.findById(id).orElseThrow();
        Article article = Article.of(userAccount,articleForm.getTitle(),articleForm.getContent(),articleForm.getHashtag());
        ArticleDto articleDto = ArticleDto.from(article);
        articleService.saveArticle(articleDto);
        return "redirect:/articles";
    }

    @PostMapping("/comments/{articleId}")
    public String writeArticleComment(@PathVariable Long articleId,
            @Valid CommentForm commentForm, BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            return "redirect:/articles/"+articleId;
        }
        System.out.println("article id"+ articleId);
        System.out.println("comment content"+ commentForm.getContent());
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserAccount account = userService.getUserAccount(userDetails.getUsername()).toEntity();
        System.out.println("account id"+ account.getUserId());

        ArticleComment articleComment = ArticleComment.of(articleRepository.getReferenceById(articleId),account,commentForm.getContent());
        ArticleCommentDto articleCommentDto = ArticleCommentDto.from(articleComment);
        articleCommentService.saveArticleComment(articleCommentDto);
        return "redirect:/articles/"+articleId;
    }



    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{articleId}")
    public String articleDelete(@PathVariable Long articleId){
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String id = userDetails.getUsername();
        if(articleService.getArticle(articleId).userAccountDto().userId().equals(id)){
            articleService.deleteArticle(articleId);
        }
        else{
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
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

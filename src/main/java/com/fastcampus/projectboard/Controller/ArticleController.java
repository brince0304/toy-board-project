package com.fastcampus.projectboard.Controller;

import com.fastcampus.projectboard.Service.ArticleService;
import com.fastcampus.projectboard.domain.Article;
import com.fastcampus.projectboard.domain.UserAccount;
import com.fastcampus.projectboard.domain.forms.ArticleForm;
import com.fastcampus.projectboard.domain.forms.DeleteForm;
import com.fastcampus.projectboard.domain.type.SearchType;
import com.fastcampus.projectboard.dto.ArticleDto;
import com.fastcampus.projectboard.dto.response.ArticleCommentResponse;
import com.fastcampus.projectboard.dto.response.ArticleResponse;
import com.fastcampus.projectboard.dto.response.ArticleWithCommentResponse;
import com.fastcampus.projectboard.repository.ArticleRepository;
import com.fastcampus.projectboard.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
    private final UserAccountRepository userAccountRepository;
    private final ArticleRepository articleRepository;
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
    public String article(@PathVariable Long articleId,ModelMap map){
        ArticleWithCommentResponse article =  ArticleWithCommentResponse.from(articleService.getArticle(articleId));
        map.addAttribute("article",article);
        map.addAttribute("articleComments", article.articleCommentsResponse());

        return "articles/detail";
    }

    @GetMapping("/create")
    public String articleCreate(ArticleForm articleForm){
        return "articles/create/article_form";
    }

    @PostMapping("/create")
    public String articleSave(@Valid ArticleForm articleForm , BindingResult bindingResult, ModelMap map) {
        if (bindingResult.hasErrors()) {
            return "articles/create/article_form";
        }
        UserAccount userAccount = userAccountRepository.findById("brince").orElseThrow();
        Article article = Article.of(createUserAccount(),articleForm.getTitle(),articleForm.getContent(),articleForm.getHashtag());
        ArticleDto articleDto = ArticleDto.from(article);
        articleService.saveArticle(articleDto);
        return "redirect:/articles";
    }
    @DeleteMapping("/{articleId}/delete")
    public String articleDelete(@Valid DeleteForm deleteForm,@PathVariable Long articleId){
        deleteForm.setArticleId(articleId);
        if(articleRepository.getReferenceById(articleId).getUserAccount().getUserPassword()
                        .equals(deleteForm.getPassword())){
        articleService.deleteArticle(deleteForm.getArticleId());}
        else{
            return "articles/delete/delete_form";
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

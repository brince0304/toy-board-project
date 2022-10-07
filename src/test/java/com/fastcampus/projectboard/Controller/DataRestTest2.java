package com.fastcampus.projectboard.Controller;

import com.fastcampus.projectboard.domain.Article;
import com.fastcampus.projectboard.repository.ArticleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
public class DataRestTest2 {


    private final MockMvc mvc;
    private final ArticleRepository articleRepository;
    //autowired 어노테이션 생성자에 꼭 붙여야함
    //Mockmvc 는 AutoConfigureMockMvc 어노테이션도 붙여야한다
    public @Autowired DataRestTest2 ( MockMvc mvc, ArticleRepository articleRepository) {
        this.mvc = mvc;
        this.articleRepository = articleRepository;
    }

    @Test
    void test() throws Exception {
        Article article = Article.of("haha","content","hashtag");
        articleRepository.save(article);
        Long id = article.getId();
        //이렇게 id 로 검색해서 가져오기도 가능함
        //api/articles 를 스테이터스가 ok 일때 가져오고, 데이터 형식은 hal+json (이건 콘텐트 타입에서 확인이 가능함)
        mvc.perform(get("/api/articles/"+id)).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.valueOf("application/hal+json")))
                .andDo(print());
    }
}

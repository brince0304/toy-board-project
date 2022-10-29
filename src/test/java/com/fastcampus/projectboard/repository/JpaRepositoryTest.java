package com.fastcampus.projectboard.repository;

import com.fastcampus.projectboard.config.JpaConfig;
import com.fastcampus.projectboard.domain.Article;
import com.fastcampus.projectboard.domain.UserAccount;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
@DisplayName("JPA 연결 테스트")
@Import(JpaConfig.class)
@DataJpaTest
class JpaRepositoryTest {

    private final ArticleRepository articleRepository;
    private final ArticleCommentRepository articleCommentRepository;

    public JpaRepositoryTest(
            @Autowired ArticleRepository articleRepository,
            @Autowired ArticleCommentRepository articleCommentRepository) {
        this.articleRepository = articleRepository;
        this.articleCommentRepository = articleCommentRepository;
    }
    @DisplayName("delete 테스트")
    @Test
    void givenTestData_whenDeleting_thenWorksFine(){
        //given
        long previousArticleCount = articleRepository.count();
        Article article = articleRepository.findById(1L).orElseThrow();
        long commentSize = article.getArticleComments().size();
        //when
        articleRepository.delete(article);
        long deletecomments = commentSize-articleCommentRepository.count();
        //then
        assertThat(previousArticleCount).isEqualTo(articleRepository.count()+1);
        assertThat(commentSize).isNotEqualTo(articleCommentRepository.count()-deletecomments);


    }
    @DisplayName("select 테스트")
    @Test
    void givenTestData_whenSelecting_thenWorksFine(){
        //given

        //when
        List<Article> articles = articleRepository.findAll();

        //then
        assertThat(articles).isNotNull().hasSize(1);
    }
    @Disabled
    @DisplayName("insert 테스트")
    @Test
    void givenTestData_whenInserting_thenWorksFine(){
        //given
        long previousArticleCount = articleRepository.count();
        UserAccount account = UserAccount.of("brince","1234","brince@naver.com","brince","hhaa");
        Article article = Article.of(account,"this","spring"); //새 게시글 of 로 저장

        //when
        articleRepository.save(article); //새 게시글 저장
        //then
        assertThat(articleRepository.count()).isEqualTo(previousArticleCount+1); //게시글이 추가가 되었는지
    }
    @DisplayName("update 테스트")
    @Test
    void givenTestData_whenUpdating_thenWorksFine(){
        //given
        Article article = articleRepository.findById(1L).orElseThrow(); //아이디로 가져와서 없으면 예외
        String updatedHashtag = "#springboot"; //수정할 부분


        //when
        Article savedArticle = articleRepository.save(article); //수정된 게시글

        //then
        assertThat(savedArticle).hasFieldOrPropertyWithValue("hashtag",updatedHashtag);
        //hashtag 필드가 업데이트 되었는가
    }
    @DisplayName("해시태그 업데이트 직접 작성")
    @Test
    void hashtagUpdatingTest() {
        Article previousArticle = articleRepository.getReferenceById(1L);
        String updatedHashtag = "changed";
        Article newArticle = articleRepository.save(previousArticle);

    }
}
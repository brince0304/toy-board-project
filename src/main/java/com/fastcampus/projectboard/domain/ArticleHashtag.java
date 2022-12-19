package com.fastcampus.projectboard.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Builder
public class ArticleHashtag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    @Setter
    private Article article;

    @ManyToOne(cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @JoinColumn(name = "hashtag_id")
    @Setter
    private Hashtag hashtag;

    protected ArticleHashtag() {
    }

    public ArticleHashtag(Long id, Article article, Hashtag hashtag) {
        this.id = id;
        this.article = article;
        this.hashtag = hashtag;
    }

    public ArticleHashtag(Article article, Hashtag hashtag) {
        this.article = article;
        this.hashtag = hashtag;
    }
    public static ArticleHashtag of(Article article, Hashtag hashtag) {
        return new ArticleHashtag(article, hashtag);
    }
}

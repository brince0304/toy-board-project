package com.fastcampus.projectboard.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
public class ArticleSaveFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Setter
    @OneToOne (fetch = FetchType.LAZY)
    private Article article;

    @Setter
    @ManyToOne (fetch = FetchType.LAZY)
    private SaveFile saveFile;

    private ArticleSaveFile(Long id, Article article, SaveFile saveFile) {
        this.id = id;
        this.article = article;
        this.saveFile = saveFile;
    }

    public static ArticleSaveFile of (Article article, SaveFile saveFile) {
        return new ArticleSaveFile(null, article, saveFile);
    }
}

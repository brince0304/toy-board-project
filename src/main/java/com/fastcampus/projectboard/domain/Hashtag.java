package com.fastcampus.projectboard.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@ToString(callSuper = true)
public class Hashtag{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable = true, unique = true)
    private String hashtag;

    @ToString.Exclude
    @ManyToMany(mappedBy = "hashtag")
    private Set<Article> articles = new LinkedHashSet<>();

    protected Hashtag() {
    }

    public Hashtag(Long id,String hashtag) {
        this.id = id;
        this.hashtag = hashtag;
    }
    public Hashtag(String hashtag) {
        this.hashtag = hashtag;
    }

    public static Hashtag of(String hashtag) {
        return new Hashtag(hashtag);
    }

    public static Hashtag of(Long id, String hashtag) {
        return new Hashtag(id, hashtag);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Hashtag hashtag)) return false;
        return id!=null && id.equals(hashtag.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

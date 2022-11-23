package com.fastcampus.projectboard.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.*;

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

    @OneToMany(mappedBy = "hashtag")
    @ToString.Exclude
    private Set<ArticleHashtag> articles = new HashSet<>();

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

    public record HashtagDto(Long id, String hashtag) {

        public static HashtagDto of(String hashtag) {
            return new HashtagDto(null, hashtag);
        }

        public static HashtagDto of(Long id, String hashtag) {
            return new HashtagDto(id, hashtag);
        }

        public static HashtagDto from(Hashtag entity) {
            return new HashtagDto(
                    entity.getId(),
                    entity.getHashtag()
            );
        }


        public Hashtag toEntity() {
            return Hashtag.of(
                    id,
                    hashtag
            );
        }
    }
}

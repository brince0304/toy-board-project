package Service;

import com.fastcampus.projectboard.repository.ArticleCommentRepository;
import com.fastcampus.projectboard.repository.ArticleRepository;
import dto.ArticleCommentDto;

import dto.ArticleDto;
import dto.ArticleWithCommentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Transactional
@Service
public class ArticleCommentService {

    private final ArticleCommentRepository articleCommentRepository;
    private final ArticleRepository articleRepository;
    @Transactional(readOnly = true)
    public List<ArticleCommentDto> searchArticleComment() {
        return List.of();
    }

    public void saveArticleComment(ArticleWithCommentDto dto) {
    }

    public void updateArticleComment(ArticleDto dto, long articleCommentId,String update) {
    }

    public void deleteArticleComment(long articleCommentId) {
    }
}

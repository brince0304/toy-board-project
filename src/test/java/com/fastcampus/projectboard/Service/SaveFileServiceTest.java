package com.fastcampus.projectboard.Service;

import com.fastcampus.projectboard.domain.Article;
import com.fastcampus.projectboard.domain.ArticleSaveFile;
import com.fastcampus.projectboard.domain.SaveFile;
import com.fastcampus.projectboard.repository.ArticleSaveFileRepository;
import com.fastcampus.projectboard.repository.SaveFileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityNotFoundException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.*;

@DisplayName("비즈니스 로직 - 파")
@ExtendWith(MockitoExtension.class)
class SaveFileServiceTest {
    @InjectMocks
    private SaveFileService sut;

    @Mock
    private ArticleSaveFileRepository articleSaveFileRepository;


    @Mock
    private SaveFileRepository fileRepository;

    @DisplayName("파일 아이디를 입력하면 파일을 조회할 수 있다.")
    @Test
    void givenFileId_whenGettingFile_thenGetsFile() {
        //given
        Long fileId = 1L;
        SaveFile saveFile = SaveFile.builder().
                id(fileId).
                build();
        given(fileRepository.findById(fileId)).willReturn(java.util.Optional.of(saveFile));
        //when
        sut.getFile(fileId);

        //then
        then(fileRepository).should().findById(fileId);
    }
    @DisplayName("없는 파일 아이디를 입력하면 예외를 던진다")
    @Test
    void givenNothing_whenGettingFile_thenThrowsException() {
        //given
        //when
        Throwable throwable = catchThrowable(() -> sut.getFile(1L));
        //then
        assertThat(throwable).isInstanceOf(EntityNotFoundException.class);

    }

    @DisplayName("없는 파일 아이디를 입력하면 예외를 던진다")
    @Test
    void givenNothing_whenDeletesFile_thenThrowsException() {
        //given
        //when
        Throwable throwable = catchThrowable(() -> sut.deleteFile(1L));
        //then
        assertThat(throwable).isInstanceOf(EntityNotFoundException.class);

    }

    @DisplayName("파일 아이디를 입력하면 파일을 삭제할 수 있다.")
    @Test
    void givenFileId_whenDeletingFile_thenDeletesFile() {
        //given
        Long fileId = 1L;
        SaveFile saveFile = SaveFile.builder().
                id(fileId)
                .fileName("test")
                .filePath("test").
                build();
        given(fileRepository.getReferenceById(fileId)).willReturn(saveFile);
        given(fileRepository.existsById(fileId)).willReturn(true);
        //when
        sut.deleteFile(fileId);

        //then
        then(fileRepository).should().deleteById(fileId);
    }

    @Test
    @DisplayName("파일을 저장할 수 있다.")
    void givenFileInfo_whenInsertingFile_thenSavesFile() {
        //given
        Long fileId = 1L;
        SaveFile saveFile = SaveFile.builder().
                id(fileId).
                build();
        given(fileRepository.save(saveFile)).willReturn(saveFile);
        //when
        sut.saveFile(saveFile.toDto());

        //then
        then(fileRepository).should().save(saveFile);
    }

    @Test
    @DisplayName("게시글 내용에 포함되지않은 파일을 삭제한다.")
    void givenArticleContent_whenDeleteUnUploadedSaveFile_thenDeletesSaveFile() {
        //given
        Article article = Article.builder().
                id(1L).
                content("test").
                build();
        Long fileId = 1L;
        SaveFile saveFile = SaveFile.builder().
                id(fileId)
                .fileName("test31")
                .filePath("test").
                build();
        given(fileRepository.getReferenceById(fileId)).willReturn(saveFile);
        given(fileRepository.existsById(fileId)).willReturn(true);
        //when
        sut.deleteUnuploadedFilesFromArticleContent(article.getContent(), "1");

        //then
        then(fileRepository).should().deleteById(fileId);
    }

    @Test
    @DisplayName("게시글 아이디를 입력하면 연관된 파일을 삭제한다.")
    void givenArticleId_whenDeletesSaveFilesByArticleId_thenDeletesSaveFiles() {
        //given
        SaveFile saveFile = SaveFile.builder().
                id(1L)
                .fileName("test")
                .filePath("test").
                build();
        Article article = Article.builder().
                id(1L).
                content("test").
                build();
        ArticleSaveFile articleSaveFile = ArticleSaveFile.of(article, saveFile);
        given(fileRepository.existsById(1L)).willReturn(true);
        given(fileRepository.getReferenceById(any())).willReturn(saveFile);
        given(articleSaveFileRepository.getSaveFileByArticleId(1L)).willReturn(Set.of(articleSaveFile));
        //when
        sut.deleteSaveFilesFromArticleId(1L);

        //then
        then(articleSaveFileRepository).should().deleteBySaveFileId(1L);
        then(fileRepository).should().deleteById(1L);

    }

    @Test
    @DisplayName("게시글 아이디를 입력하면 연관된 파일을 조회한다.")
    void givenArticleRequest_whenGetsFileDtosFromArticleRequest_thenReturnFileDtos() {
        //given
        Article.ArticleRequest articleRequest = Article.ArticleRequest.builder().
                content("test").
                fileIds("1").
                build();
        Long fileId = 1L;
        SaveFile saveFile = SaveFile.builder().
                id(fileId)
                .fileName("test")
                .filePath("test").
                build();
        given(fileRepository.findById(fileId)).willReturn(java.util.Optional.of(saveFile));

        //when
        sut.getFileDtosFromRequestsFileIds(articleRequest);

        //then
        then(fileRepository).should().findById(fileId);
    }


}
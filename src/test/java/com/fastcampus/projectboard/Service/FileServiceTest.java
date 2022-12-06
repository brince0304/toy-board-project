package com.fastcampus.projectboard.Service;

import com.fastcampus.projectboard.domain.File;
import com.fastcampus.projectboard.repository.ArticleRepository;
import com.fastcampus.projectboard.repository.FileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.BDDMockito.*;

import static org.junit.jupiter.api.Assertions.*;
@DisplayName("비즈니스 로직 - 파")
@ExtendWith(MockitoExtension.class)
class FileServiceTest {
    @InjectMocks
    private FileService sut;


    @Mock
    private FileRepository fileRepository;
    @DisplayName("파일 아이디를 입력하면 파일을 조회할 수 있다.")
    @Test
    void givenFileId_whenGettingFile_thenGetsFile() {
        //given
        Long fileId = 1L;
        File file = File.builder().
                id(fileId).
                build();
        given(fileRepository.findById(fileId)).willReturn(java.util.Optional.of(file));
        //when
        sut.getFile(fileId);

        //then
        then(fileRepository).should().findById(fileId);
    }
    @DisplayName("파일 아이디를 입력하면 파일을 삭제할 수 있다.")
    @Test
    void givenFileId_whenDeletingFile_thenDeletesFile() {
        //given
        Long fileId = 1L;
        File file = File.builder().
                id(fileId).
                build();

        //when
        sut.deleteFile(fileId);

        //then
        then(fileRepository).should().deleteById(fileId);
    }

    @Test
    void givenFileInfo_whenInsertingFile_thenSavesFile() {
        //given
        Long fileId = 1L;
        File file = File.builder().
                id(fileId).
                build();
        given(fileRepository.save(file)).willReturn(file);
        //when
        sut.saveFile(file);

        //then
        then(fileRepository).should().save(file);
    }
}
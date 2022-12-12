package com.fastcampus.projectboard.Service;

import com.fastcampus.projectboard.domain.SaveFile;
import com.fastcampus.projectboard.repository.SaveFileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.BDDMockito.*;

@DisplayName("비즈니스 로직 - 파")
@ExtendWith(MockitoExtension.class)
class SaveFileServiceTest {
    @InjectMocks
    private SaveFileService sut;


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
    @DisplayName("파일 아이디를 입력하면 파일을 삭제할 수 있다.")
    @Test
    void givenFileId_whenDeletingFile_thenDeletesFile() {
        //given
        Long fileId = 1L;
        SaveFile saveFile = SaveFile.builder().
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
        SaveFile saveFile = SaveFile.builder().
                id(fileId).
                build();
        given(fileRepository.save(saveFile)).willReturn(saveFile);
        //when
        sut.saveFile(saveFile.toDto());

        //then
        then(fileRepository).should().save(saveFile);
    }
}
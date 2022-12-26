package com.fastcampus.projectboard.Controller;

import com.fastcampus.projectboard.Service.SaveFileService;
import com.fastcampus.projectboard.config.SecurityConfig;
import com.fastcampus.projectboard.domain.SaveFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("view 컨트롤러 - 게시글")
@AutoConfigureMockMvc
@SpringBootTest
@Import(SecurityConfig.class)
public class SaveFileControllerTest {
    private final MockMvc mvc;
    @MockBean
    private final SaveFileService saveFileService;
    private final ObjectMapper objectMapper;

    public SaveFileControllerTest(@Autowired MockMvc mvc, @Autowired SaveFileService saveFileService, @Autowired ObjectMapper objectMapper) {
        this.mvc = mvc;
        this.saveFileService = saveFileService;
        this.objectMapper = objectMapper;
    }

    @Test
    @DisplayName("[view][POST]  파일 업로드")
    void givenMultipartFile_whenUploadingFile_thenSavesFile() throws Exception {
        //given
        SaveFile.SaveFileDto fileDto = SaveFile.SaveFileDto.builder()
                .fileName("test")
                .filePath("test")
                .fileType("jpg")
                .build();
        given(saveFileService.saveFile(any())).willReturn(fileDto);
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpg", "test".getBytes());
        //when&then
        mvc.perform(multipart("/files").file(file))
                .andExpect(status().isOk());
        then(saveFileService).should().saveFile(any());
    }
    @Test
    @DisplayName("[view][GET]  파일 조회")
    void givenSaveFileId_whenGettingFileDto_thenReturnsSaveFileDto() throws Exception {
        //given
        SaveFile.SaveFileDto fileDto = SaveFile.SaveFileDto.builder()
                .fileName("default.jpg")
                .filePath("/Users/brinc/Desktop/brincestudy/fastcampus-project-board/src/main/resources/static/images/default.jpg")
                .fileType("jpg")
                .build();
        given(saveFileService.getFileByFileName(any())).willReturn(fileDto);
        //when&then
        mvc.perform(get("/files/default.jpg"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));
        then(saveFileService).should().getFileByFileName(any());
    }
}

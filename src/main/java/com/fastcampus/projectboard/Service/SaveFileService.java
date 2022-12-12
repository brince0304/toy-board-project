package com.fastcampus.projectboard.Service;

import com.fastcampus.projectboard.domain.Article;
import com.fastcampus.projectboard.domain.SaveFile;
import com.fastcampus.projectboard.repository.ArticleSaveFileRepository;
import com.fastcampus.projectboard.repository.SaveFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.io.File;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class SaveFileService {

    private final SaveFileRepository saveFileRepository;

    private final ArticleSaveFileRepository articleSaveFileRepository;


    @Transactional(readOnly = true)
    public SaveFile.SaveFileDto getFile(Long fileId) {
        log.info("getFile() fileId: {}", fileId);
        return saveFileRepository.findById(fileId).map(SaveFile.SaveFileDto::from).orElseThrow(()-> new EntityNotFoundException("파일이 없습니다 - fileId: " + fileId));
    }

    @Transactional(readOnly = true)
    public SaveFile.SaveFileDto getFileByFileName(String fileName) {
        log.info("getFile() fileId: {}", fileName);
        return saveFileRepository.findByFileName(fileName).toDto();
    }


    public void deleteFile(Long fileId) {
        log.info("deleteFile() fileId: {}", fileId);
        File file = new File(saveFileRepository.getReferenceById(fileId).getFilePath());
        if (file.exists()) {
            if(file.delete()){
                log.info("파일삭제 성공");
            }
        }
        articleSaveFileRepository.deleteBySaveFileId(fileId);
        saveFileRepository.deleteById(fileId);
    }


    public SaveFile.SaveFileDto saveFile(SaveFile.SaveFileDto saveFile) {
        log.info("saveFile() saveFile: {}", saveFile);
        return SaveFile.SaveFileDto.from(saveFileRepository.save(saveFile.toEntity()));
    }


    public void deleteSaveFilesFromDeletedSavedFileIds(Set<Long> deletedFileIds){
        log.info("deleteSaveFilesFromDeletedSavedFileIds() deletedFileIds: {}", deletedFileIds);
        for (Long deletedFileId : deletedFileIds) {
            if(saveFileRepository.existsById(deletedFileId)){
                deleteFile(deletedFileId);
            }
        }
    }

    public void deleteUnuploadedFilesFromArticleContent(String content,String fileIds){
        if(Objects.isNull(content) || Objects.isNull(fileIds) || fileIds.equals("")){
            return;
        }
        for(String fileId : fileIds.split(",")){
            if(!content.contains(saveFileRepository.getReferenceById(Long.parseLong(fileId)).getFileName())){
                deleteFile(Long.parseLong(fileId));
            }
        }
    }

    public Set<SaveFile.SaveFileDto> getFileDtosFromRequestsFileIds(Article.ArticleRequest dto) {
        String[] fileIdArr = Objects.requireNonNull(dto.getFileIds()).split(",");
        Set<SaveFile.SaveFileDto> saveFileDtos = new HashSet<>();
        for (String fileId : fileIdArr) {
            log.info("fileId: {}", fileId);
            if (fileId.equals("")) {
                break;
            }
            saveFileDtos.add(saveFileRepository.findById(Long.parseLong(fileId)).map(SaveFile.SaveFileDto::from).orElseThrow(() -> new EntityNotFoundException("파일이 없습니다 - fileId: " + fileId)));
        }
        return saveFileDtos;
    }


    public void deleteSaveFilesFromArticleId(Long articleId) {
        log.info("deleteSaveFilesFromArticleId() articleId: {}", articleId);
        articleSaveFileRepository.getSaveFileByArticleId(articleId).forEach(t->{
            long fileId = t.getSaveFile().getId();
            deleteFile(fileId);
        });
        }
    }


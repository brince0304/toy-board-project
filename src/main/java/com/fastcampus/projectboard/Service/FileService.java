package com.fastcampus.projectboard.Service;

import com.fastcampus.projectboard.Util.FileUtil;
import com.fastcampus.projectboard.domain.Article;
import com.fastcampus.projectboard.domain.ArticleSaveFile;
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

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class FileService {

    private final SaveFileRepository fileRepository;

    private final ArticleSaveFileRepository articleSaveFileRepository;

    public SaveFile.FileDto getFile(Long fileId) {
        log.info("getFile() fileId: {}", fileId);
        return fileRepository.findById(fileId).map(SaveFile.FileDto::from).orElseThrow(()-> new EntityNotFoundException("파일이 없습니다 - fileId: " + fileId));
    }

    public SaveFile.FileDto getFileByFileName(String fileName) {
        log.info("getFile() fileId: {}", fileName);
        return fileRepository.findByFileName(fileName).toDto();
    }
    public void deleteFile(Long fileId) {
        log.info("deleteFile() fileId: {}", fileId);
        fileRepository.deleteById(fileId);
    }
    public SaveFile.FileDto saveFile(SaveFile.FileDto saveFile) {
        log.info("saveFile() saveFile: {}", saveFile);
        return SaveFile.FileDto.from(fileRepository.save(saveFile.toEntity()));
    }


    public void deleteSaveFilesFromDeletedSavedFileIds(Set<Long> deletedFileIds){
        log.info("deleteSaveFilesFromDeletedSavedFileIds() deletedFileIds: {}", deletedFileIds);
        for (Long deletedFileId : deletedFileIds) {
            articleSaveFileRepository.findBySaveFileId(deletedFileId).ifPresent(t-> {
                File file = new File(t.getSaveFile().getFilePath());
                if (file.exists()) {
                    if(file.delete()){
                        log.info("delete file id:{} success", t.getSaveFile().getId());
                    }
                }
                articleSaveFileRepository.deleteBySaveFileId(deletedFileId);
                    }
                    );
        }
        deletedFileIds.forEach(fileRepository::deleteById);
    }

    public void deleteUnuploadedFilesFromArticleContent(String content,String fileIds){
        if(Objects.isNull(content) || Objects.isNull(fileIds) || fileIds.equals("")){
            return;
        }
        for(String fileId : fileIds.split(",")){
            if(!content.contains(fileRepository.getReferenceById(Long.parseLong(fileId)).getFileName())){
                File file = new File(fileRepository.getReferenceById(Long.parseLong(fileId)).getFilePath());
                deleteFile(Long.parseLong(fileId));
                if (file.exists()) {
                    if(file.delete()){
                        log.info("delete fileid : {}", fileId);
                    }
                }
            }
        }
    }

    public Set<SaveFile.FileDto> getFileDtosFromRequestsFileIds(Article.ArticleRequest dto) {
        String[] fileIdArr = Objects.requireNonNull(dto.getFileIds()).split(",");
        Set<SaveFile.FileDto> fileDtos = new HashSet<>();
        for (String fileId : fileIdArr) {
            log.info("fileId: {}", fileId);
            if (fileId.equals("")) {
                continue;
            }
            fileDtos.add(fileRepository.findById(Long.parseLong(fileId)).map(SaveFile.FileDto::from).orElseThrow(() -> new EntityNotFoundException("파일이 없습니다 - fileId: " + fileId)));
        }
        return fileDtos;
    }


    public void deleteSaveFilesFromArticleId(Long aId) {
        for( ArticleSaveFile articleSaveFile : articleSaveFileRepository.getSaveFileByArticleId(aId)){
            File file = new File(articleSaveFile.getSaveFile().getFilePath());
            long fileId = articleSaveFile.getSaveFile().getId();
            if(file.exists()){
                if(file.delete()){
                    log.info("file deleted : {}",fileId);
                }
            }
            articleSaveFileRepository.deleteByArticleId(aId);
            fileRepository.deleteById(fileId);
        }
    }
}

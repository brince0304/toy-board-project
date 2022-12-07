package com.fastcampus.projectboard.Service;

import com.fastcampus.projectboard.domain.SaveFile;
import com.fastcampus.projectboard.repository.SaveFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class FileService {

    private final SaveFileRepository fileRepository;

    public SaveFile.FileDto getFile(Long fileId) {
        log.info("getFile() fileId: {}", fileId);
        return fileRepository.findById(fileId).map(SaveFile.FileDto::from).orElseThrow(()-> new EntityNotFoundException("파일이 없습니다 - fileId: " + fileId));
    }
    public void deleteFile(Long fileId) {
        log.info("deleteFile() fileId: {}", fileId);
        fileRepository.deleteById(fileId);
    }
    public SaveFile.FileDto saveFile(SaveFile.FileDto saveFile) {
        log.info("saveFile() saveFile: {}", saveFile);
        return SaveFile.FileDto.from(fileRepository.save(saveFile.toEntity()));
    }
}

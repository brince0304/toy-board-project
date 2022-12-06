package com.fastcampus.projectboard.Service;

import com.fastcampus.projectboard.domain.File;
import com.fastcampus.projectboard.repository.FileRepository;
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
    private final FileRepository fileRepository;

    public File.FileDto getFile(Long fileId) {
        return fileRepository.findById(fileId).map(File.FileDto::from).orElseThrow(()-> new EntityNotFoundException("파일이 없습니다 - fileId: " + fileId));
    }

    public void deleteFile(Long fileId) {
        fileRepository.deleteById(fileId);
    }

    public void saveFile(File file) {
        fileRepository.save(file);
    }
}

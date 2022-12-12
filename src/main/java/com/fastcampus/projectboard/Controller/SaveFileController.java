package com.fastcampus.projectboard.Controller;

import com.fastcampus.projectboard.Messages.ErrorMessages;
import com.fastcampus.projectboard.Service.SaveFileService;
import com.fastcampus.projectboard.Util.FileUtil;
import com.fastcampus.projectboard.domain.SaveFile;
import com.fastcampus.projectboard.domain.UserAccount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.*;

@RestController
@Slf4j
@RequiredArgsConstructor
public class SaveFileController {
    private final SaveFileService saveFileService;

    @PostMapping("/files/upload")
    public ResponseEntity<?> uploadFile(@RequestPart("file")MultipartFile file, @AuthenticationPrincipal UserAccount.BoardPrincipal principal) throws IOException {
        if(file.isEmpty()){
            return new ResponseEntity<>(ErrorMessages.NOT_FOUND, HttpStatus.BAD_REQUEST);
        }
        SaveFile.SaveFileDto fileDto = saveFileService.saveFile(FileUtil.getFileDtoFromMultiPartFile(file,principal!=null ? principal.getUsername() : "anonymous"));
        return new ResponseEntity<>(fileDto, HttpStatus.OK);
    }

    @GetMapping("/files/{fileName}")
    public ResponseEntity<?> getFileImg(@PathVariable String fileName) throws IOException {
        try{
        File file = FileUtil.getFileFromSaveFile(saveFileService.getFileByFileName(fileName));
        byte[] imgArray = IOUtils.toByteArray(new FileInputStream(file));
        return new ResponseEntity<>(imgArray,HttpStatus.OK);
    }
        catch (EntityNotFoundException e){
            return new ResponseEntity<>(ErrorMessages.NOT_FOUND,HttpStatus.NOT_FOUND);
        }
    }
}

package com.fastcampus.projectboard.Controller;

import com.fastcampus.projectboard.Service.FileService;
import com.fastcampus.projectboard.Util.FileUtil;
import com.fastcampus.projectboard.domain.SaveFile;
import com.fastcampus.projectboard.domain.UserAccount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

@RestController
@Slf4j
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @PostMapping("/files/upload")
    public ResponseEntity<?> uploadFile(@RequestPart("file")MultipartFile file, @AuthenticationPrincipal UserAccount.BoardPrincipal principal) throws IOException {
        if(file!=null){
        SaveFile.FileDto fileDto = FileUtil.getFileDtoFromMultiPartFile(file,principal.getUsername());
        SaveFile.FileDto savedFile = fileService.saveFile(fileDto);
        return new ResponseEntity<>(savedFile, HttpStatus.OK);
        }
        if(principal==null){
            return new ResponseEntity<>("로그인이 필요합니다.",HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/files/{fileName}")
    public ResponseEntity<?> getFileImg(@PathVariable String fileName) throws IOException {
        SaveFile.FileDto fileDto = fileService.getFileByFileName(fileName);
        File file = FileUtil.getFileFromFileDomain(fileDto);
        byte[] imgArray = IOUtils.toByteArray(new FileInputStream(file));
        return new ResponseEntity<>(imgArray,HttpStatus.OK);
    }
}

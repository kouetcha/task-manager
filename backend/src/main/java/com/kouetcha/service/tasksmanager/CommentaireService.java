package com.kouetcha.service.tasksmanager;

import com.kouetcha.dto.tasksmanager.CommentaireDto;
import com.kouetcha.dto.tasksmanager.CommentaireUpdateDto;
import com.kouetcha.model.base.BaseCommentaire;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CommentaireService<C extends BaseCommentaire<T>, T> {

    C create(CommentaireDto dto);

    C update(CommentaireDto dto);
    C changeContenu(CommentaireUpdateDto dto);

    void delete(Long commentaireId);

    List<C> findByParent(Long parentId);

    public String saveFile(MultipartFile file);

    ResponseEntity<Resource> downloadFile(String fileCode);
}
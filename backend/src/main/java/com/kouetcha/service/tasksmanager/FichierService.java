package com.kouetcha.service.tasksmanager;

import com.kouetcha.model.base.BaseVS;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface FichierService<F extends BaseVS, T> {

    F upload(Long parentId, MultipartFile file, String fichierNom);

    void delete(Long fichierId);

    List<F> findByParent(Long parentId);
    ResponseEntity<Resource> downloadFile(String fileCode);

    void deleteAllByParentId(Long id);

    void deleteById(Long fichierId);

    Map<String, Integer> saveDocumentOnLyOffice(Long id, Map<String, Object> payload);
}

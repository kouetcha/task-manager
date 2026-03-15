package com.kouetcha.service.tasksmanager;

import com.kouetcha.dto.tasksmanager.CountMenuDto;
import com.kouetcha.repository.tasksmanager.ActiviteRepository;
import com.kouetcha.repository.tasksmanager.ProjetRepository;
import com.kouetcha.repository.tasksmanager.TacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatistiqueServiceImpl implements StatistiqueService{
    private final ActiviteRepository activiteRepository;
    private final ProjetRepository projetRepository;
    private final TacheRepository tacheRepository;

    @Override
    public CountMenuDto recupereMenuCount(String email){
        return new CountMenuDto(
                projetRepository.countActiveByEmail(email),
                activiteRepository.countActiveByEmail(email),
                tacheRepository.countActiveByEmail(email)
        );
    }
}

package com.kouetcha.dto.tasksmanager;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CountMenuDto {
    Long nbreProjets;
    Long nbrActivites;
    Long nbrTaches;

    public CountMenuDto( Long nbreProjets,
                         Long nbrActivites,
                         Long nbrTaches
                        )
    {
        this.nbreProjets=nbreProjets;
        this.nbrActivites=nbrActivites;
        this.nbrTaches=nbrTaches;
    }
}

package com.kingjakeu.lolesport.api.crawl.dto.game;

import com.kingjakeu.lolesport.api.tournament.domain.Tournament;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GameTournamentDto {
    private String id;

    public Tournament toTournamentEntity(){
        return Tournament.builder().id(this.id).build();
    }
}
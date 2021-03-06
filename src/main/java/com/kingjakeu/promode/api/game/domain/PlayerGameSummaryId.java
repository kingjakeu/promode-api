package com.kingjakeu.promode.api.game.domain;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Getter
@NoArgsConstructor
@Embeddable
public class PlayerGameSummaryId implements Serializable {
    @Setter
    @Column(name = "GAME_ID")
    private String gameId;

    @Setter
    @Column(name = "PLAYER_ID")
    private String playerId;

    @Builder
    public PlayerGameSummaryId (String gameId, String playerId) {
        this.gameId = gameId;
        this.playerId = playerId;
    }
}

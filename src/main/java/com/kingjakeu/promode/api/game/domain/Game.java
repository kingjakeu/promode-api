package com.kingjakeu.promode.api.game.domain;

import com.kingjakeu.promode.api.match.domain.Match;
import com.kingjakeu.promode.api.tournament.domain.Tournament;
import com.kingjakeu.promode.api.league.domain.League;
import com.kingjakeu.promode.api.team.domain.Team;
import com.kingjakeu.promode.common.constant.CommonCode;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "GAME_INFO")
public class Game {
    @Id
    @Column(name = "ID", length = 20)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MATCH_ID")
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TOURNAMENT_ID")
    private Tournament tournament;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LEAGUE_ID")
    private League league;

    @Column(name = "GAME_SEQ", length = 2)
    private Integer sequence;

    @Column(name = "STATE", length = 20)
    private String state;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BLUE_TEAM_ID")
    private Team blueTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RED_TEAM_ID")
    private Team redTeam;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WIN_TEAM_ID")
    private Team winTeam;

    @Setter
    @Column(name = "PATCH_VER", length = 20)
    private String patchVersion;

    @Column(name = "START_DATETIME", columnDefinition = "datetime")
    private LocalDateTime startTime;

    @Column(name = "START_MILLIS", length = 10)
    private Long startMillis;

    @Column(name = "END_MILLIS", length = 10)
    private Long endMillis;

    @Setter
    @Lob
    @Column(name = "MATCH_HISTORY_URL")
    private String matchHistoryUrl;

    @CreationTimestamp
    @Column(name = "CREATE_DTM", nullable = false, updatable = false, columnDefinition = "timestamp")
    private LocalDateTime createDateTime;

    @UpdateTimestamp
    @Column(name = "UPDATE_DTM", nullable = false, columnDefinition = "timestamp")
    private LocalDateTime updateDateTime;

    public boolean isMatchHistoryLinkEmpty(){
        return this.matchHistoryUrl == null;
    }

    public Team getTeamBySide(String side){
        if (CommonCode.BLUE_SIDE.codeEqualsTo(side)){
            return this.blueTeam;
        }else if(CommonCode.RED_SIDE.codeEqualsTo(side)){
            return this.redTeam;
        }else{
            return null;
        }
    }
}

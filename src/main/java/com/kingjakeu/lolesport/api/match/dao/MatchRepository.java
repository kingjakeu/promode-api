package com.kingjakeu.lolesport.api.match.dao;

import com.kingjakeu.lolesport.api.match.domain.Match;
import com.kingjakeu.lolesport.api.tournament.domain.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MatchRepository extends JpaRepository<Match, String> {
    List<Match> findAllByStartTimeBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<Match> findAllByBlockName(String blockName);
    List<Match> findAllByTournament(Tournament tournament);
    List<Match> findAllByTournamentIdAndState(String tournamentId, String state);
}

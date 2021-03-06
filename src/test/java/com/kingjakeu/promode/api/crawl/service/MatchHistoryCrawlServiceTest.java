package com.kingjakeu.promode.api.crawl.service;

import com.kingjakeu.promode.api.crawl.dto.request.MatchHistoryRequestDto;
import com.kingjakeu.promode.api.game.dao.GameRepository;
import com.kingjakeu.promode.api.game.domain.Game;
import com.kingjakeu.promode.api.match.dao.MatchRepository;
import com.kingjakeu.promode.api.match.domain.Match;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class   MatchHistoryCrawlServiceTest {

    @Autowired
    private MatchHistoryCrawlService matchHistoryCrawlService;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private GameRepository gameRepository;

    @Test
    void crawlGameTimeLine() {
        matchHistoryCrawlService.crawlGameTimeLine("https://acs.leagueoflegends.com/v1/stats/game/ESPORTSTMNT01/1896701/timeline?gameHash=b5f757080fbc9aed");
    }

    @Test
    void crawlGameResultDetail() {
        List<Match> matchList = this.matchRepository.findAllByBlockName("WEEK9");

        for(Match match : matchList){
            MatchHistoryRequestDto matchHistoryRequestDto = new MatchHistoryRequestDto();
            matchHistoryRequestDto.setMatchId(match.getId());
            this.matchHistoryCrawlService.crawlGameResultDetail(matchHistoryRequestDto);
        }
    }
}
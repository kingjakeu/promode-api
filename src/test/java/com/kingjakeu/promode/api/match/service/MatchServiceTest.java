package com.kingjakeu.promode.api.match.service;

import com.kingjakeu.promode.api.match.dto.response.MatchGameResultResDto;
import com.kingjakeu.promode.api.match.dto.response.MatchResultResDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class MatchServiceTest {

    @Autowired
    private MatchService matchService;

    @Test
    void getMatch() {
        LocalDate localDate = LocalDate.parse("2021-03-18");
        List<MatchResultResDto> matchResultResDtoList = this.matchService.getMatch(localDate);
        for(MatchResultResDto resultResDto : matchResultResDtoList){
            System.out.println(resultResDto.toString());
        }
    }

    @Test
    void getMatchGames() {
        List<MatchGameResultResDto> gameResultResDtos = this.matchService.getMatchGames("105522984812850465");
        System.out.println(gameResultResDtos.toString());
    }
}
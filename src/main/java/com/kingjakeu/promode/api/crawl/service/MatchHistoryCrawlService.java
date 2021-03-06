package com.kingjakeu.promode.api.crawl.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.kingjakeu.promode.api.ban.domain.BanHistory;
import com.kingjakeu.promode.api.ban.domain.BanHistoryId;
import com.kingjakeu.promode.api.champion.domain.Champion;
import com.kingjakeu.promode.api.champion.service.ChampionCommonService;
import com.kingjakeu.promode.api.ban.dao.BanHistoryRepository;
import com.kingjakeu.promode.api.config.service.ConfigService;
import com.kingjakeu.promode.api.crawl.dto.matchhistory.ParticipantDto;
import com.kingjakeu.promode.api.crawl.dto.request.MatchHistoryRequestDto;
import com.kingjakeu.promode.api.game.dao.GameRepository;
import com.kingjakeu.promode.api.crawl.dto.matchhistory.MatchHistoryDto;
import com.kingjakeu.promode.api.crawl.dto.matchhistory.TeamDto;
import com.kingjakeu.promode.api.game.dao.PlayerGameSummaryRepository;
import com.kingjakeu.promode.api.game.dao.TeamGameSummaryRepository;
import com.kingjakeu.promode.api.game.domain.*;
import com.kingjakeu.promode.api.game.service.GameCommonService;
import com.kingjakeu.promode.api.match.dao.MatchRepository;
import com.kingjakeu.promode.api.match.domain.Match;
import com.kingjakeu.promode.api.match.service.MatchCommonService;
import com.kingjakeu.promode.api.pick.dao.PickHistoryRepository;
import com.kingjakeu.promode.api.pick.domain.PickHistory;
import com.kingjakeu.promode.api.pick.domain.PickHistoryId;
import com.kingjakeu.promode.api.player.domain.Player;
import com.kingjakeu.promode.api.player.service.PlayerCommonService;
import com.kingjakeu.promode.api.team.domain.Team;
import com.kingjakeu.promode.common.constant.CommonCode;
import com.kingjakeu.promode.common.constant.CommonError;
import com.kingjakeu.promode.common.constant.LolRole;
import com.kingjakeu.promode.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class MatchHistoryCrawlService {

    private final CrawlCommonService crawlCommonService;

    private final BanHistoryRepository banHistoryRepository;
    private final PickHistoryRepository pickHistoryRepository;
    private final GameRepository gameRepository;
    private final MatchRepository matchRepository;

    private final ConfigService configService;
    private final GameCommonService gameCommonService;
    private final MatchCommonService matchCommonService;
    private final ChampionCommonService championCommonService;
    private final PlayerCommonService playerCommonService;
    private final PlayerGameSummaryRepository playerGameSummaryRepository;
    private final TeamGameSummaryRepository teamGameSummaryRepository;


    public void crawlGameTimeLine(String url) {
//        TimeLineDto timeLineDto = Crawler.doGetObject(url, CrawlUrl.acsMatchHistoryHeader(), Collections.emptyMap(), new TypeReference<TimeLineDto>() {});
//        System.out.println("DONE");
    }

    /**
     * ?????? ?????? ?????????
     * @param requestDto ????????? ??????
     */
    public void crawlGameResultDetail(MatchHistoryRequestDto requestDto) {
        if (requestDto.getGameId() != null){
            final Game game = this.gameCommonService.findGameById(requestDto.getGameId());
            this.crawlGameResultDetail(game);
        }else if(requestDto.getMatchId() != null){
            final Match match = this.matchCommonService.findByMatchId(requestDto.getMatchId());
            this.crawlGameResultDetail(match);
        }else{
            throw new ResourceNotFoundException(CommonError.GAME_INFO_NOT_FOUND);
        }
    }


    /**
     * ?????? ?????? ?????? ?????????
     * @param match ?????? ??????
     */
    private void crawlGameResultDetail(Match match) {
        List<Game> gameList = this.gameCommonService.findGameByMatchId(match.getId());

        Map<String, Integer> gameWinnerMap = new HashMap<>();
        gameWinnerMap.put(match.getTeam1().getId(), 0);
        gameWinnerMap.put(match.getTeam2().getId(), 0);

        for(Game game : gameList){
            this.crawlGameResultDetail(game);
            String winnerId = game.getWinTeam().getId();
            gameWinnerMap.put(winnerId, gameWinnerMap.get(winnerId) + 1);
        }

        // ?????? ?????? ?????? ??????
        if(gameWinnerMap.get(match.getTeam1().getId()) > gameWinnerMap.get(match.getTeam2().getId())){
            match.setWinTeam(match.getTeam1());
        }else{
            match.setWinTeam(match.getTeam2());
        }
        this.matchRepository.save(match);
    }

    /**
     * ?????? ?????? ????????? ?????????
     * @param game ?????? ??????
     */
    private void crawlGameResultDetail(Game game){
        // ?????? ???????????? ????????? ?????? ????????? ?????? ????????????, ??????
        if(game.isMatchHistoryLinkEmpty()) throw new ResourceNotFoundException(CommonError.GAME_MATCH_INFO_NOT_FOUND);

        // ?????? ??????
        MatchHistoryDto matchHistoryDto = this.crawlMatchHistoryByApi(game.getMatchHistoryUrl());

        // ?????? ?????? ????????????
        game.setPatchVersion(matchHistoryDto.getGameVersion());
        game.setWinTeam(game.getTeamBySide(matchHistoryDto.getWinTeamSide()));
        this.gameRepository.save(game);

        // ????????? ??????
        this.saveTeamGameSummary(game, matchHistoryDto);
        // ??? ??????
        this.saveBanHistory(game, matchHistoryDto);
        // ???????????? ?????? ??????
        this.savePlayerGameSummaryAndPickHistory(game, matchHistoryDto);
    }

    /**
     * acs riot match history ?????? ?????????
     * @param matchHistoryUrl ?????? ??????
     * @return ?????? ?????? ??????
     */
    private MatchHistoryDto crawlMatchHistoryByApi(String matchHistoryUrl){
        // refine url
        String matchHistoryPageBaseUrl = this.configService.findConfigValue("MATCH_HISTORY_PAGE_BASE");
        String matchHistoryApiBaseUrl = this.configService.findConfigValue("MATCH_HISTORY_API_BASE");
        String url = matchHistoryUrl.replace(matchHistoryPageBaseUrl, matchHistoryApiBaseUrl);

        //do crawl
        return this.crawlCommonService.crawlAcsMatchHistory(url, new TypeReference<>() {});
    }

    /**
     * ??? ?????? ?????? ?????? ??????
     * @param game ??????
     * @param matchHistoryDto ?????? ??????
     */
    public void saveTeamGameSummary(Game game, MatchHistoryDto matchHistoryDto){
        // ?????? ???
        this.saveTeamGameSummary(game, matchHistoryDto, CommonCode.BLUE_SIDE.getCode());
        // ?????? ???
        this.saveTeamGameSummary(game, matchHistoryDto, CommonCode.RED_SIDE.getCode());
    }

    /**
     * ??? ?????? ?????? ?????? ?????? by side
     * @param game ??????
     * @param matchHistoryDto ?????? ??????
     * @param side side
     */
    private void saveTeamGameSummary(Game game, MatchHistoryDto matchHistoryDto, String side){
        // ?????? or ?????? ???
        Team team = CommonCode.BLUE_SIDE.codeEqualsTo(side) ? game.getBlueTeam() : game.getRedTeam();

        // ??? ?????? ?????? ?????????
        TeamGameSummaryId teamGameSummaryId = TeamGameSummaryId.builder()
                .gameId(game.getId())
                .teamId(team.getId())
                .build();

        // ?????? or ?????? ??????
        TeamGameSummary teamGameSummary =  CommonCode.BLUE_SIDE.codeEqualsTo(side) ?
                matchHistoryDto.getBlueTeamDto().toTeamGameSummaryEntity()
                : matchHistoryDto.getRedTeamDto().toTeamGameSummaryEntity();

        // ??? ?????? ?????? ??????
        teamGameSummary.setTeamGameSummaryId(teamGameSummaryId);
        teamGameSummary.setGame(game);
        teamGameSummary.setTeam(team);
        teamGameSummary.setSide(side);
        this.teamGameSummaryRepository.save(teamGameSummary);
    }

    /**
     * ?????? ??? ?????? ??????
     * @param game ??????
     * @param matchHistoryDto ?????? ??????
     */
    public void saveBanHistory(Game game, MatchHistoryDto matchHistoryDto) {
        // ?????? ???
        this.saveBanHistory(game, matchHistoryDto, CommonCode.BLUE_SIDE.getCode());
        // ?????? ???
        this.saveBanHistory(game, matchHistoryDto, CommonCode.RED_SIDE.getCode());
    }

    /**
     * ?????? ??? ?????? ?????? by side
     * @param game ??????
     * @param matchHistoryDto ????????????
     * @param side side
     */
    private void saveBanHistory(Game game, MatchHistoryDto matchHistoryDto, String side) {
        //Side ??? ????????? ????????????
        TeamDto teamDto = CommonCode.BLUE_SIDE.codeEqualsTo(side) ? matchHistoryDto.getBlueTeamDto() : matchHistoryDto.getRedTeamDto();
        List<String> banChampionKeyList = teamDto.getBanChampionKeyList();

        int banTurn = 1;
        for(String champKey : banChampionKeyList){
            // ??? ?????? ????????? ??????
            BanHistoryId banHistoryId = BanHistoryId.builder()
                    .gameId(game.getId())
                    .side(side)
                    .banTurn(banTurn++) // ??? ?????? ????????? ??????
                    .build();

            // ??? ?????? ??????
            this.banHistoryRepository.save(
                    BanHistory.builder()
                    .banHistoryId(banHistoryId)
                    .game(game)
                    .bannedChampion(this.championCommonService.findById(champKey))
                    .patchVersion(matchHistoryDto.getGameVersion())
                    .build()
            );
        }
    }

    /**
     * ???????????? ????????? ?????? + ??? ???????????? ??????
     * @param game ?????? ??????
     * @param matchHistoryDto ?????? ??????
     */
    private void savePlayerGameSummaryAndPickHistory(Game game, MatchHistoryDto matchHistoryDto){
        String gameVersion = matchHistoryDto.getGameVersion();

        int i = 0;
        for(ParticipantDto participantDto : matchHistoryDto.getParticipants()){
            // ???????????? ??????
            Player player = this.playerCommonService.findPlayerBySummonerName(
                    matchHistoryDto.findSummonerNameById(participantDto.getParticipantId()));
            // ????????? ??????
            PlayerGameSummary playerGameSummary = this.savePlayerGameSummary(game, player, i, participantDto);
            this.savePickHistory(playerGameSummary, gameVersion);
            i += 1;
        }
    }

    /**
     * ???????????? ?????? ??????
     * @param game ??????
     * @param player ????????????
     * @param seqNo ?????? ??????
     * @param participantDto ???????????? ?????? ??????
     * @return ???????????? ?????? ??????
     */
    private PlayerGameSummary savePlayerGameSummary(Game game, Player player, int seqNo, ParticipantDto participantDto){
        // ?????? ????????? ??????
        Champion champion = this.championCommonService.findById(participantDto.getChampionId().toString());
        // ????????? ??????
        String side = seqNo < 5 ? CommonCode.BLUE_SIDE.getCode() : CommonCode.RED_SIDE.getCode();
        // ????????? ??????
        LolRole lolRole = LolRole.findBySequence(seqNo);

        PlayerGameSummaryId playerGameSummaryId = PlayerGameSummaryId.builder()
                .gameId(game.getId())
                .playerId(player.getId())
                .build();

        PlayerGameSummary playerGameSummary = participantDto.getStats().toPlayerGameSummaryEntity();
        playerGameSummary.setPlayerGameSummaryId(playerGameSummaryId);
        playerGameSummary.setGame(game);
        playerGameSummary.setPlayer(player);
        playerGameSummary.setChampion(champion);
        playerGameSummary.setSide(side);
        playerGameSummary.setRole(lolRole);
        return this.playerGameSummaryRepository.save(playerGameSummary);
    }

    /**
     * ??? ?????? ??????
     * @param playerGameSummary ???????????? ?????? ??????
     */
    private void savePickHistory(PlayerGameSummary playerGameSummary, String gameVersion){
        PickHistoryId pickHistoryId = PickHistoryId.builder()
                .gameId(playerGameSummary.getGame().getId())
                .role(playerGameSummary.getRole())
                .side(playerGameSummary.getSide())
                .build();

        PickHistory pickHistory = PickHistory.builder()
                .pickHistoryId(pickHistoryId)
                .game(playerGameSummary.getGame())
                .player(playerGameSummary.getPlayer())
                .champion(playerGameSummary.getChampion())
                .patchVersion(gameVersion)
                .build();
        this.pickHistoryRepository.save(pickHistory);
    }
}

package com.kingjakeu.promode.api.crawl.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.kingjakeu.promode.api.game.dao.GameRepository;
import com.kingjakeu.promode.api.league.dao.LeagueRepository;
import com.kingjakeu.promode.api.game.domain.Game;
import com.kingjakeu.promode.api.league.domain.League;
import com.kingjakeu.promode.api.match.dao.MatchRepository;
import com.kingjakeu.promode.api.match.domain.Match;
import com.kingjakeu.promode.api.team.dao.TeamRepository;
import com.kingjakeu.promode.api.tournament.dao.TournamentRepository;
import com.kingjakeu.promode.api.tournament.domain.Tournament;
import com.kingjakeu.promode.api.crawl.dto.LolEsportDataDto;
import com.kingjakeu.promode.api.crawl.dto.game.GameDataDto;
import com.kingjakeu.promode.api.crawl.dto.game.GameEventDto;
import com.kingjakeu.promode.api.crawl.dto.league.LeagueDataDto;
import com.kingjakeu.promode.api.crawl.dto.schedule.ScheduleDataDto;
import com.kingjakeu.promode.api.crawl.dto.schedule.ScheduleDto;
import com.kingjakeu.promode.api.crawl.dto.schedule.ScheduleEventDto;
import com.kingjakeu.promode.api.crawl.dto.tournament.TournamentDataDto;
import com.kingjakeu.promode.api.crawl.dto.tournament.TournamentLeagueDto;
import com.kingjakeu.promode.api.league.service.LeagueCommonService;
import com.kingjakeu.promode.api.tournament.service.TournamentCommonService;
import com.kingjakeu.promode.common.constant.CommonCode;
import com.kingjakeu.promode.common.constant.CommonError;
import com.kingjakeu.promode.common.constant.CrawlUrlConfig;
import com.kingjakeu.promode.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchInfoCrawlService {

    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final GameRepository gameRepository;

    private final LeagueCommonService leagueCommonService;
    private final LeagueRepository leagueRepository;

    private final TournamentCommonService tournamentCommonService;
    private final TournamentRepository tournamentRepository;

    private final CrawlCommonService crawlCommonService;

    /**
     * ?????? ?????? ?????? ?????????
     * ex. lck, lpl
     */
    public void crawlLeagueInfos() {
        Map<String, String> parameters = this.crawlCommonService.createCommonLolEsportParameters();

        LolEsportDataDto<LeagueDataDto> resultDto = this.crawlCommonService.crawlLolEsportApi(
                CrawlUrlConfig.LEAGUE_LIST, parameters, new TypeReference<>() {});

        List<League> leagueList = resultDto.getData()
                .toLeagueEntities();

        this.leagueRepository.saveAll(leagueList);
    }

    /**
     * ?????? ?????? ?????? ?????????
     * aka ?????? ??????
     * @param leagueId ?????? ?????????
     */
    public void crawlLeagueTournamentInfos(String leagueId) {
        final League league = this.leagueCommonService.findLeagueById(leagueId);

        Map<String, String> parameters = this.crawlCommonService.createCommonLolEsportParameters();
        parameters.put("leagueId", league.getId());

        LolEsportDataDto<TournamentDataDto> resultDto = this.crawlCommonService.crawlLolEsportApi(
                CrawlUrlConfig.TOURNAMENT_LIST, parameters, new TypeReference<>() {});

        TournamentLeagueDto tournamentLeagueDto = resultDto.getData().getLeagues().get(0);

        this.tournamentRepository.saveAll(tournamentLeagueDto.toTournamentEntities(league));
    }


    /**
     * ?????? ?????? ?????? ?????? ?????????
     * @param tournamentId ???????????? ?????????
     */
    public void crawlLeagueMatchSchedules(String tournamentId)  {
        final Tournament tournament = this.tournamentCommonService.findTournamentById(tournamentId);
        final League league = tournament.getLeague();
        
        Map<String, String> parameters = this.crawlCommonService.createCommonLolEsportParameters();
        parameters.put("leagueId", league.getId());
        ScheduleDto scheduleDto = this.crawlLeagueSchedule(parameters);

        for(ScheduleEventDto eventDto : scheduleDto.getEvents()){
            this.saveMatch(eventDto, tournament);
        }

        while (scheduleDto.getPages().getOlder() != null) {
            parameters.put("pageToken", scheduleDto.getPages().getOlder());
            scheduleDto = this.crawlLeagueSchedule(parameters);

            for(ScheduleEventDto eventDto : scheduleDto.getEvents()){
                this.saveMatch(eventDto, tournament);
            }
        }
    }

    /**
     * ?????? ?????? ?????????
     * @param parameters leagueId, pageToken(optional)
     * @return ?????? ??????
     */
    private ScheduleDto crawlLeagueSchedule(Map<String, String> parameters) {
        LolEsportDataDto<ScheduleDataDto> resultDto = this.crawlCommonService.crawlLolEsportApi(
                CrawlUrlConfig.LEAGUE_SCHEDULE_LIST, parameters, new TypeReference<>() {});
        return resultDto.getData()
                .getSchedule();
    }
    /**
     * ?????? ?????? ??????
     * @param eventDto ?????? ????????? DTO
     * @param tournament ???????????? ??????
     */
    private void saveMatch(ScheduleEventDto eventDto, Tournament tournament){
        if(eventDto.isNotInProgress()){
            Match match = eventDto.toMatchEntity();
            if(match.isStartDateBetween(tournament.getStartDate(), tournament.getEndDate())){
                match.setTournament(tournament);
                match.setTeam1(this.teamRepository.findByCode(match.getTeam1().getCode()));
                match.setTeam2(this.teamRepository.findByCode(match.getTeam2().getCode()));
                this.matchRepository.save(match);
            }
        }
    }

    /**
     * ?????? ?????? ?????? ??????
     */
    public void crawlMatchGameEvents() {
        List<Match> matchList = this.matchRepository.findAll();
        for(Match match : matchList){
            this.crawlMatchGameEvents(match.getId());
        }
    }

    /**
     * ?????? ?????? ?????? ?????? ??????
     * @param matchId ?????? ID
     */
    public void crawlMatchGameEvents(String matchId) {
        Map<String, String> parameters = this.crawlCommonService.createCommonLolEsportParameters();
        parameters.put("id", matchId);

        LolEsportDataDto<GameDataDto> resultDto = this.crawlCommonService.crawlLolEsportApi(
                CrawlUrlConfig.MATCH_EVENT_DETAIL, parameters, new TypeReference<>() {});

        GameEventDto eventDto = resultDto.getData().getEvent();

        this.gameRepository.saveAll(eventDto.toGameEntities());
    }

    /**
     * ?????? ?????? Match History Link ?????????
     */
    public void crawlAllLckMatchHistoryLink() {
        List<Game> gameList = this.gameRepository.findAllByState(CommonCode.STATE_COMPLETED.getCode());
        Document document = this.crawlCommonService.crawlDocument(CrawlUrlConfig.LCK_MATCH_HISTORY_LIST);

        for(Game game : gameList){
            this.crawlMatchHistoryLink(game, document);
        }
    }

    /**
     * ?????? ?????? match history link ?????????
     * @param gameId ?????? ?????????
     */
    public void crawlLckMatchHistoryLink(String gameId) {
        Optional<Game> optionalGame = this.gameRepository.findByIdAndState(gameId, CommonCode.STATE_COMPLETED.getCode());
        if(optionalGame.isEmpty()) throw new ResourceNotFoundException(CommonError.GAME_INFO_NOT_FOUND);

        final Game game = optionalGame.get();

        Document document = this.crawlCommonService.crawlDocument(CrawlUrlConfig.LCK_MATCH_HISTORY_LIST);
        this.crawlMatchHistoryLink(game, document);
    }

    /**
     *  match history link ?????????
     * @param game ?????? ??????
     * @param document ????????? ?????????
     */
    public void crawlMatchHistoryLink(Game game, Document document){
        Elements rowElements = document.getElementsByClass("mhgame-red multirow-highlighter");
        rowElements.addAll(document.getElementsByClass("mhgame-blue multirow-highlighter"));

        LinkedList<String> linkList = new LinkedList<>();
        for(Element row : rowElements){
            String gameDate = row.getElementsByClass("mhgame-result").get(0).text();

            Elements cells = row.getElementsByTag("a");
            String blueTeamLink = cells.get(1).attr("href");
            String redTeamLink = cells.get(2).attr("href");

            String matchHistoryLink = cells.get(15).attr("href");
            if(game.getMatch().startDateEqualsTo(LocalDate.parse(gameDate))){
                if((game.getBlueTeam().crawlKeyEqualsTo(blueTeamLink) && game.getRedTeam().crawlKeyEqualsTo(redTeamLink))
                        || (game.getBlueTeam().crawlKeyEqualsTo(redTeamLink) && game.getRedTeam().crawlKeyEqualsTo(blueTeamLink))) {
                    linkList.addFirst(matchHistoryLink);
                }
            }
        }
        if(!linkList.isEmpty()){
            game.setMatchHistoryUrl(linkList.get(game.getSequence()-1));
            this.gameRepository.save(game);
        }
    }
}

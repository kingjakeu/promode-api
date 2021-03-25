package com.kingjakeu.lolesport.api.champion.service;

import com.kingjakeu.lolesport.api.champion.dao.ChampionRepository;
import com.kingjakeu.lolesport.api.champion.domain.Champion;
import com.kingjakeu.lolesport.common.constant.CommonError;
import com.kingjakeu.lolesport.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChampionCommonService {

    private final ChampionRepository championRepository;

    public Champion findById(String id){
        Optional<Champion> optionalChampion = this.championRepository.findById(id);
        if(optionalChampion.isEmpty()) throw new ResourceNotFoundException(CommonError.CHAMP_NOT_FOUND);
        return optionalChampion.get();
    }

    public Champion findByChampKey(String crawlKey) {
        Optional<Champion> optionalChampion = this.championRepository.findByChampKey(crawlKey);
        if(optionalChampion.isEmpty()) throw new ResourceNotFoundException(CommonError.CHAMP_NOT_FOUND);
        return optionalChampion.get();
    }
}

package com.kingjakeu.lolesport.api.crawl.dto.standing;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class StandSectionDto {
    private String name;
    private LinkedHashMap<String, Object> matches;
    private ArrayList<StandRankDto> rankings;
}
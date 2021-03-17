package com.kingjakeu.lolesport.api.info.dto.schedule;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Getter
@NoArgsConstructor
public class ScheduleDto {
    private ArrayList<ScheduleEventDto> events;
    private SchedulePageDto pages;
}

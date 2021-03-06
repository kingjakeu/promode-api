package com.kingjakeu.promode.api.crawl.dto.timeline;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Getter
@NoArgsConstructor
public class TimeLineFrameDto {
   private ArrayList<FrameEventDto> events;
   private ParticipantFrameDto participantFrames;
   private Long timestamp;
}

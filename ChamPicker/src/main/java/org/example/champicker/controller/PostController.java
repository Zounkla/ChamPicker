package org.example.champicker.controller;

import com.merakianalytics.orianna.Orianna;
import com.merakianalytics.orianna.types.common.Platform;
import org.example.champicker.model.dto.Duo;
import org.example.champicker.model.dto.Summoner;
import org.example.champicker.service.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;
import java.util.List;

@Controller
public class PostController {

    @Autowired
    private Service service;
    @PostMapping("/postMatches")
    public String getMatches(@ModelAttribute("duo") Duo duo) throws IOException {
        Summoner firstSummoner = duo.getFirstSummoner();
        Summoner secondSummoner = duo.getSecondSummoner();
        String puuid = service.getPUUIDFromName(firstSummoner.getGameName(), firstSummoner.getGameTag());
        String secondPuuid = service.getPUUIDFromName(secondSummoner.getGameName(), secondSummoner.getGameTag());
        List<String> matchIds = service.getMatchesFromPUUID(puuid);
        for (String matchId : matchIds) {
            service.saveMatch(matchId, puuid, secondPuuid);
        }
        return "index";
    }

}

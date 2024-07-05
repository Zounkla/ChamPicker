package org.example.champicker.controller;

import org.example.champicker.model.entity.Duo;
import org.example.champicker.model.entity.Summoner;
import org.example.champicker.model.entity.Champion;
import org.example.champicker.service.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

import java.io.IOException;
import java.util.List;

@SessionAttributes("duo")
@Controller
public class PostController {

    @Autowired
    private Service service;
    @PostMapping("/postMatches")
    public String postMatches(@ModelAttribute("duo") Duo duo, Model model) throws IOException {
        Summoner firstSummoner = duo.getFirstSummoner();
        Summoner secondSummoner = duo.getSecondSummoner();
        String puuid = service.getPUUIDFromName(firstSummoner.getGameName(), firstSummoner.getGameTag());
        String secondPuuid = service.getPUUIDFromName(secondSummoner.getGameName(), secondSummoner.getGameTag());
        List<String> matchIds = service.getMatchesFromPUUID(puuid);
        for (String matchId : matchIds) {
            service.saveMatch(matchId, puuid, secondPuuid, firstSummoner, secondSummoner);
        }
        model.addAttribute("duo", duo);
        model.addAttribute("champion", new Champion());
        return "index";
    }

    @PostMapping("/publishChampionPage")
    public String publishChampionPage(@ModelAttribute("duo") Duo duo, Model model) {
        model.addAttribute("duo", duo);
        model.addAttribute("champion", new Champion());
        return "index";
    }
}

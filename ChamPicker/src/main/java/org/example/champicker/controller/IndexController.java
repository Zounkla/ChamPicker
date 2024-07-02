package org.example.champicker.controller;

import org.example.champicker.model.dto.Duo;
import org.example.champicker.model.dto.Summoner;
import org.example.champicker.model.entity.Champion;
import org.example.champicker.service.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    public IndexController() {
    }

    @GetMapping("/")
    public String homePage(Model model) {
        Duo duo = new Duo();
        duo.setFirstSummoner(new Summoner());
        duo.setSecondSummoner(new Summoner());
        model.addAttribute("duo", duo);
        model.addAttribute("firstChampion", new Champion());
        model.addAttribute("secondChampion", new Champion());
        return "index";
    }
}

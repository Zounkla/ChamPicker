package org.example.champicker.controller;

import org.example.champicker.model.entity.Duo;
import org.example.champicker.model.entity.Summoner;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

@SessionAttributes("duo")
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
        return "duoSelection";
    }
}

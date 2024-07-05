package org.example.champicker.controller;

import org.example.champicker.model.entity.Duo;
import org.example.champicker.model.entity.Champion;
import org.example.champicker.service.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;

@SessionAttributes("duo")
@Controller
public class GetController {

    @Autowired
    private Service service;

    @GetMapping("/getChampion")
    public String getChampion(@ModelAttribute("champion") Champion champion,
                              Model model) {
        Duo duo = (Duo) model.getAttribute("duo");
        model.addAttribute("name", service.bestDuoWith(champion.getName(), duo).getName());
        return "index";
    }
}

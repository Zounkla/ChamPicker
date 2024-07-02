package org.example.champicker.controller;

import org.example.champicker.model.entity.Champion;
import org.example.champicker.service.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class GetController {

    @Autowired
    private Service service;

    @GetMapping("/getFirstChampion")
    public String getFirstChampion(@ModelAttribute("champion") Champion secondChampion, Model model) {
        model.addAttribute("name", service.bestDuoWith(secondChampion.getName(),
                Service.FIRST_POSITION).getName());
        return "index";
    }

    @GetMapping("/getSecondChampion")
    public String getSecondChampion(@ModelAttribute("champion") Champion firstChampion, Model model) {
        model.addAttribute("name", service.bestDuoWith(firstChampion.getName(),
                Service.SECOND_POSITION).getName());
        return "index";
    }
}

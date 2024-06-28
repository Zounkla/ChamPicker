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

    @GetMapping("/getChampion")
    public String getChampion(@ModelAttribute("champion") Champion secondChampion, Model model) {
        model.addAttribute("name", service.bestDuoWith(secondChampion.getName()).getName());
        return "index";
    }
}

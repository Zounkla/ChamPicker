package org.example.champicker.model.entity;

import jakarta.persistence.*;
import org.example.champicker.model.entity.Summoner;

@Entity
public class Duo {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "first_summoner")
    private Summoner firstSummoner;

    @ManyToOne
    @JoinColumn(name = "second_summoner")
    private Summoner secondSummoner;

    public Integer getId() {
        return id;
    }

    public Summoner getFirstSummoner() {
        return firstSummoner;
    }

    public void setFirstSummoner(Summoner firstSummoner) {
        this.firstSummoner = firstSummoner;
    }

    public Summoner getSecondSummoner() {
        return secondSummoner;
    }

    public void setSecondSummoner(Summoner secondSummoner) {
        this.secondSummoner = secondSummoner;
    }
}

package org.example.champicker.model.entity;

import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.hibernate.annotations.Type;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "matchUp")
public class MatchUp {

    private final static int NB_TEAMS = 8;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "first_champion_name")
    private Champion firstChampion;


    @ManyToOne
    @JoinColumn(name = "second_champion_name")
    private Champion secondChampion;

    @ElementCollection
    @Column(name = "tops")
    private List<Integer> tops;

    public MatchUp() {
        tops = new ArrayList<>();
        for (int i = 0; i < NB_TEAMS; ++i) {
            tops.add(0);
        }
    }

    public Integer getId() {
        return id;
    }

    public Champion getFirstChampion() {
        return firstChampion;
    }

    public void setFirstChampion(Champion firstChampion) {
        this.firstChampion = firstChampion;
    }

    public Champion getSecondChampion() {
        return secondChampion;
    }

    public void setSecondChampion(Champion secondChampion) {
        this.secondChampion = secondChampion;
    }

    public List<Integer> getTops() {
        return tops;
    }

    public void setTops(List<Integer> tops) {
        this.tops = tops;
    }

}

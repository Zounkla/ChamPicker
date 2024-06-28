package org.example.champicker.model.repository;

import org.example.champicker.model.entity.Champion;
import org.example.champicker.model.entity.MatchUp;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface MatchUpRepository extends CrudRepository<MatchUp, Integer> {

    Optional<List<MatchUp>> getMatchUpsByFirstChampion(Champion champion);

    Optional<List<MatchUp>> getMatchUpsBySecondChampion(Champion champion);

    Optional<MatchUp> getMatchUpByFirstChampionAndSecondChampion(Champion firstChampion, Champion secondChampion);
}

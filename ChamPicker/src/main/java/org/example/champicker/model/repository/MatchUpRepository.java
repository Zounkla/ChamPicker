package org.example.champicker.model.repository;

import org.example.champicker.model.entity.Duo;
import org.example.champicker.model.entity.Champion;
import org.example.champicker.model.entity.MatchUp;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface MatchUpRepository extends CrudRepository<MatchUp, Integer> {

    Optional<List<MatchUp>> getMatchUpsByDuoAndFirstChampion(Duo duo, Champion champion);

    Optional<List<MatchUp>> getMatchUpsByDuoAndSecondChampion(Duo duo, Champion champion);

    Optional<MatchUp> getMatchUpByDuoAndFirstChampionAndSecondChampion(Duo duo,
                                                                       Champion firstChampion,
                                                                       Champion secondChampion);
}

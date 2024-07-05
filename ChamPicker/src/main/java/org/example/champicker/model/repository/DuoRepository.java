package org.example.champicker.model.repository;

import org.example.champicker.model.entity.Duo;
import org.example.champicker.model.entity.Summoner;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface DuoRepository extends CrudRepository<Duo, Integer> {

    Optional<Duo> findByFirstSummonerAndSecondSummoner(Summoner firstSummoner, Summoner secondSummoner);
}

package org.example.champicker.model.repository;

import org.example.champicker.model.entity.Summoner;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface SummonerRepository extends CrudRepository<Summoner, Integer> {
    Optional<Summoner> findByGameNameAndGameTag(String gameName, String gameTag);
}

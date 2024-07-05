package org.example.champicker.model.repository;

import org.example.champicker.model.entity.Champion;
import org.example.champicker.model.entity.Match;
import org.example.champicker.model.entity.Summoner;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface MatchRepository extends CrudRepository<Match, String> {

    Optional<Match> findByMatchIdAndSummoner(String id, Summoner summoner);
}

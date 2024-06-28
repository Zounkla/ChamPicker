package org.example.champicker.model.repository;

import org.example.champicker.model.entity.Champion;
import org.example.champicker.model.entity.Match;
import org.springframework.data.repository.CrudRepository;

public interface MatchRepository extends CrudRepository<Match, String> {
}

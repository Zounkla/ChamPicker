package org.example.champicker.model.repository;

import org.example.champicker.model.entity.Champion;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ChampionRepository extends CrudRepository<Champion, String> {
}

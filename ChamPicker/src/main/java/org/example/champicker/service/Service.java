package org.example.champicker.service;

import org.example.champicker.model.entity.Duo;
import org.example.champicker.model.entity.Summoner;
import org.example.champicker.model.entity.Champion;
import org.example.champicker.model.entity.Match;
import org.example.champicker.model.entity.MatchUp;
import org.example.champicker.model.repository.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Service
public class Service {

    @Value("${riot.api.key}")
    private String riotAPIKey;

    private final ChampionRepository championRepository;

    private final MatchUpRepository matchUpRepository;

    private final MatchRepository matchRepository;

    private final DuoRepository duoRepository;

    private final SummonerRepository summonerRepository;

    public Service(ChampionRepository championRepository,
                   MatchRepository matchRepository,
                   MatchUpRepository matchUpRepository,
                   DuoRepository duoRepository,
                   SummonerRepository summonerRepository
                   ) {
        this.championRepository = championRepository;
        this.matchRepository = matchRepository;
        this.matchUpRepository = matchUpRepository;
        this.summonerRepository = summonerRepository;
        this.duoRepository = duoRepository;
    }

    public String getPUUIDFromName(String gameName, String tagLine) throws IOException {
        URL url;
        try {
            url = new URL("https://europe.api.riotgames.com/riot/account/v1/accounts/by-riot-id/"
                    + gameName + "/"+ tagLine +
                    "?api_key=" + riotAPIKey);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        BufferedReader br = new BufferedReader(new InputStreamReader((con.getInputStream())));
        StringBuilder sb = new StringBuilder();
        String output;
        while ((output = br.readLine()) != null) {
            sb.append(output);
        }
        JSONObject json = new JSONObject(sb.toString());
        return json.getString("puuid");
    }

    public List<String> getMatchesFromPUUID(String puuid) throws IOException {
        URL url;

        try {
            url = new URL("https://europe.api.riotgames.com/lol/match/v5/matches/by-puuid/"
                    + puuid + "/ids?start=0&count=50&api_key=" +
                    riotAPIKey);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        BufferedReader br = new BufferedReader(new InputStreamReader((con.getInputStream())));
        List<String> tmp = new ArrayList<>();
        String output;
        while ((output = br.readLine()) != null) {
            output = output.substring(1);
            tmp = List.of(output.split(","));
        }
        List<String> matchIds = new ArrayList<>(tmp);
        String lastMatch = matchIds.get(matchIds.size() - 1);
        matchIds.remove(lastMatch);
        lastMatch = lastMatch.substring(0, lastMatch.length() - 1);
        matchIds.add(lastMatch);
        return matchIds;
    }


    public void saveMatch(String matchId, String puuid, String secondPuuid,
                          Summoner firstSummoner, Summoner secondSummoner) throws IOException {
        matchId = matchId.replaceAll("\"", "");
        if (matchRepository.findByMatchIdAndSummoner(matchId, firstSummoner).isPresent()) {
            return;
        }
        URL url;
        try {
            url = new URL("https://europe.api.riotgames.com/lol/match/v5/matches/"
                    + matchId + "?api_key="
                    + riotAPIKey);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        BufferedReader br = new BufferedReader(new InputStreamReader((con.getInputStream())));
        StringBuilder sb = new StringBuilder();
        String output;
        while ((output = br.readLine()) != null) {
            sb.append(output);
        }
        JSONObject json = new JSONObject(sb.toString());
        JSONObject jsonInfo = json.getJSONObject("info");
        JSONArray participants = jsonInfo.getJSONArray("participants");
        Champion first = null;
        Champion second = null;
        int placement = 0;
        for (int i = 0; i < participants.length(); ++i) {
            JSONObject participant = (JSONObject) participants.get(i);
            if (participant.get("puuid").equals(puuid)) {
                String champName = participant.getString("championName");
                Optional<Champion> champion = championRepository.findById(champName);
                if (champion.isPresent()) {
                    first = champion.get();
                } else {
                    first = new Champion();
                    first.setName(champName);
                    championRepository.save(first);
                }
                placement = participant.getInt("placement");
            } else if (participant.get("puuid").equals(secondPuuid)) {
                String champName = participant.getString("championName");
                Optional<Champion> champion = championRepository.findById(champName);
                if (champion.isPresent()) {
                    second = champion.get();
                } else {
                    second = new Champion();
                    second.setName(champName);
                    championRepository.save(second);
                }
            }
        }
        summonerRepository.save(firstSummoner);
        summonerRepository.save(secondSummoner);
        Optional<Duo> duo = duoRepository.findByFirstSummonerAndSecondSummoner(firstSummoner, secondSummoner);
        Duo newDuo = duo.orElseGet(Duo::new);
        newDuo.setFirstSummoner(firstSummoner);
        newDuo.setSecondSummoner(secondSummoner);
        duoRepository.save(newDuo);
        Optional<MatchUp> matchUp = matchUpRepository.getMatchUpByDuoAndFirstChampionAndSecondChampion
                (newDuo, first, second);
        MatchUp newMatchUP;
        newMatchUP = matchUp.orElseGet(MatchUp::new);
        newMatchUP.setFirstChampion(first);
        newMatchUP.setSecondChampion(second);
        newMatchUP.setDuo(newDuo);
        int oldNb = newMatchUP.getTops().get(placement - 1);
        newMatchUP.getTops().set(placement - 1, oldNb + 1);
        matchUpRepository.save(newMatchUP);
        Match match = new Match();
        match.setMatchId(matchId);
        matchRepository.save(match);
    }

    public Champion bestDuoWith(String championName, Duo oldDuo) {
        Optional<Summoner> optionalSummoner = summonerRepository.findByGameNameAndGameTag(
                oldDuo.getFirstSummoner().getGameName(), oldDuo.getFirstSummoner().getGameTag()
        );
        Summoner firstSummoner;
        if (optionalSummoner.isEmpty()) {
            firstSummoner = new Summoner();
            firstSummoner.setGameName(oldDuo.getFirstSummoner().getGameName());
            firstSummoner.setGameTag(oldDuo.getFirstSummoner().getGameTag());
            summonerRepository.save(firstSummoner);
        }
        optionalSummoner = summonerRepository.findByGameNameAndGameTag(
                oldDuo.getSecondSummoner().getGameName(), oldDuo.getSecondSummoner().getGameTag()
        );
        Summoner secondSummoner;
        if (optionalSummoner.isEmpty()) {
            secondSummoner = new Summoner();
            secondSummoner.setGameName(oldDuo.getSecondSummoner().getGameName());
            secondSummoner.setGameTag(oldDuo.getSecondSummoner().getGameTag());
            summonerRepository.save(secondSummoner);
        }
        Optional<Duo> optDuo = duoRepository.findByFirstSummonerAndSecondSummoner(oldDuo.getFirstSummoner(),
                oldDuo.getSecondSummoner());
        Duo duo;
        if (optDuo.isEmpty()) {
            duo = new Duo();
            duo.setFirstSummoner(oldDuo.getFirstSummoner());
            duo.setSecondSummoner(oldDuo.getSecondSummoner());
            duoRepository.save(duo);
        } else {
            duo = optDuo.get();
        }
        Optional<Champion> optionalChampion = championRepository.findById(championName);
        if (optionalChampion.isEmpty()) {
            return bestChampion(championName, duo);
        }
        Champion champion = optionalChampion.get();
        Optional<List<MatchUp>> optionalMatchUps = matchUpRepository.getMatchUpsByDuoAndSecondChampion
                (duo, champion);
        if (optionalMatchUps.isEmpty()) {
            return bestChampion(championName, duo);
        }
        List<MatchUp> matchUps = optionalMatchUps.get();
        double rank = Double.POSITIVE_INFINITY;
        Champion result = null;
        for(MatchUp matchUp : matchUps) {
            Champion otherChampion = matchUp.getFirstChampion();
            double tmp = averageDuoRank(duo, otherChampion, champion);
            if (tmp < rank && tmp >= 1.0) {
                result = otherChampion;
                rank = tmp;
            }
        }
        return rank <= 4.0 ? result : bestChampion(championName, duo);
    }

    private double averageDuoRank(Duo duo, Champion firstChampion, Champion secondChampion) {
        Optional<MatchUp> matchUp = matchUpRepository.getMatchUpByDuoAndFirstChampionAndSecondChampion
                (duo, firstChampion, secondChampion);
        if (matchUp.isEmpty()) {
            return Double.POSITIVE_INFINITY;
        }
        List<Integer> tops = matchUp.get().getTops();
        double averageRank = 0.0;
        int nb = 0;
        for (int i = 1; i <= tops.size(); ++i) {
            nb += tops.get(i - 1);
            averageRank += tops.get(i - 1) * i;

        }
        return averageRank / nb;
    }


    private double averageChampionRank(String championName, Duo duo) {
        Optional<Champion> optionalChampion = championRepository.findById(championName);
        if (optionalChampion.isEmpty()) {
            return 0.0;
        }
        Champion champion = optionalChampion.get();
        Optional<List<MatchUp>> optionalMatchUps = matchUpRepository.getMatchUpsByDuoAndFirstChampion
                (duo, champion);
        if (optionalMatchUps.isEmpty()) {
            return 0.0;
        }
        List<MatchUp> matchUps = optionalMatchUps.get();
        double averageRank = 0.0;
        for (MatchUp matchUp : matchUps) {
            List<Integer> tops = matchUp.getTops();
            for (Integer top : tops) {
                averageRank += top * (tops.indexOf(top) + 1);
            }
        }
        return averageRank / matchUps.size();
    }

    private Champion bestChampion(String championName, Duo duo) {
        Iterable<Champion> champions = championRepository.findAll();
        double rank = Double.POSITIVE_INFINITY;
        Champion result = null;
        for(Champion champion : champions) {
            if (championName.equals(champion.getName())) {
                continue;
            }
            double tmp = averageChampionRank(champion.getName(), duo);
            if (tmp >= 1.0 && tmp < rank) {
                rank = tmp;
                result = champion;
            }
        }
        return result;
    }
}

package org.example.champicker.service;

import com.merakianalytics.orianna.Orianna;
import com.merakianalytics.orianna.types.common.Region;
import com.merakianalytics.orianna.types.core.staticdata.Champions;
import net.sf.saxon.expr.Component;
import org.example.champicker.model.entity.Champion;
import org.example.champicker.model.entity.Match;
import org.example.champicker.model.entity.MatchUp;
import org.example.champicker.model.repository.ChampionRepository;
import org.example.champicker.model.repository.MatchRepository;
import org.example.champicker.model.repository.MatchUpRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Service
public class Service {

    @Value("${riot.api.key}")
    private String riotAPIKey;

    private final ChampionRepository championRepository;

    private final MatchUpRepository matchUpRepository;

    private final MatchRepository matchRepository;

    public Service(ChampionRepository championRepository,
                   MatchRepository matchRepository,
                   MatchUpRepository matchUpRepository) {
        this.championRepository = championRepository;
        this.matchRepository = matchRepository;
        this.matchUpRepository = matchUpRepository;
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


    public void saveMatch(String matchId, String puuid, String secondPuuid) throws IOException {
        matchId = matchId.replaceAll("\"", "");
        if (matchRepository.findById(matchId).isPresent()) {
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
        Optional<MatchUp> matchUp = matchUpRepository.getMatchUpByFirstChampionAndSecondChampion(first, second);
        MatchUp newMatchUP;
        newMatchUP = matchUp.orElseGet(MatchUp::new);
        newMatchUP.setFirstChampion(first);
        newMatchUP.setSecondChampion(second);
        int oldNb = newMatchUP.getTops().get(placement - 1);
        newMatchUP.getTops().set(placement - 1, oldNb + 1);
        matchUpRepository.save(newMatchUP);
        Match match = new Match();
        match.setMatchId(matchId);
        matchRepository.save(match);
    }

    public Champion bestDuoWith(String championName) {
        Optional<Champion> optionalChampion = championRepository.findById(championName);
        if (optionalChampion.isEmpty()) {
            return bestChampion(championName);
        }
        Champion champion = optionalChampion.get();
        Optional<List<MatchUp>> optionalMatchUps  = matchUpRepository.getMatchUpsBySecondChampion(champion);
        if (optionalMatchUps.isEmpty()) {
            return bestChampion(championName);
        }
        List<MatchUp> matchUps = optionalMatchUps.get();
        double rank = Double.POSITIVE_INFINITY;
        Champion result = null;
        for(MatchUp matchUp : matchUps) {
            Champion firstChampion = matchUp.getFirstChampion();
            double tmp = averageDuoRank(firstChampion, champion);
            if (tmp < rank && tmp >= 1.0) {
                result = firstChampion;
                rank = tmp;
            }
        }
        return rank <= 4.0 ? result : bestChampion(championName);
    }

    private double averageDuoRank(Champion firstChampion, Champion secondChampion) {
        Optional<MatchUp> matchUp = matchUpRepository.getMatchUpByFirstChampionAndSecondChampion(firstChampion,
                secondChampion);
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


    private double averageChampionRank(String championName) {
        Optional<Champion> optionalChampion = championRepository.findById(championName);
        if (optionalChampion.isEmpty()) {
            return 0.0;
        }
        Champion champion = optionalChampion.get();
        Optional<List<MatchUp>> optionalMatchUps  = matchUpRepository.getMatchUpsByFirstChampion(champion);
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

    private Champion bestChampion(String championName) {
        Iterable<Champion> champions = championRepository.findAll();
        double rank = Double.POSITIVE_INFINITY;
        Champion result = null;
        for(Champion champion : champions) {
            if (championName.equals(champion.getName())) {
                continue;
            }
            double tmp = averageChampionRank(champion.getName());
            if (tmp >= 1.0 && tmp < rank) {
                rank = tmp;
                result = champion;
            }
        }
        return result;
    }
}

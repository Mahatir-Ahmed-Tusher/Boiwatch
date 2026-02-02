package com.tusher.boiwatch.utils;

import com.tusher.boiwatch.models.PlayerSource;
import java.util.ArrayList;
import java.util.List;

public class PlayerProvider {
    public static List<PlayerSource> getMoviePlayers(String movieId, int startAt) {
        List<PlayerSource> players = new ArrayList<>();
        players.add(new PlayerSource("VidLink", "https://vidlink.pro/movie/" + movieId + "?player=jw&primaryColor=22c55e&secondaryColor=4ade80&iconColor=86efac&autoplay=false&startAt=" + startAt, true, true, true, "Recommended"));
        players.add(new PlayerSource("VidLink 2", "https://vidlink.pro/movie/" + movieId + "?player=hls&primaryColor=22c55e&secondaryColor=4ade80&iconColor=86efac&autoplay=false&startAt=" + startAt, false, false, true, null));
        players.add(new PlayerSource("2Embed", "https://www.2embed.cc/embed/" + movieId, false, false, true, null));
        players.add(new PlayerSource("VidSrc 5", "https://vidsrc.cc/v3/embed/movie/" + movieId + "?autoPlay=false", true, true, true, null));
        players.add(new PlayerSource("MoviesAPI", "https://moviesapi.club/movie/" + movieId, false, false, true, null));
        return players;
    }

    public static List<PlayerSource> getTVPlayers(String tvId, int season, int episode) {
        List<PlayerSource> players = new ArrayList<>();
        players.add(new PlayerSource("VidLink", "https://vidlink.pro/tv/" + tvId + "/" + season + "/" + episode + "?player=jw&primaryColor=22c55e&secondaryColor=4ade80&iconColor=86efac&autoplay=false", true, true, true, "Recommended"));
        players.add(new PlayerSource("VidLink 2", "https://vidlink.pro/tv/" + tvId + "/" + season + "/" + episode + "?player=hls&primaryColor=22c55e&secondaryColor=4ade80&iconColor=86efac&autoplay=false", false, false, true, null));
        players.add(new PlayerSource("2Embed", "https://www.2embed.cc/embed/tv/" + tvId + "/" + season + "/" + episode, false, false, true, null));
        players.add(new PlayerSource("VidSrc 5", "https://vidsrc.cc/v3/embed/tv/" + tvId + "/" + season + "/" + episode + "?autoPlay=false", true, true, true, null));
        players.add(new PlayerSource("MoviesAPI", "https://moviesapi.club/tv/" + tvId + "-" + season + "-" + episode, false, false, true, null));
        return players;
    }
}

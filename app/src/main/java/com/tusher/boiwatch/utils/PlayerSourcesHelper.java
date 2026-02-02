package com.tusher.boiwatch.utils;

import com.tusher.boiwatch.models.Movie;
import com.tusher.boiwatch.models.PlayerSource;
import java.util.ArrayList;
import java.util.List;

public class PlayerSourcesHelper {

    /**
     * Determines the best player sources based on the movie's origin and language.
     */
    public static List<PlayerSource> getPlayerSources(String tmdbId, int startAtSeconds, Movie movieDetails, boolean isTV, int season, int episode) {
        boolean isBangla = isBanglaOrBangladeshiMovie(movieDetails);

        if (isBangla) {
            return getBanglaOptimizedPlayers(tmdbId, isTV, season, episode, startAtSeconds);
        } else {
            return getDefaultGlobalPlayers(tmdbId, isTV, season, episode, startAtSeconds);
        }
    }

    /**
     * Checks if a movie is of Bengali origin or has Bengali language.
     */
    public static boolean isBanglaOrBangladeshiMovie(Movie details) {
        if (details == null) return false;

        // 1. Language check: original_language == "bn"
        if ("bn".equals(details.getOriginalLanguage())) {
            return true;
        }

        // 2. Production Country check: iso_3166_1 == "BD"
        if (details.getProductionCountries() != null) {
            for (Movie.ProductionCountry country : details.getProductionCountries()) {
                if ("BD".equals(country.getIso31661())) {
                    return true;
                }
            }
        }

        // 3. Origin Country check: BD
        if (details.getOriginCountry() != null) {
            for (String countryCode : details.getOriginCountry()) {
                if ("BD".equals(countryCode)) {
                    return true;
                }
            }
        }

        // 4. Regex check for Bengali script in title
        if (details.getTitle() != null && details.getTitle().matches(".*[\\u0980-\\u09FF].*")) {
            return true;
        }

        return false;
    }

    private static List<PlayerSource> getBanglaOptimizedPlayers(String id, boolean isTV, int s, int e, int startAt) {
        List<PlayerSource> sources = new ArrayList<>();
        String type = isTV ? "tv" : "movie";
        String extra = isTV ? "/" + s + "/" + e : "";

        // Recommended sources for Bangla content
        sources.add(new PlayerSource("VidSrc.to", "https://vidsrc.to/embed/" + type + "/" + id + extra, true, true, true, "Recommended"));
        sources.add(new PlayerSource("VidSrc.me", "https://vidsrc.me/embed/" + id + (isTV ? "/" + s + "/" + e : ""), true, true, true, "Fast"));
        
        sources.add(new PlayerSource("Embed.su", "https://embed.su/embed/" + type + "/" + id + extra, false, false, true, null));
        sources.add(new PlayerSource("MultiEmbed.mov", "https://multiembed.mov/?video_id=" + id + (isTV ? "&s=" + s + "&e=" + e : ""), false, false, true, null));
        sources.add(new PlayerSource("2Embed.cc", "https://www.2embed.cc/embed" + (isTV ? "tv?tmdb=" : "/movie/") + id + (isTV ? "&s=" + s + "&e=" + e : ""), false, false, true, null));
        
        String vidLinkUrl = "https://vidlink.pro/" + type + "/" + id + (isTV ? "/" + s + "/" + e : "") + "?player=jw&primaryColor=006fee&secondaryColor=a2a2a2&iconColor=eefdec&autoplay=false";
        if (startAt > 0) vidLinkUrl += "&startAt=" + startAt;
        sources.add(new PlayerSource("VidLink.pro", vidLinkUrl, false, true, false, "JW Player"));
        
        sources.add(new PlayerSource("FlixBaba", "https://flixbaba.com/embed/" + type + "/" + id + extra, false, false, true, "Fallback"));

        return sources;
    }

    private static List<PlayerSource> getDefaultGlobalPlayers(String id, boolean isTV, int s, int e, int startAt) {
        List<PlayerSource> sources = new ArrayList<>();
        String type = isTV ? "tv" : "movie";
        
        // Primary Global Servers
        sources.add(new PlayerSource("VidLink (JW)", "https://vidlink.pro/" + type + "/" + id + (isTV ? "/" + s + "/" + e : "") + "?player=jw", true, true, false, "Recommended"));
        sources.add(new PlayerSource("VidSrc.icu", "https://vidsrc.icu/embed/" + type + "/" + id + (isTV ? "/" + s + "/" + e : ""), false, true, true, null));
        sources.add(new PlayerSource("2Embed", "https://www.2embed.cc/embed" + (isTV ? "tv?tmdb=" : "/movie/") + id + (isTV ? "&s=" + s + "&e=" + e : ""), false, false, true, null));
        sources.add(new PlayerSource("VidSrc.pm", "https://vidsrc.pm/embed/" + type + "/" + id + (isTV ? "/" + s + "/" + e : ""), false, false, true, null));
        sources.add(new PlayerSource("AutoEmbed", "https://player.autoembed.cc/" + type + "/" + id + (isTV ? "/" + s + "/" + e : ""), false, false, true, null));
        
        return sources;
    }
}

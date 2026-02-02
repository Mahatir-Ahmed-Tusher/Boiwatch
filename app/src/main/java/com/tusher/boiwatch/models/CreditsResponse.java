package com.tusher.boiwatch.models;

import java.util.List;

public class CreditsResponse {
    private List<Cast> cast;
    private List<Crew> crew;

    public List<Cast> getCast() {
        return cast;
    }

    public List<Crew> getCrew() {
        return crew;
    }
}

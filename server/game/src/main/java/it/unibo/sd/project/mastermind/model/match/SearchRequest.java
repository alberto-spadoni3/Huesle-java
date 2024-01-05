package it.unibo.sd.project.mastermind.model.match;

public class SearchRequest {
    private final String requester;
    private final boolean isMatchPrivate;
    private final String matchAccessCode;

    public SearchRequest(String requester, boolean isMatchPrivate) {
        this.requester = requester;
        this.isMatchPrivate = isMatchPrivate;
        this.matchAccessCode = null;
    }

    public SearchRequest(String requester, String matchAccessCode) {
        this.requester = requester;
        this.isMatchPrivate = true;
        this.matchAccessCode = matchAccessCode;
    }

    public String getRequester() {
        return requester;
    }

    public boolean isMatchPrivate() {
        return isMatchPrivate;
    }

    public String getMatchAccessCode() {
        return matchAccessCode;
    }
}

package it.unibo.sd.project.webservice.rabbit;

public enum MessageType {
    CREATE("create"),
    JOIN("join"),
    START("start"),
    ATTEMPT("attempt"),
    HINTS("hints"),
    SECRETCODE("secretcode"),
    STOP("stop"),
    SCORES("scores"),
    CHECK("check"),
    FINISH("finish"),
    UPDATE("update"),
    DISCONNECT("disconnect");

    private final String type;

    MessageType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}

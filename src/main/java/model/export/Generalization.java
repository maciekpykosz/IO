package model.export;

public class Generalization {
    private Integer from;
    private Integer to;

    public Generalization(Integer from, Integer to) {
        this.from = from;
        this.to = to;
    }

    public Integer getFrom() {
        return from;
    }

    public Integer getTo() {
        return to;
    }
}

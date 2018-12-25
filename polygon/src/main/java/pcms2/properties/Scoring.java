package pcms2.properties;

public enum Scoring {
    SUM("sum"),
    GROUP("group");

    private String word;

    Scoring(String word) {
        this.word = word;
    }

    @Override
    public String toString() {
        return word;
    }

    public static Scoring parse(String word) {
        switch (word) {
            case "each-test":
                return Scoring.SUM;
            case "complete-group":
                return Scoring.GROUP;
        }
        throw new AssertionError("Couldn't parse scoring = '" + word + "'");
    }

    public static Scoring getScoring(String word) {
        switch (word) {
            case "sum":
                return Scoring.SUM;
            case "group":
                return Scoring.GROUP;
        }
        throw new AssertionError("Couldn't parse scoring = '" + word + "'");
    }
}

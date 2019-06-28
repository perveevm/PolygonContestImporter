package polygon.properties;

public enum FeedbackPolicy {
    NONE("none"),
    POINTS("points"),
    ICPC("icpc"),
    COMPLETE("complete");

    private String word;
    FeedbackPolicy(String word) {
        this.word = word;
    }
    public String toString() {
        return word;
    }

    public static FeedbackPolicy parse(String word) {
        if (word == null) return null;

        switch (word) {
            case "":
                return null;
            case "none":
                return FeedbackPolicy.NONE;
            case "points":
                return FeedbackPolicy.POINTS;
            case "icpc":
                return FeedbackPolicy.ICPC;
            case "complete":
                return FeedbackPolicy.COMPLETE;
        }
        throw new AssertionError("Couldn't parse feedback = '" + word + "'");
    }
}

package pcms2.properties;

import polygon.properties.FeedbackPolicy;

public enum Feedback {
    HIDE("hide"),
    NONE("none"),
    GROUP_SCORE("group-score"),
    GROUP_SCORE_AND_TEST("group-score-and-test"),
    TEST_SCORE("test-score"),
    OUTCOME("outcome"),
    STATISTICS("statistics"),
    COMMENT("comment");

    private String word;

    Feedback(String word) {
        this.word = word;
    }

    @Override
    public String toString() {
        return word;
    }

    /**
     * Maps Polygon feedback policy to PCMS feedback.
     */
    public static Feedback parse(String word) {
        switch (word) {
            case "none":
                return Feedback.NONE;
            case "points":
                return Feedback.GROUP_SCORE;
            case "icpc":
                return Feedback.GROUP_SCORE_AND_TEST;
            case "complete":
                return Feedback.OUTCOME;
        }
        throw new AssertionError("Couldn't parse feedback = '" + word + "'");
    }

    public static Feedback parse(FeedbackPolicy feedbackPolicy) {
        switch (feedbackPolicy) {
            case NONE:
                return Feedback.NONE;
            case POINTS:
                return Feedback.GROUP_SCORE;
            case ICPC:
                return Feedback.GROUP_SCORE_AND_TEST;
            case COMPLETE:
                return Feedback.OUTCOME;
        }
        throw new AssertionError("Couldn't parse feedback = '" + feedbackPolicy + "'");
    }

    /**
     * Gets enum Feedback from PCMS feedback.
     */
    public static Feedback getFeedback(String word) {
        switch (word) {
            case "hide":
                return Feedback.HIDE;
            case "none":
                return Feedback.HIDE;
            case "group-score":
                return Feedback.GROUP_SCORE;
            case "group-score-and-test":
                return Feedback.GROUP_SCORE_AND_TEST;
            case "test-score":
                return Feedback.TEST_SCORE;
            case "outcome":
                return Feedback.OUTCOME;
            case "statistics":
                return Feedback.STATISTICS;
            case "comment":
                return Feedback.COMMENT;
        }
        throw new AssertionError("Couldn't get feedback = '" + word + "'");
    }
}

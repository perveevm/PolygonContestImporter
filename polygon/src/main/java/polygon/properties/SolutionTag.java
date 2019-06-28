package polygon.properties;

public enum SolutionTag {
    ACCEPTED("accepted"),
    MAIN("main"),
    WRONG_ANSWER("wrong-answer"),
    REJECTED("rejected"),
    FAILED("failed"),
    MEMORY_LIMIT_EXCEEDED("memory-limit-exceeded"),
    PRESENTATION_ERROR("presentation-error"),
    TIME_LIMIT_EXCEEDED("time-limit-exceeded"),
    TIME_LIMIT_EXCEEDED_OR_ACCEPTED("time-limit-exceeded-or-accepted"),
    TIME_LIMIT_EXCEEDED_OR_MEMORY_LIMIT_EXCEEDED("time-limit-exceeded-or-memory-limit-exceeded");

    private String word;

    SolutionTag(String word) {
        this.word = word;
    }

    @Override
    public String toString() {
        return word;
    }

    public static SolutionTag parse(String word) {
        switch (word) {
            case "accepted":
                return SolutionTag.ACCEPTED;
            case "main":
                return SolutionTag.MAIN;
            case "wrong-answer":
                return SolutionTag.WRONG_ANSWER;
            case "rejected":
                return SolutionTag.REJECTED;
            case "failed":
                return SolutionTag.FAILED;
            case "memory-limit-exceeded":
                return SolutionTag.MEMORY_LIMIT_EXCEEDED;
            case "presentation-error":
                return SolutionTag.PRESENTATION_ERROR;
            case "time-limit-exceeded":
                return SolutionTag.TIME_LIMIT_EXCEEDED;
            case "time-limit-exceeded-or-accepted":
                return SolutionTag.TIME_LIMIT_EXCEEDED_OR_ACCEPTED;
            case "time-limit-exceeded-or-memory-limit-exceeded":
                return SolutionTag.TIME_LIMIT_EXCEEDED_OR_MEMORY_LIMIT_EXCEEDED;
        }
        throw new AssertionError("WARNING: Couldn't parse solution tag = '" + word + "'");
    }
}

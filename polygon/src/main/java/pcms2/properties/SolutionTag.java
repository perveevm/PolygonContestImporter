package pcms2.properties;

public enum SolutionTag {
    ACCEPTED("accepted"),
    MAIN("main");

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
        }
        throw new AssertionError("WARNING: Couldn't parse solution tag = '" + word + "'");
    }
}

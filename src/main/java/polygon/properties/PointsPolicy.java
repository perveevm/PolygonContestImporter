package polygon.properties;

public enum PointsPolicy {
    EACH_TEST("each-test"),
    COMPLETE_GROUP("complete-group");

    private String word;

    PointsPolicy(String word) {
        this.word = word;
    }

    public String toString() {
        return word;
    }

    public static PointsPolicy parse(String pointsPolicy) {
        if (pointsPolicy == null) return null;

        switch (pointsPolicy) {
            case "":
                return null;
            case "each-test":
                return PointsPolicy.EACH_TEST;
            case "complete-group":
                return PointsPolicy.COMPLETE_GROUP;
        }
        throw new AssertionError("Couldn't parse scoring = '" + pointsPolicy + "'");
    }
}

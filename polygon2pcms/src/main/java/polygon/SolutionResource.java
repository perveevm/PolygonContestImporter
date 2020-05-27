package polygon;

public class SolutionResource {
    private final String path;
    private final String forTypes;
    private final String type;

    public SolutionResource(String path, String forTypes, String type) {
        this.path = path;
        this.forTypes = forTypes;
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public String getForTypes() {
        return forTypes;
    }

    public String getType() {
        return type;
    }
}

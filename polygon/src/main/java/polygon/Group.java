package polygon;

import polygon.properties.FeedbackPolicy;
import polygon.properties.PointsPolicy;
import xmlwrapper.XMLElement;

import java.util.TreeMap;
import java.util.TreeSet;

public class Group {
    String name;
    FeedbackPolicy feedbackPolicy;
    PointsPolicy pointsPolicy;
    double points;
    TreeSet<String> dependencies;
    //parameters from groups.txt
    TreeMap<String, String> parameters;

    public static Group parse(XMLElement groupElement) {
        Group group = new Group();
        group.name = groupElement.getAttribute("name");
        group.pointsPolicy = PointsPolicy.parse(groupElement.getAttribute("points-policy"));
        group.feedbackPolicy = FeedbackPolicy.parse(groupElement.getAttribute("feedback-policy"));
        if (!groupElement.getAttribute("points").isEmpty()) {
            group.points = Double.parseDouble(groupElement.getAttribute("points"));
        }

        XMLElement depElement = groupElement.findFirstChild("dependencies");
        if (depElement.exists()) {
            group.dependencies = new TreeSet<>();
            for (XMLElement dep : depElement.findChildren("dependency")) {
                group.dependencies.add(dep.getAttribute("group"));
            }
        }
        return group;
    }

    public String getName() {
        return name;
    }

    public FeedbackPolicy getFeedbackPolicy() {
        return feedbackPolicy;
    }

    public PointsPolicy getPointsPolicy() {
        return pointsPolicy;
    }

    public double getPoints() {
        return points;
    }

    public TreeSet<String> getDependencies() {
        return dependencies;
    }

    public TreeMap<String, String> getParameters() {
        return parameters;
    }
}

package polygon;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import polygon.properties.FeedbackPolicy;
import polygon.properties.PointsPolicy;

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

    public static Group parse(Element groupElement) {
        Group group = new Group();
        group.name = groupElement.getAttribute("name");
        group.pointsPolicy = PointsPolicy.parse(groupElement.getAttribute("points-policy"));
        group.feedbackPolicy = FeedbackPolicy.parse(groupElement.getAttribute("feedback-policy"));
        if (!groupElement.getAttribute("points").isEmpty()) {
            group.points = Double.parseDouble(groupElement.getAttribute("points"));
        }

        NodeList dependencies = groupElement.getElementsByTagName("dependencies");
        if (dependencies != null && dependencies.getLength() > 0) {
            group.dependencies = new TreeSet<>();
            dependencies = ((Element) dependencies.item(0))
                    .getElementsByTagName("dependency");
            for (int i = 0; i < dependencies.getLength(); i++) {
                Element dep = (Element) dependencies.item(i);
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

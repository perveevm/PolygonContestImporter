package polygon;

import xmlwrapper.XMLElement;

public class Test {
    String group;
    String method;
    String cmd;

    String description;
    double points;
    boolean sample;

    public static Test parse(XMLElement testEl) {
        Test test = new Test();
        test.method = testEl.getAttribute("method");
        test.cmd = testEl.getAttribute("cmd");
        test.description = testEl.getAttribute("description");
        test.points = 0;
        if (!testEl.getAttribute("points").isEmpty()) {
            test.points = Double.parseDouble(testEl.getAttribute("points"));
        }
        test.sample = Boolean.parseBoolean(testEl.getAttribute("sample"));
        test.group = testEl.getAttribute("group");
        return test;
    }

    public String getGroup() {
        return group;
    }

    public String getMethod() {
        return method;
    }

    public String getCmd() {
        return cmd;
    }

    public String getDescription() {
        return description;
    }

    public double getPoints() {
        return points;
    }

    public boolean isSample() {
        return sample;
    }
}

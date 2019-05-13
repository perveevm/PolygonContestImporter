package polygon;

import org.w3c.dom.Element;

public class Test {
    String group;
    String method;
    String cmd;
    double points;
    boolean sample;

    public static Test parse(Element testEl) {
        Test test = new Test();
        test.method = testEl.getAttribute("method");
        test.cmd = testEl.getAttribute("cmd");
        test.points = 0;
        if (!testEl.getAttribute("points").isEmpty()) {
            test.points = Double.parseDouble(testEl.getAttribute("points"));
        }
        test.sample = Boolean.valueOf(testEl.getAttribute("sample"));
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

    public double getPoints() {
        return points;
    }

    public boolean isSample() {
        return sample;
    }
}

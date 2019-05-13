package polygon;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.*;

public class Testset {
    String name;
    String inputPathPattern;
    String outputPathPattern;
    double timeLimit;
    String memoryLimit;
    int testCount;

    //group name maps to group
    TreeMap<String, Group> groups;
    Test[] tests;

    public static Testset parse(Element el) {
        Testset ts = new Testset();

        ts.name = el.getAttribute("name");

        ts.timeLimit = Double.parseDouble(el.getElementsByTagName("time-limit").item(0).
                getChildNodes().item(0).getNodeValue());
        ts.memoryLimit = el.getElementsByTagName("memory-limit").item(0).
                getChildNodes().item(0).getNodeValue();
        ts.testCount = Integer.parseInt(el.getElementsByTagName("test-count").item(0).
                getChildNodes().item(0).getNodeValue());
        ts.inputPathPattern = el.getElementsByTagName("input-path-pattern").item(0).
                getChildNodes().item(0).getNodeValue();
        ts.outputPathPattern = el.getElementsByTagName("answer-path-pattern").item(0).
                getChildNodes().item(0).getNodeValue();

        //tests
        NodeList testsNodeList = el.getElementsByTagName("tests");
        testsNodeList = ((Element) testsNodeList.item(0)).getElementsByTagName("test");
        ts.tests = new Test[ts.testCount];
        for (int j = 0; j < testsNodeList.getLength(); j++) {
            Element testEl = (Element) testsNodeList.item(j);
            ts.tests[j] = Test.parse(testEl);
        }

        //groups
        NodeList groupsList = el.getElementsByTagName("groups");
        if (groupsList != null && groupsList.getLength() > 0) {
            ts.groups = new TreeMap<>();
            groupsList = ((Element) groupsList.item(0)).getElementsByTagName("group");
            for (int i = 0; i < groupsList.getLength(); i++) {
                Element groupElement = (Element) groupsList.item(i);
                Group group = Group.parse(groupElement);
                ts.groups.put(group.name, group);
            }
        }
        return ts;
    }

    public int getSampleTestCount() {
        int res = 0;
        for (int i = 0; i < tests.length; i++) {
            if (tests[i].sample) res++;
        }
        return res;
    }
    public String getName() {
        return name;
    }

    public String getInputPathPattern() {
        return inputPathPattern;
    }

    public String getOutputPathPattern() {
        return outputPathPattern;
    }

    public double getTimeLimit() {
        return timeLimit;
    }

    public String getMemoryLimit() {
        return memoryLimit;
    }

    public int getTestCount() {
        return testCount;
    }

    public TreeMap<String, Group> getGroups() {
        return groups;
    }

    public Test[] getTests() {
        return tests;
    }
}

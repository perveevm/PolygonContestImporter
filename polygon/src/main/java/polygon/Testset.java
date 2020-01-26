package polygon;

import xmlwrapper.XMLElement;

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

    public static Testset parse(XMLElement testsetElement) {
        Testset ts = new Testset();

        ts.name = testsetElement.getAttribute("name");

        ts.timeLimit = Double.parseDouble(testsetElement.findFirstChild("time-limit").getText());
        ts.memoryLimit = testsetElement.findFirstChild("memory-limit").getText();
        ts.testCount = Integer.parseInt(testsetElement.findFirstChild("test-count").getText());
        ts.inputPathPattern = testsetElement.findFirstChild("input-path-pattern").getText();
        ts.outputPathPattern = testsetElement.findFirstChild("answer-path-pattern").getText();

        //tests
        XMLElement testsElement = testsetElement.findFirstChild("tests");
        XMLElement[] tests = testsElement.findChildren("test");
        ts.tests = new Test[ts.testCount];
        for (int j = 0; j < tests.length; j++) {
            ts.tests[j] = Test.parse(tests[j]);
        }

        //groups
        XMLElement groupsElement = testsetElement.findFirstChild("groups");
//        System.out.println("DEBUG: " + groupsList + " " + groupsList.getLength());
        if (groupsElement.exists()) {
            ts.groups = new TreeMap<>();
            XMLElement[] groupsList = groupsElement.findChildren("group");
            for (XMLElement xmlElement : groupsList) {
                Group group = Group.parse(xmlElement);
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

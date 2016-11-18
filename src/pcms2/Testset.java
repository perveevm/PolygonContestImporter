package pcms2;

import java.io.PrintWriter;
import java.util.*;

/**
 * Created by Ilshat on 11/22/2015.
 */
public class Testset {
    public String name;
    public String inputName;
    public String outputName;
    public String inputHref;
    public String outputHref;
    public double timeLimit;
    public String memoryLimit;
    public ArrayList<Group> groups;
    public Test[] tests;

    public Testset() {
        groups = new ArrayList<>();
    }

    public Testset(String name, String input_name,
                   String output_name,
                   String input_href,
                   String output_href,
                   int time_limit,
                   String memor_limit) {
        this.name = name;
        outputName = output_name;
        inputName = input_name;
        inputHref = input_href;
        outputHref = output_href;
        timeLimit = time_limit;
        memoryLimit = memor_limit;
        groups = new ArrayList<>();
    }

    public String formatHref(String in) {
        int begi = in.indexOf("%");
        int endi = in.indexOf("d");
        String tt = in.substring(begi + 1, endi);
        String ttt = "";
        for (int j = 0; j < Integer.parseInt(tt); j++) ttt += "#";

        return in.replace("%" + tt + "d", ttt);
    }
    public void print(PrintWriter pw, String tabs, String type){
        //TODO: Get rid of preliminary testset
        //if (name.equals("preliminary")) {
            //return;
        //}
        pw.println(tabs + "<testset");
        if (!name.equals("preliminary")){
            name = "main";
        }
        if (type.equals("ioi")){
            pw.println(tabs + "\tname = \"" + name + "\"");
            if (name.equals("main") && groups.size() == 0){
                pw.println(tabs + "\tfeedback = \"outcome\"");
            }

        }
        pw.println(tabs + "\tinput-name = \"" + inputName + "\"");
        pw.println(tabs + "\toutput-name = \"" + outputName + "\"");
        pw.println(tabs + "\tinput-href = \"" + formatHref(inputHref) + "\"");
        pw.println(tabs + "\tanswer-href = \"" + formatHref(outputHref) + "\"");
        pw.println(tabs + "\ttime-limit = \"" + timeLimit + "s\"");
        pw.println(tabs + "\tmemory-limit = \"" + memoryLimit + "\"");
        if (type.equals("icpc")) {
            pw.println(tabs + "\ttest-count = \"" + tests.length + "\"");
        }
        pw.println(tabs + ">");

        if (type.equals("ioi")) {
            if (name.equals("preliminary")) {
                for (int i = 0; i < tests.length; i++) {
                    tests[i].println(pw, tabs + "\t");
                }
            } else {
                int g = groups.size();
                if (g == 0) {
                    for (int i = 0; i < tests.length; i++) {
                        tests[i].println(pw, tabs + "\t");
                    }
                } else {
                    for (int i = 0; i < g; i++) {
                        groups.get(i).println(pw, tabs + "\t");
                        for (int j = groups.get(i).first; j <= groups.get(i).last; j++) {
                            tests[j].println(pw, tabs + "\t\t");
                        }
                        pw.println(tabs + "\t</test-group>");
                    }
                }

            }
        }
        pw.println(tabs + "</testset>");
    }
}

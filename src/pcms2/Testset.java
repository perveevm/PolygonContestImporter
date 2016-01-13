package pcms2;

import java.io.PrintWriter;
import java.util.*;

/**
 * Created by Ilshat on 11/22/2015.
 */
public class Testset {
    public String Name;
    public String InputName;
    public String OutputName;
    public String InputHref;
    public String OutputHref;
    public double TimeLimit;
    public String MemoryLimit;
    public ArrayList <Group> groups;
    public Test[] Tests;
    public Testset(){
        groups = new ArrayList<>();
    }
    public Testset(String name, String input_name,
                   String output_name,
                   String input_href,
                   String output_href,
                   int time_limit,
                   String memor_limit){
        Name = name;
        OutputName = output_name;
        InputName = input_name;
        InputHref = input_href;
        OutputHref = output_href;
        TimeLimit = time_limit;
        MemoryLimit = memor_limit;
        groups = new ArrayList<>();
    }
    public String formatHref(String in){
        int begi = in.indexOf("%");
        int endi = in.indexOf("d");
        String tt = in.substring(begi + 1, endi);
        String ttt = "";
        for (int j = 0; j < Integer.parseInt(tt); j++) ttt += "#";

        return in.replace("%" + tt + "d", ttt);
    }
    public void print(PrintWriter pw, String tabs, String type){
		pw.println(tabs + "<testset");
        if (!Name.equals("preliminary")){
            Name = "main";
        }
        if (type.equals("ioi")){
			pw.println(tabs + "\tname = \"" + Name + "\"");
		}
        pw.println(tabs + "\tinput-name = \"" + InputName + "\"");
        pw.println(tabs + "\toutput-name = \"" + OutputName + "\"");
        pw.println(tabs + "\tinput-href = \"" + formatHref(InputHref) + "\"");
        pw.println(tabs + "\tanswer-href = \"" + formatHref(OutputHref) + "\"");
        pw.println(tabs + "\ttime-limit = \"" + TimeLimit + "s\"");
        pw.println(tabs + "\tmemory-limit = \"" + MemoryLimit + "\"");
        if (type.equals("icpc")){
			pw.println(tabs + "\ttest-count = \"" + Tests.length + "\"");
		}
		pw.println(tabs + ">");

        if (type.equals("ioi")){
			if (Name.equals("preliminary")){
				for (int i = 0; i < Tests.length; i++){
					Tests[i].println(pw, tabs + "\t");
				}
			} else {
				int g = groups.size();
				if (g == 0){
					for (int i = 0; i < Tests.length; i++){
						Tests[i].println(pw, tabs + "\t");
					}
				} else {
					for (int i = 0; i < g; i++) {
						groups.get(i).println(pw, tabs + "\t");
						for (int j = groups.get(i).first; j <= groups.get(i).last; j++){
							Tests[j].println(pw, tabs + "\t\t");
						}
						pw.println(tabs + "\t</test-group>");
					}
				}

			}
		}
        pw.println(tabs + "</testset>");
    }
}

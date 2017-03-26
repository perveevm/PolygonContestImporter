

all: polygon.jar

polygon.jar:
	mkdir __temp__
	javac -encoding utf-8 -sourcepath src/ -cp lib/* src/pcms2/Main.java -d __temp__
	jar cfe deploy/polygon.jar pcms2.Main -C __temp__/ .
	rm __temp__ -r

clean:
	rm deploy/polygon.jar


all: compile

compile:
	javac src/geo/*.java -cp lib/commons-cli-1.5.0.jar:lib/json-20230227.jar
	jar cfm Cover.jar lib/MANIFEST.MF src/geo/*.class lib/commons-cli-1.5.0.jar lib/json-20230227.jar
///usr/bin/env jbang \"$0\" \"$@\" ; exit $?
//JAVA 11+
//DEPS info.picocli:picocli:4.6.1
//DEPS com.javax0.jamal:jamal-engine:1.8.0
//DEPS com.javax0.jamal:jamal-api:1.8.0
//DEPS com.javax0.jamal:jamal-tools:1.8.0
//DEPS com.javax0.jamal:jamal-core:1.8.0
//DEPS com.javax0.jamal:jamal-cmd:1.8.0
//DEPS com.javax0.jamal:jamal-snippet:1.8.0
//DEPS com.javax0.jamal:jamal-scriptbasic:1.8.0
//DEPS com.javax0.jamal:jamal-groovy:1.8.0
//DEPS com.javax0.jamal:jamal-ruby:1.8.0
//DEPS com.javax0.jamal:jamal-plantuml:1.8.0
//DEPS com.javax0.jamal:jamal-debug:1.8.0
//DEPS com.javax0.jamal:jamal-jamal:1.8.0
//DEPS com.javax0.jamal:jamal-yaml:1.8.0
//DEPS com.javax0.jamal:jamal-io:1.8.0
//DEPS com.javax0.jamal:jamal-assertions:1.8.0



import javax0.jamal.cmd.JamalMain;

class jbangstarter {
    public static void main(String... args) {
        JamalMain.main(args);
    }
}

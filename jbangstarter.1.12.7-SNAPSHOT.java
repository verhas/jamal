///usr/bin/env jbang \"$0\" \"$@\" ; exit $? jaja
//JAVA 11+
//DEPS com.javax0.jamal:jamal-assertions:1.12.7-SNAPSHOT
//DEPS com.javax0.jamal:jamal-jamal:1.12.7-SNAPSHOT
//DEPS com.javax0.jamal:jamal-markdown:1.12.7-SNAPSHOT
//DEPS com.javax0.jamal:jamal-mock:1.12.7-SNAPSHOT
//DEPS com.javax0.jamal:jamal-plantuml:1.12.7-SNAPSHOT
//DEPS com.javax0.jamal:jamal-snippet:1.12.7-SNAPSHOT
//DEPS com.javax0.jamal:jamal-yaml:1.12.7-SNAPSHOT
//DEPS com.javax0.jamal:jamal-prog:1.12.7-SNAPSHOT
//DEPS com.javax0.jamal:jamal-maven-load:1.12.7-SNAPSHOT
//DEPS com.javax0.jamal:jamal-groovy:1.12.7-SNAPSHOT
//DEPS com.javax0.jamal:jamal-io:1.12.7-SNAPSHOT
//DEPS com.javax0.jamal:jamal-ruby:1.12.7-SNAPSHOT
//DEPS com.javax0.jamal:jamal-scriptbasic:1.12.7-SNAPSHOT
//DEPS com.javax0.jamal:jamal-word:1.12.7-SNAPSHOT
//DEPS com.javax0.jamal:jamal-engine:1.12.7-SNAPSHOT
//DEPS com.javax0.jamal:jamal-api:1.12.7-SNAPSHOT
//DEPS com.javax0.jamal:jamal-tools:1.12.7-SNAPSHOT
//DEPS com.javax0.jamal:jamal-core:1.12.7-SNAPSHOT
//DEPS com.javax0.jamal:jamal-cmd:1.12.7-SNAPSHOT
//DEPS com.javax0.jamal:jamal-debug:1.12.7-SNAPSHOT
//DEPS com.javax0.jamal:jamal-maven-input:1.12.7-SNAPSHOT
//DEPS com.javax0.jamal:jamal-jar-input:1.12.7-SNAPSHOT



import javax0.jamal.cmd.JamalMain;

class jbangstarter {
    public static void main(String... args) {
        JamalMain.main(args);
    }
}

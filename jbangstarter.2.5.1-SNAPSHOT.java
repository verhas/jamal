///usr/bin/env jbang \"$0\" \"$@\" ; exit $? jaja
//JAVA 17+
//DEPS com.javax0.jamal:jamal-assertions:2.5.1-SNAPSHOT
//DEPS com.javax0.jamal:jamal-jamal:2.5.1-SNAPSHOT
//DEPS com.javax0.jamal:jamal-markdown:2.5.1-SNAPSHOT
//DEPS com.javax0.jamal:jamal-mock:2.5.1-SNAPSHOT
//DEPS com.javax0.jamal:jamal-snippet:2.5.1-SNAPSHOT
//DEPS com.javax0.jamal:jamal-yaml:2.5.1-SNAPSHOT
//DEPS com.javax0.jamal:jamal-json:2.5.1-SNAPSHOT
//DEPS com.javax0.jamal:jamal-prog:2.5.1-SNAPSHOT
//DEPS com.javax0.jamal:jamal-maven-load:2.5.1-SNAPSHOT
//DEPS com.javax0.jamal:jamal-sql:2.5.1-SNAPSHOT
//DEPS com.javax0.jamal:jamal-engine:2.5.1-SNAPSHOT
//DEPS com.javax0.jamal:jamal-api:2.5.1-SNAPSHOT
//DEPS com.javax0.jamal:jamal-tools:2.5.1-SNAPSHOT
//DEPS com.javax0.jamal:jamal-core:2.5.1-SNAPSHOT
//DEPS com.javax0.jamal:jamal-cmd:2.5.1-SNAPSHOT
//DEPS com.javax0.jamal:jamal-debug:2.5.1-SNAPSHOT
//DEPS com.javax0.jamal:jamal-maven-input:2.5.1-SNAPSHOT
//DEPS com.javax0.jamal:jamal-jar-input:2.5.1-SNAPSHOT

import javax0.jamal.cmd.JamalMain;

class jbangstarter {
    public static void main(String... args) {
        JamalMain.main(args);
    }
}

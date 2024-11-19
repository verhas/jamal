package javax0.jamal.java;

public class DependencyManagement extends Xml {


    public DependencyManagement dependency(CharSequence... dependencies) {
        for (var dependency : dependencies) {
            if (!(dependency instanceof Dependency)) {
                dependency = Dependency.dependency(dependency);
            }
            add("dependencies", dependency);
        }
        return this;
    }
}


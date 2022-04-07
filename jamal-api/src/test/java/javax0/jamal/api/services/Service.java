package javax0.jamal.api.services;

import javax0.jamal.api.ServiceLoaded;

import java.util.List;

public interface Service extends ServiceLoaded {
    static List<Service> getInstances(){
        return ServiceLoaded.getInstances(Service.class);
    }
}

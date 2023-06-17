package javax0.jamal.api;

import java.util.LinkedHashMap;

/**
 * This interface contains a lot of classes, which can be used in Yaml data structures that hold XML data.
 * When a Yaml data has the type of one of these classes then the Yaml module XML converter will create the
 * appropriate part of the XML. For example, an {@code !attr} object will be converted
 * to an attribute. Each of these classes have an {@code id} as content and nothing else, as there is no need
 * for anything else.
 * <p>
 * These classes are defined in this module to have a simple and general name for them since they are used in the
 * yaml files.
 */
public interface Xml {

    class ATTR extends LinkedHashMap<String,String> {

        public final String id;

        public ATTR() {
            id = null;
        }

        public ATTR(String id) {
            this.id = id;
        }

        public String toString() {
            return id;
        }
    }

    class TEXT {

        public final String id;

        public TEXT() {
            id = null;
        }

        public TEXT(String id) {
            this.id = id;
        }

        public String toString() {
            return id;
        }
    }

    class CDATA {

        public final String id;

        public CDATA() {
            id = null;
        }

        public CDATA(String id) {
            this.id = id;
        }

        public String toString() {
            return id;
        }
    }

    class CDATATEXT {

        public final String id;

        public CDATATEXT() {
            id = null;
        }

        public CDATATEXT(String id) {
            this.id = id;
        }

        public String toString() {
            return id;
        }
    }

    class TAG {

        public final String id;

        public TAG() {
            id = null;
        }

        public TAG(String id) {
            this.id = id;
        }

        public String toString() {
            return id;
        }
    }
}

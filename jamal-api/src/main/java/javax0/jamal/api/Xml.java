package javax0.jamal.api;

import java.util.LinkedHashMap;

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

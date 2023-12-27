package javax0.jamal.java;

public abstract class TextTags {
    public abstract static class TextTag extends Xml {

        TextTag(CharSequence text) {
            super();
            add(lowerCaseFirstCharacter(this.getClass().getSimpleName()), text);
        }

        private static String lowerCaseFirstCharacter(String s) {
            return s.substring(0, 1).toLowerCase() + s.substring(1);
        }

    }


    //<editor-fold id="text_tags">


/*
* This is generated code. DO NOT edit manually.
*/



    public static DefaultGoal defaultGoal(CharSequence defaultGoal) {
        return new DefaultGoal(defaultGoal);
    }

    public static class DefaultGoal extends TextTags.TextTag {
        private DefaultGoal(final CharSequence defaultGoal) {
            super(defaultGoal);
        }
    }



    public static Module module(CharSequence module) {
        return new Module(module);
    }

    public static class Module extends TextTags.TextTag {
        private Module(final CharSequence module) {
            super(module);
        }
    }



    public static FinalName finalName(CharSequence finalName) {
        return new FinalName(finalName);
    }

    public static class FinalName extends TextTags.TextTag {
        private FinalName(final CharSequence finalName) {
            super(finalName);
        }
    }



    public static TargetPath targetPath(CharSequence targetPath) {
        return new TargetPath(targetPath);
    }

    public static class TargetPath extends TextTags.TextTag {
        private TargetPath(final CharSequence targetPath) {
            super(targetPath);
        }
    }



    public static Directory directory(CharSequence directory) {
        return new Directory(directory);
    }

    public static class Directory extends TextTags.TextTag {
        private Directory(final CharSequence directory) {
            super(directory);
        }
    }



    public static Filter filter(CharSequence filter) {
        return new Filter(filter);
    }

    public static class Filter extends TextTags.TextTag {
        private Filter(final CharSequence filter) {
            super(filter);
        }
    }





    public static Filtering filtering(CharSequence filtering) {
        return new Filtering(filtering);
    }

    public static Filtering filtering(boolean filtering) {
        return new Filtering(""+filtering);
    }

    public static class Filtering extends TextTags.TextTag {
        private Filtering(final CharSequence filtering) {
            super(filtering);
        }
    }





    public static Include include(CharSequence include) {
        return new Include(include);
    }

    public static class Include extends TextTags.TextTag {
        private Include(final CharSequence include) {
            super(include);
        }
    }

    public static class Includes extends Xml {
        private Includes() {
            super();
        }
    }
    /**
     
     */
    public static Includes includes(CharSequence ...includes) {
        return new Includes();
    }


    //</editor-fold>
    


}
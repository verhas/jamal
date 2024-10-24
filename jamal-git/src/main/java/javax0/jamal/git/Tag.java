package javax0.jamal.git;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.tools.Scanner;
import javax0.jamal.tools.param.EnumerationParameter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;

import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The {@code Tag} class implements a macro to interact with Git tags and branches in a Jamal environment.
 * It allows retrieving and listing Git tags or branches, optionally filtering or sorting them by name, date,
 * or matching against a regular expression.
 *
 * <p>Supported macro names: "git:tag", "git:tags", "git:branch", "git:branches".
 *
 * <p>Usage examples can include listing tags, filtering them based on regex, or retrieving information such
 * as the commit time, hash, or tag name.
 */
@Macro.Name({"git:tag", "git:tags", "git:branch", "git:branches"})
public class Tag implements Macro, Scanner.WholeInput {

    enum OrderBy {
        orderByName, orderByDate
    }

    enum TagWhat {
        name, time, hash
    }

    /**
     * Evaluates the macro by retrieving and processing Git tags or branches.
     *
     * <p>The macro supports several options such as filtering tags using a regular expression,
     * retrieving tags in a specific order (by name or by date), or extracting specific information
     * such as the tag name, commit time, or hash. It also supports options like retrieving a specific
     * tag by index, or getting the first or last tag.
     *
     * @param in        the input provided to the macro, which can contain additional parameters like the regular expression to match
     * @param processor the Jamal processor instance
     * @return the result of evaluating the macro, which is a list of tags or branches formatted as a string,
     * potentially filtered and sorted based on the input options
     * @throws BadSyntax if there is a conflict in the options provided or if the Git repository cannot be accessed
     */
    @Override
    public String evaluate(Input in, javax0.jamal.api.Processor processor) throws BadSyntax {
        final var me = processor.getId();
        final var scanner = newScanner(in, processor);
        // snippet tag_parameters
        final var id = scanner.str(null, "id").optional();
        // is the identifier of the opened git repository.
        // The default value is `$git` which is also the default value of the `git:connect` macro.
        // If you are not dealing with more than one git repositories you can omit this parameter.
        // Use it only if you also used it in the `git:connect` macro.
        final var match = scanner.str(null, "match").optional();
        // is a regular expression that the tag name should match.
        // If this parameter is present, then only those tags are listed that match the regular expression.
        final var index = scanner.number(null, "index").optional();
        // is the index of the tag to list.
        // If this parameter is present, then only the tag at the index is listed.
        // The index is 0-based.
        // Negative index is also allowed.
        // In this case, the index is counted from the end of the list.
        // For example, -1 means the last tag.
        // If the index is too large or too small, then an error is thrown.
        final var last = scanner.bool(null, "last");
        // If this parameter is present, then only the last tag is listed.
        // It is the same as `index=-1`.
        final var first = scanner.bool(null, "first");
        // If this parameter is present, then only the first tag is listed.
        // It is the same as `index=0`.
        final var single = scanner.bool(null, "single");
        // If this parameter is present, then the result is a single tag.
        // If this parameter is present and the result is more than one tag then an error is thrown.
        final var order = scanner.enumeration(OrderBy.class).defaultValue(OrderBy.orderByName);
        // * `orderByName` orders the tags by name.
        // * `orderByDate` orders the tags by the commit date.
        //+
        // The default value is `orderByName`.
        // `orderByName` and `orderByDate` are exclusive; you can use only one.
        final var what = scanner.enumeration(TagWhat.class).defaultValue(TagWhat.name);
        // * `name` will return the name(s) of the tag(s) or branch(es).
        // * `time` will return the time of the commit of the tag(s) or branch(es).
        // * `hash` will return the hash of the commit of the tag(s) or branch(es).
        //+
        // The default value is `name`.
        // `name`, `time`, and `hash` are exclusive; you can use only one.
        final var sep = scanner.str(null, "sep").defaultValue(",");
        // is the separator between the tags.
        // The default value is `,` (a comma).
        // This string (not only a single character is possible) is used to separate the tags in the result.
        // The list can be used as the value list for the `for` macro.
        // In the very special case when some of the tag or branch names contains a comma, then you can use this parameter.
        // end snippet
        scanner.done();
        BadSyntax.when(last.isPresent() && index.isPresent(), "You cannot specify both 'last' and 'index'");
        BadSyntax.when(first.isPresent() && index.isPresent(), "You cannot specify both 'first' and 'index'");
        BadSyntax.when(last.isPresent() && first.isPresent(), "You cannot specify both 'last' and 'first'");
        try {
            final var git = Connect.git(processor, id);
            final var tags = me.startsWith("git:tag") ? git.tagList().call() : git.branchList().call();
            switch (order.get(OrderBy.class)) {
                case orderByName:
                    tags.sort(Comparator.comparing(Ref::getName));
                    break;
                case orderByDate:
                    tags.sort((tag1, tag2) -> getCommitTimeForTag(tag1, git) - getCommitTimeForTag(tag2, git));
                    break;
            }
            final Function<Ref, String> mapper = getMapper(what, git);
            var tagStream = tags.stream()
                    .map(mapper)
                    .map(s -> s.substring(s.lastIndexOf('/') + 1));
            if (match.isPresent()) {
                final var regex = match.get();
                tagStream = tagStream.filter(s -> s.matches(regex));
            }
            final var tagList = tagStream.collect(Collectors.toList());
            BadSyntax.when(tagList.isEmpty(), "No tags were found");
            if (index.isPresent() || last.isPresent() || first.isPresent()) {
                var i = last.isPresent() ? tagList.size() - 1 : first.isPresent() ? 0 : index.get() < 0 ? tagList.size() + index.get() : index.get();
                BadSyntax.when(i >= tagList.size(), "The index is too large");
                BadSyntax.when(i < 0, "The index is too small");
                return tagList.get(i);
            } else {
                BadSyntax.when(single.is() && tagList.size() > 1, "There are more than one tags");
                return String.join(sep.get(), tagList);
            }
        } catch (BadSyntax bs) {
            throw bs;
        } catch (Exception e) {
            throw new BadSyntax("Cannot list tags from git repository '" + id + "'", e);
        }
    }

    /**
     * Retrieves a mapper function that converts a {@link Ref} object (Git tag or branch) into a string representation.
     *
     * <p>The mapper function depends on the {@code what} parameter and returns either the tag/branch name,
     * the commit hash, or the commit time.
     *
     * @param what the type of information to retrieve (name, hash, or time)
     * @param git  the Git object representing the current repository
     * @return a function that maps a Git {@link Ref} object to a string based on the selected type
     * @throws BadSyntax if the {@code what} parameter is invalid
     */
    private Function<Ref, String> getMapper(EnumerationParameter what, Git git) throws BadSyntax {
        final Function<Ref, String> mapper;
        switch (what.get(TagWhat.class)) {
            case hash:
                mapper = s -> s.getObjectId().getName();
                break;
            case time:
                mapper = s -> String.valueOf(getCommitTimeForTag(s, git));
                break;
            case name:
                mapper = s -> {
                    final var name = s.getName();
                    return name.substring(name.lastIndexOf('/') + 1);
                };
                break;
            default:
                throw new BadSyntax("Internal error: unknown tag what");
        }
        return mapper;
    }

    /**
     * Retrieves the commit time for a given Git tag.
     *
     * <p>This method resolves the commit associated with the provided Git tag reference.
     * If the tag is an annotated tag, the method uses {@link RevWalk} to resolve the tag
     * and get the commit it points to. It then retrieves the commit time of the associated commit.
     *
     * @param tag the {@link Ref} object representing the Git tag
     * @param git the {@link Git} object representing the current repository
     * @return the commit time as an integer, which is the number of seconds since the epoch (Unix timestamp)
     * @throws RuntimeException if the commit time cannot be retrieved due to an error
     */
    private int getCommitTimeForTag(Ref tag, Git git) {
        try (RevWalk revWalk = new RevWalk(git.getRepository())) {
            ObjectId objectId = tag.getObjectId();
            RevCommit commit;

            // Check if the tag is an annotated tag
            RevObject revObject = revWalk.parseAny(objectId);
            if (revObject instanceof RevTag) {
                // If it's an annotated tag, get the commit the tag points to
                RevTag revTag = (RevTag) revObject;
                return (int) revTag.getTaggerIdent().getWhen().getTime() / 1000;
            } else {
                // If it's a lightweight tag, directly parse the commit
                commit = revWalk.parseCommit(objectId);
            }

            return commit.getCommitTime();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve commit time for tag: " + tag.getName(), e);
        }
    }

}

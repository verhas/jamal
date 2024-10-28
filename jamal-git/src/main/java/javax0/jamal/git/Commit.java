package javax0.jamal.git;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.tools.Scanner;
import javax0.jamal.tools.param.EnumerationParameter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * The {@code Commit} class is a Jamal macro for retrieving information about commits in a Git repository.
 * It allows filtering and customizing the output by specifying options such as commit author, date, message,
 * and other commit attributes.
 *
 * <p>This class can be used in Jamal templates to list commits or retrieve specific commit details.
 * Users can configure the output format, specify the branch or tag, and apply filtering options like limit
 * and index to control the commit data displayed.</p>
 *
 * <h2>Example Usage</h2>
 * <pre>
 * {@code
 * // Example macro usage in Jamal template
 * {{git:commit branch="main" limit=5 what=message sep="|"}}
 * }
 * </pre>
 */
@Macro.Name({"git:commit", "git:commits"})
public class Commit implements Macro, Scanner.WholeInput {
    /**
     * Enum defining the attributes that can be retrieved for each commit.
     * Options include:
     * <ul>
     *   <li>{@code hash} - The full SHA-1 hash of the commit.</li>
     *   <li>{@code abbreviated} - The abbreviated SHA-1 hash of the commit.</li>
     *   <li>{@code author} - The name of the author of the commit.</li>
     *   <li>{@code committer} - The name of the committer of the commit.</li>
     *   <li>{@code date} - The author date of the commit (Unix timestamp in **seconds** since the epoch).</li>
     *   <li>{@code commitTime} - The commit time (Unix timestamp in **seconds** since the epoch).</li>
     *   <li>{@code message} - The full commit message.</li>
     *   <li>{@code shortMessage} - The short commit message.</li>
     *   <li>{@code parentIds} - The SHA-1 hashes of the parent commits.</li>
     *   <li>{@code treeId} - The SHA-1 hash of the tree object associated with the commit.</li>
     * </ul>
     */
    enum CommitWhat {
        hash, author, date, commitTime, message, shortMessage, abbreviated, committer, parentIds, treeId
    }

    /**
     * Evaluates the macro input and retrieves the list of commits for the specified Git branch, tag, or ref.
     * The output can be customized to include specific attributes, and filtering options can be applied.
     *
     * @param in        the input provided to the macro
     * @param processor the Jamal processor to handle macro evaluation
     * @return a string containing the selected commit information, formatted according to the specified options
     * @throws BadSyntax if the macro parameters contain errors, or the Git repository cannot be accessed
     */
    @Override
    public String evaluate(Input in, javax0.jamal.api.Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        // snippet commit_parameters
        final var id = scanner.str(null, "id").optional();
        // {%ID%}
        final var refName = scanner.str(null, "branch", "tag", "ref");
        // `tag` or `branch` is the name of the tag, branch to list.
        // You can use any of them interchangeably.
        // It is not an error to use `tag` when you list branches or other ways around.
        // If you want to emphasize that the name can be either a tag or a branch, then use `ref`.
        // * `ref` can be used as an alternative to specifying the name of the branch or tag.
        // You can also specify the hex value of a commit ID to retrieve the values for a single commit.
        // You need to specify at least 4 characters of the commit ID, and it has to be unique.
        // Using at least 7 characters is recommended to avoid ambiguity.
        final var from = scanner.str(null, "from").optional();
        final var to = scanner.str(null, "to").optional();
        // specify the starting and ending commit tags to list.
        // `from` is the starting tag, and `to` is the ending tag.
        // The list will include the commits from the `from` tag to the `to` tag.
        // The order of the list is from the latest to the oldest commit.
        final var limit = scanner.number(null, "limit").optional();
        // is the maximum number of tags to list.
        // Cannot be used when `last` or `index` is also present.
        final var index = scanner.number(null, "index").optional();
        // is the index of the tag to list.
        // If this parameter is present, then only the tag at the index is listed.
        // The index is 0-based.
        // Negative indices are not allowed.
        // `index=0` means the last commit.
        // If the index is too large or negative (would mean future commits), an error is thrown.
        final var last = scanner.bool(null, "last");
        // If this parameter is present, then only the last tag is listed.
        // It is the same as `index=0`.
        final var what = scanner.enumeration(CommitWhat.class).defaultValue(CommitWhat.hash);
        // * {%EXCLUSIVE%}
        //   {%DEFVAL name%}
        // ** `hash` - The full SHA-1 hash of the commit.
        // ** `abbreviated` - The abbreviated SHA-1 hash of the commit.
        // ** `author` - The name of the author of the commit.
        // ** `committer` - The name of the committer of the commit.
        // ** `date` - The author date of the commit (Unix timestamp in **seconds** since the epoch).
        // ** `commitTime` - The commit time (Unix timestamp in **seconds** since the epoch).
        // ** `message` - The full commit message.
        // ** `shortMessage` - The short commit message.
        // ** `parentIds` - The SHA-1 hashes of the parent commits.
        // ** `treeId` - The SHA-1 hash of the tree object associated with the commit.
        final var footnote = scanner.str(null, "footnote").optional();
        // can specify the footnote to use as an output.
        // When this parameter is used, no other output option like `has`, `abbreviated`, `author`, etc. can be used.
        // The output will be the line or lines of the message that follows the footnote line.
        final var sep = scanner.str(null, "sep").defaultValue(",");
        // {%SEPARATOR%}
        // end snippet
        scanner.done();

        final boolean isCommit = refName.name().equals("ref") && refName.get().matches("[0-9a-f]{4,40}");
        // snippet commit_restrictions
        BadSyntax.when(footnote.isPresent() && what.isPresent(), "'footnote' and 'what' cannot be used together.");
        BadSyntax.when(to.isPresent() != from.isPresent(), "You have to specify both 'from' and 'to' or none of them.");
        BadSyntax.when(last.isPresent() && index.isPresent(), "You cannot specify both 'last' and 'index'.");
        BadSyntax.when(last.isPresent() && limit.isPresent(), "You cannot specify both 'last' and 'limit'.");
        BadSyntax.when(index.isPresent() && limit.isPresent(), "You cannot specify both 'index' and 'limit'.");
        BadSyntax.when(to.isPresent() && limit.isPresent(), "You cannot specify both 'to' and 'limit'.");
        BadSyntax.when(isCommit && (from.isPresent() || to.isPresent()), "You cannot specify 'from' or 'to' with a commit ID specified using 'ref'.");
        // end snippet
        final int commitIndex;
        if (index.isPresent() || last.isPresent()) {
            commitIndex = last.isPresent() ? 0 : index.get();
        } else {
            commitIndex = -1;
        }
        try {
            final var git = Connect.git(processor, id);
            final var commits = git.log();
            if (to.isPresent()) {
                commits.addRange(getId(git, from.get()), getId(git, to.get()));
            } else {
                commits.add(isCommit ? getCommitId(git, refName.get()) : getId(git, refName.get()));
            }
            if (isCommit || limit.isPresent() || last.isPresent() || index.isPresent()) {
                commits.setMaxCount(isCommit ? 1 : limit.isPresent() ? limit.get() : commitIndex + 1);
            }
            final var list = commits.call();
            final Function<RevCommit, String> mapper;
            if (footnote.isPresent()) {
                mapper = getMapper(footnote.get());
            } else {
                mapper = getMapper(what);
            }
            final var commitList = StreamSupport.stream(list.spliterator(), false)
                    .map(mapper).collect(Collectors.toList());
            BadSyntax.when(commitList.isEmpty(), "No commits were found");
            if (index.isPresent() || last.isPresent()) {
                BadSyntax.when(commitIndex >= commitList.size(), "The index is too large");
                BadSyntax.when(commitIndex < 0, "The index is too small");
                return commitList.get(commitIndex);
            } else {
                return String.join(sep.get(), commitList);
            }
        } catch (
                BadSyntax bs) {
            throw bs;
        } catch (
                Exception e) {
            throw new BadSyntax("Cannot list branches from git repository '" + id + "'", e);
        }
    }

    private ObjectId getCommitId(Git git, String refName) throws IOException, BadSyntax {
        try {
            // Attempt to resolve as a commit ID or revision string
            ObjectId id = git.getRepository().resolve(refName);
            BadSyntax.when(id == null, "No commit found with the ID: " + refName);
            return id;
        } catch (AmbiguousObjectException e) {
            throw new BadSyntax("The provided commit ID is ambiguous: " + refName);
        } catch (IncorrectObjectTypeException e) {
            // Handle if the resolved object is not a commit
            throw new BadSyntax("The provided ID does not refer to a commit: " + refName);
        }
    }

    private ObjectId getId(Git git, String refName) throws IOException, BadSyntax {
        if (refName.contains("/")) {
            return getObjectId(git.getRepository().findRef("refs/heads/" + refName));
        }
        // Attempt to resolve as a branch or tag name
        var ref = git.getRepository().findRef("refs/tags/" + refName);
        if (ref == null) {
            ref = git.getRepository().findRef("refs/heads/" + refName);
            BadSyntax.when(ref == null, "No branch or tag found with the name: " + refName);
        }
        return getObjectId(ref);
    }

    private ObjectId getObjectId(Ref ref) {
        return ref.getPeeledObjectId() == null ? ref.getObjectId() : ref.getPeeledObjectId();
    }

    /**
     * Extracts the content following a specified footnote prefix from a multi-line text.
     *
     * <p>This method scans each line of the given text, attempting to locate lines that start
     * with the specified {@code footnote}. Once the footnote prefix is found, it extracts
     * the subsequent text after the first colon ({@code :}), handling multi-line content
     * that is indicated by lines ending with an underscore ({@code _}). Lines with this
     * continuation marker are joined into the final result.</p>
     *
     * <p>If the specified {@code footnote} does not exist in the {@code from} text, an empty
     * string is returned. If the footnote appears multiple times in the text, only the first
     * occurrence is processed and returned.</p>
     *
     * @param footnote the footnote prefix to look for in the text, case-insensitive
     * @param from     the multi-line text to search within
     * @return the extracted content following the specified footnote and colon, or an empty
     * string if the footnote is not found
     */
    private static String extract(final String footnote, final String from) {
        final StringBuilder sb = new StringBuilder();
        final var lines = from.split("\n");
        for (int i = 0; i < lines.length; i++) {
            var line = lines[i].stripLeading();
            if (line.toLowerCase().startsWith(footnote.toLowerCase())) {
                line = line.substring(footnote.length()).stripLeading();
                if (line.startsWith(":")) {
                    line = line.substring(1).stripLeading();
                    boolean continuation = line.endsWith("_");
                    if (continuation) {
                        line = line.substring(0, line.length() - 1).stripTrailing();
                    }
                    sb.append(line).append("\n");
                    while (continuation && i < lines.length - 1) {
                        i++;
                        line = lines[i].stripLeading();
                        continuation = line.endsWith("_");
                        if (continuation) {
                            line = line.substring(0, line.length() - 1).stripTrailing();
                        }
                        sb.append(line).append("\n");
                    }
                }
                return sb.toString();
            }
        }
        return "";
    }

    private Function<RevCommit, String> getMapper(final String footnote) throws BadSyntax {
        return s -> extract(footnote, s.getFullMessage());
    }

    /**
     * Provides a function to map a {@link RevCommit} object to a string based on the specified attribute.
     *
     * @param what the attribute of the commit to retrieve (e.g., author, message, date)
     * @return a function that converts a {@link RevCommit} to the desired string representation
     * @throws BadSyntax if the specified attribute is not recognized
     */
    private Function<RevCommit, String> getMapper(final EnumerationParameter what) throws BadSyntax {
        final Function<RevCommit, String> mapper;
        switch (what.get(CommitWhat.class)) {
            case author:
                mapper = s -> s.getAuthorIdent().getName();
                break;
            case message:
                mapper = RevCommit::getFullMessage;
                break;
            case shortMessage:
                mapper = RevCommit::getShortMessage;
                break;
            case date:
                mapper = s -> String.valueOf(s.getAuthorIdent().getWhen().getTime() / 1000);
                break;
            case commitTime:
                mapper = s -> String.valueOf(s.getCommitTime());
                break;

            case hash:
                mapper = AnyObjectId::getName;
                break;
            case abbreviated:
                mapper = s -> s.abbreviate(7).name();
                break;
            case committer:
                mapper = s -> s.getCommitterIdent().getName();
                break;
            case parentIds:
                mapper = s -> Arrays.stream(s.getParents())
                        .map(AnyObjectId::getName)
                        .collect(Collectors.joining(","));
                break;
            case treeId:
                mapper = s -> s.getTree().getName();
                break;
            default:
                throw new BadSyntax("Internal error: unknown tag what");
        }
        return mapper;
    }

}

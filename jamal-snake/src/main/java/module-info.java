import javax0.jamal.api.Macro;
import javax0.jamal.snake.Define;
import javax0.jamal.snake.Dump;
import javax0.jamal.snake.Output;
import javax0.jamal.snake.Ref;
import javax0.jamal.snake.Resolve;

module jamal.snake {
    exports javax0.jamal.snake;
    requires jamal.api;
    requires jamal.tools;
    requires snakeyaml;
    provides Macro with Define, Resolve, Ref, Dump, Output;
}
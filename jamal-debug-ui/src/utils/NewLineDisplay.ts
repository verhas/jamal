import {state} from "./GlobalState"

const showNewLine = (s: string) => {
    if (state.showP) {
        return s.replaceAll("\n", "\u00b6\n");
    } else {
        return s;
    }
}

export default showNewLine;
import {AxiosResponse} from "axios";
import loadSource from "./LoadSource";
import {state} from "../StateHandler"
import debug from "./Debug"

const postAndReload = (x: () => Promise<AxiosResponse>) => {
    x().then(() => loadSource());
};

export const step = () => postAndReload(debug.step);
export const stepInto = () => postAndReload(debug.stepInto);
export const stepOut = () => postAndReload(debug.stepOut);
export const quit = () => postAndReload(debug.quit);
export const run = (evalBreakpoints: any) =>
    debug.run("" + evalBreakpoints?.current?.value)
        .then(loadSource);
export const evaluate = (evalInput: any) =>
    debug.execute("" + evalInput?.current?.value).then((response) => {
        if (typeof response.data != "object") {
            if (response.data.length === 0) {
                state.setEvalOutput("");
                state.setResultCaption("empty evaluation result");
            } else {
                state.setEvalOutput("" + response.data);
                state.setResultCaption("result");
            }
            document.title = "Jamal Debugger";
        } else {
            state.setEvalOutput("" + response?.data?.message + "\n" + response?.data?.trace);
            state.setResultCaption("error result");
            document.title = "Jamal Debugger (e)";
        }
        loadSource();
    });


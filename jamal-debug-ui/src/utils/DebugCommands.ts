import {AxiosError, AxiosResponse} from "axios";
import loadSource from "./LoadSource";
import {state} from "./GlobalState"
import debug from "./Debug"
import {AFTER, RUN, RUN_RESPONSE_CODE, RUN_WAIT} from "../Constants";

const postAndReload = (x: () => Promise<AxiosResponse>) => {
    state.setStateMessage(RUN);
    x().then(loadSource)
        .catch((err: AxiosError<undefined>) => {
            if (err.response?.status === RUN_RESPONSE_CODE) {
                setTimeout(() => postAndReload(x), RUN_WAIT);
            } else {
                setTimeout(loadSource, RUN_WAIT);
            }
        });
};

const postDoubleAndReload = (x: () => Promise<AxiosResponse>) => {
    if (state.stateMessage !== AFTER) {
        postAndReload(x);
        return;
    }
    state.setStateMessage(RUN);
    x().then(
        () => postAndReload(x)
    ).catch(
        (err: AxiosError<undefined>) => {
            if (err.response?.status === RUN_RESPONSE_CODE) {
                setTimeout(() => postDoubleAndReload(x), RUN_WAIT);
            } else {
                setTimeout(loadSource, RUN_WAIT);
            }
        }
    );
};

export const step = () => postDoubleAndReload(debug.step);
export const fetch = () => postAndReload(debug.step);
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
                const resultCaption = "empty evaluation result";
                state.setResultCaption(resultCaption);
                state.setSavedEvalOutput("");
                state.setSavedResultCaption(resultCaption);
            } else {
                const evalOutput = "" + response.data;
                const resultCaption = "result";
                state.setEvalOutput(evalOutput);
                state.setResultCaption(resultCaption);
                state.setSavedEvalOutput(evalOutput);
                state.setSavedResultCaption(resultCaption);
            }
            document.title = "Jamal Debugger";
        } else {
            state.setEvalOutput("" + response?.data?.message + "\n" + response?.data?.trace);
            state.setResultCaption("error result");
            document.title = "Jamal Debugger (e)";
        }
        loadSource();
    });


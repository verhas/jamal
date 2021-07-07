import debug from "./Debug";
import {AxiosError} from "axios";
import {state} from "./GlobalState";

const loadSource = () => {
    debug.all(
        "level&errors&input&output&inputBefore&processing&macros&userDefined&state&output&version"
    )
        .then((response) => {
            console.log(state)
            const level = +(response.data?.level);
            const inputBeforeArray = [];
            for (let i = 0; i < state.inputBeforeArray.length && i < level-1; i++) {
                inputBeforeArray[i] = state.inputBeforeArray[i];
            }
            inputBeforeArray[level-1] = response.data?.inputBefore;
            const outputArray = [];
            for (let i = 0; i < state.outputArray.length && i < level-1; i++) {
                outputArray[i] = state.outputArray[i];
            }
            outputArray[level-1] = response.data?.output;
            state.setDisplayedLevel(level);
            state.setInputBefore(response.data?.inputBefore ?? "");
            state.setInputBeforeArray(inputBeforeArray);
            state.setMacro(response.data?.processing ?? "");
            state.setOutput(response.data?.output ?? "");
            state.setOutputArray(outputArray);
            state.setStateMessage(response.data?.state ?? "");
            state.setLevel(response.data?.level ?? "");
            state.setServerVersion(response.data?.version?.version ?? "unknown");
            const lastErrors = response.data?.errors ?? [];
            state.setErrors(lastErrors);
            if (state.currentTabStop !== 2 && lastErrors.length) {
                state.setEvalOutput(lastErrors.join("\n"));
                state.setResultCaption("execution errors");
            }
            state.setData(response.data);
        })
        .catch((err: AxiosError) => {
            if (err?.response?.status === 503) {
                state.setStateMessage("RUN");
                setTimeout(loadSource, 500);
            } else {
                state.setStateMessage("DISCONNECTED");
                state.setInputBefore("");
                state.setMacro("");
                state.setOutput("");
            }
        });
};

export default loadSource;
import debug from "./Debug";
import {AxiosError} from "axios";
import {state} from "../StateHandler";

const loadSource = () => {
    debug.all(
            "level&errors&input&output&inputBefore&processing&macros&userDefined&state&output&version"
        )
        .then((response) => {
            state.setInputBefore(response.data?.inputBefore ?? "");
            state.setMacro(response.data?.processing ?? "");
            state.setOutput(response.data?.output ?? "");
            state.setStateMessage(response.data?.state ?? "");
            state.setLevel(response.data?.level ?? "");
            state.setServerVersion(response.data?.version?.version ?? "unknown");
            const lastErrors = response.data?.errors ?? [];
            state.setErrors(lastErrors);
            if( state.currentTabStop !== 2 && lastErrors.length ) {
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
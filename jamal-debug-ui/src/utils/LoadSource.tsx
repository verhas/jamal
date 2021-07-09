import debug from "./Debug";
import {AxiosError, AxiosResponse} from "axios";
import {state} from "./GlobalState";
import type Data from '../server/Data';
import {RUN_WAIT, RUN_RESPONSE_CODE, DISCONNECTED, RUN} from "../Constants";

const loadSource = () => {
    debug.all("level&errors&input&output&inputBefore&processing&macros&userDefined&state&output&version")
        .then((response: AxiosResponse<Data>) => {
            const data = response.data
            if (data) {
                const level = +(data.level);

                const inputBeforeArray = state.inputBeforeArray.slice(0, level - 1);
                inputBeforeArray.push(data.inputBefore);

                const outputArray = state.outputArray.slice(0, level - 1);
                outputArray[level - 1] = data.output;

                state.setInputBeforeArray(inputBeforeArray);
                state.setOutputArray(outputArray);

                state.setDisplayedLevel(level);
                state.setInputBefore(data.inputBefore ?? "");
                state.setInputAfter(data.input ?? "");
                state.setMacro(data.processing ?? "");
                state.setOutput(data.output ?? "");
                state.setStateMessage(data.state ?? "");
                state.setLevel(data.level ?? "");
                state.setServerVersion(data.version?.version ?? "unknown");
                const lastErrors = data.errors ?? [];
                if (!state.errors.length && lastErrors.length && !state.wasErrorAlerted) {
                    let [isan, s]: [string, string] =
                        lastErrors.length === 1 ? ["is an", ''] : ["are", 's'];
                    alert(`There ${isan} error${s} in the Jamal source.\n\n`
                        + lastErrors.join("\n")
                        + "\n\n"
                        + "This is a one time only alert. When there is an error "
                        + "the 'ERROR' tab is visible and the level counter is red.");
                    state.setWasErrorAlerted(true);
                }
                state.setErrors(lastErrors);
                state.setData(data);
            } else {
                alert("Server response contained no data.");
            }
        })
        .catch((err: AxiosError<undefined>) => {
            if (err.response?.status === RUN_RESPONSE_CODE) {
                state.setStateMessage(RUN);
                setTimeout(loadSource, RUN_WAIT);
            } else {
                state.setStateMessage(DISCONNECTED);
                state.setInputBefore("");
                state.setMacro("");
                state.setOutput("");
                state.setData({});
            }
        });
};

export default loadSource;
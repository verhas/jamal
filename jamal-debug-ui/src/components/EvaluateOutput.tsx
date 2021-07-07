import {FC} from "react";
import {Paper} from "@material-ui/core";
import SimpleTextOutput from "./SimpleTextOutput";
import {state} from "../utils/GlobalState"
import '../App.css'

const EvaluateOutput: FC = () => {
    return (
        <Paper className="App_Paper, App_Eval">
            <SimpleTextOutput caption={state.resultCaption}>
                {state.evalOutput}
            </SimpleTextOutput>
        </Paper>
    );
}

export default EvaluateOutput;
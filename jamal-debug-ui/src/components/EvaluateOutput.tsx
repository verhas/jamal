import {FC} from "react";
import Grid from "@material-ui/core/Grid";
import {Paper} from "@material-ui/core";
import SimpleTextOutput from "./SimpleTextOutput";
import {state} from "../utils/GlobalState"
import '../App.css'

const EvaluateOutput: FC = () => {
    return (
        <Grid item xs={6}>
            <Paper className="App_Paper, App_Eval">
                <SimpleTextOutput caption={state.resultCaption}>
                    {state.evalOutput}
                </SimpleTextOutput>
            </Paper>
        </Grid>
    );
}

export default EvaluateOutput;
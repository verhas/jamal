import React, {useRef, useEffect} from "react";
import Input from "./components/Input";
import TabPanel from "./components/TabPanel";
import SimpleTextInput from "./components/SimpleTextInput";
import SimpleTextOutput from "./components/SimpleTextOutput";
import Grid from "@material-ui/core/Grid";
import Paper from "@material-ui/core/Paper";
import Evaluate from "@material-ui/icons/TrendingFlat";
import Tabs from "@material-ui/core/Tabs";
import Tab from "@material-ui/core/Tab";
import Run from "@material-ui/icons/DirectionsRun";
import Step from "@material-ui/icons/TextRotationNone";
import Refresh from "@material-ui/icons/Refresh";
import StepInto from "@material-ui/icons/TextRotateVertical";
import StepOut from "@material-ui/icons/TextRotationAngleup";
import Quit from "@material-ui/icons/ExitToApp";
import Label from "./components/Label";
import TitleBar from "./components/TitleBar";
import getVersionMessage from "./utils/VersionFetcher";
import BuiltInMacrosDisplay from "./components/BuiltInMacrosDisplay";
import UserDefinedMacrosDisplay from "./components/UserDefinedMacrosDisplay";
import initState, {state} from "./StateHandler"
import loadSource from "./utils/LoadSource";
import {evaluate, run, stepInto, step, stepOut, quit} from "./utils/DebugCommands"
import Button from "./components/Button";
import "./App.css";

const App = () => {

    initState({
        data: {},
        inputBefore: "",
        macro: "",
        output: "",
        level: "-",
        evalOutput: "",
        stateMessage: "",
        resultCaption: "no result",
        serverVersion: "unknown",
        currentTabStop: 0
    });

    useEffect(() => {
            document.title = "Jamal Debugger";
            loadSource(state);
            // eslint-disable-next-line
        }, []
    )


    const tabPanelChange = (event: React.ChangeEvent<{}>, newTabStop: number) => {
        state.setCurrentTabStop(newTabStop);
    };

    const input2Evaluate = useRef({value: ""});
    const breakpoints = useRef({value: ""});

    const debugButtons = (
        <Grid container direction="column">
            <Grid
                container
                direction="row"
                justify="space-around"
                alignContent="center"
            >
                <Button onClick={loadSource} caption="Refresh"><Refresh/></Button>
                <Button onClick={() => run(breakpoints)} caption="Run"><Run/></Button>
                <Button onClick={step} caption="Step"><Step/></Button>
                <Button onClick={stepInto} caption="Step In"><StepInto/></Button>
                <Button onClick={stepOut} caption="Step out"><StepOut/></Button>
            </Grid>
        </Grid>
    );

    const levelDisplay = (
        <Grid item>
            <Label message={"" + state.level}/>
        </Grid>
    );

    const commandRowDisplay = (
        <>
            <Grid item xs={6}>
                {debugButtons}
            </Grid>
            <Grid item xs={3}>
                {levelDisplay}
            </Grid>
            <Grid
                container
                direction="row"
                xs={3}
                justify="space-around"
                alignItems="flex-end"
                alignContent="flex-end"
            >
                <Button onClick={() => evaluate(input2Evaluate)} color="primary"
                        caption={"Evaluate"}><Evaluate/></Button>
                <Button onClick={quit} caption="Quit" color="secondary"><Quit/></Button>
            </Grid>
        </>
    );

    const builtInMacroList = (
        <Paper className="App_Paper, App_MacroList">
            <BuiltInMacrosDisplay data={state.data}/>
        </Paper>
    );

    const userDefinedMacroList = (
        <Paper className="App_Paper, App_MacroList">
            <UserDefinedMacrosDisplay
                data={state.data}
                captionSetter={state.setResultCaption}
                contentSetter={state.setEvalOutput}
            />
        </Paper>
    );

    const runInput = (
        <Grid item xs={6}>
            <Paper className="App_Paper, run_input">
                <div style={{marginLeft: "30px", fontSize: "12pt"}}>{"input"}</div>
                <Input text={state.inputBefore} macro={state.macro}/>
            </Paper>
        </Grid>
    );

    const runOutput = (
        <Grid item xs={6}>
            <Paper className="App_Paper">
                <div style={{marginLeft: "30px", fontSize: "12pt"}}>{"output"}</div>
                <Input text={state.output}/>
            </Paper>
        </Grid>
    );

    const evaluateInput = (
        <Paper className="App_Paper, App_Eval">
            <SimpleTextInput caption={"evaluate"} reference={input2Evaluate}>
                {""}
            </SimpleTextInput>
        </Paper>
    );

    const breakPointsInput = (
        <Paper className="App_Paper, App_Eval">
            <SimpleTextInput caption={"breakpoints"} reference={breakpoints}>
                {""}
            </SimpleTextInput>
        </Paper>
    );

    const evaluateOutput = (
        <Grid item xs={6}>
            <Paper className="App_Paper, App_Eval">
                <SimpleTextOutput caption={state.resultCaption}>
                    {state.evalOutput}
                </SimpleTextOutput>
            </Paper>
        </Grid>
    );

    let versionMessage = getVersionMessage();

    return (
        <div className="App">
            <header className="App-header">
                <Grid container direction="row">
                    <TitleBar message={state.stateMessage}/>
                </Grid>
                <Grid container direction="row">
                    {commandRowDisplay}
                </Grid>

                <Grid
                    container
                    direction="row"
                    spacing={2}
                    style={{width: "100%"}}
                    justify="space-around"
                >
                    {runInput}

                    <Grid item xs={6}>
                        <Tabs
                            value={state.currentTabStop}
                            onChange={tabPanelChange}
                            className="tab_panel"
                            centered
                            indicatorColor="secondary"
                        >
                            <Tab value={0} label="built-in macros"/>
                            <Tab value={1} label="user defined"/>
                            <Tab value={2} label="evaluate"/>
                            <Tab value={3} label="breakpoints"/>
                        </Tabs>
                        <TabPanel id="0" hidden={state.currentTabStop !== 0} other="">
                            {builtInMacroList}
                        </TabPanel>
                        <TabPanel id="1" hidden={state.currentTabStop !== 1} other="">
                            {userDefinedMacroList}
                        </TabPanel>
                        <TabPanel id="2" hidden={state.currentTabStop !== 2} other="">
                            {evaluateInput}
                        </TabPanel>
                        <TabPanel id="3" hidden={state.currentTabStop !== 3} other="">
                            {breakPointsInput}
                        </TabPanel>
                    </Grid>
                </Grid>

                <Grid
                    container
                    direction="row"
                    spacing={2}
                    style={{width: "100%"}}
                    justify="space-around"
                >
                    {runOutput}
                    {evaluateOutput}
                </Grid>
                <Grid
                    container
                    direction="row"
                    spacing={2}
                    style={{width: "100%"}}
                    justify="space-around"
                >
                    <Grid item xs={12}>
                        <div className="App_LicenseLine">
                            {"Peter Verhas 2021, Apache License 2.0, "}
                            <a href="https://github.com/verhas/jamal">
                                {"https://github.com/verhas/jamal"}
                            </a>
                            {", " + versionMessage}
                        </div>
                    </Grid>
                </Grid>
            </header>
        </div>
    );
}

export default App;

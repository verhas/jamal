import React, { useCallback, useEffect, useState } from "react";
import Input from "./components/Input";
import SimpleTextInput from "./components/SimpleTextInput";
import SimpleTextOutput from "./components/SimpleTextOutput";
import Button from "@material-ui/core/Button";
import Grid from "@material-ui/core/Grid";
import Paper from "@material-ui/core/Paper";
import Run from "@material-ui/icons/TrendingFlat";
import Step from "@material-ui/icons/TextRotationNone";
import StepInto from "@material-ui/icons/TextRotateVertical";
import StepOut from "@material-ui/icons/TextRotationAngleup";
import Quit from "@material-ui/icons/ExitToApp";
import Label from "./components/Label";
import TitleBar from "./components/TitleBar";
import queryString from "querystring";
import DebugCommand from "./utils/DebugCommand";
import BuiltInMacrosDisplay from "./components/BuiltInMacrosDisplay";
import UserDefinedMacrosDisplay from "./components/UserDefinedMacrosDisplay";

import { AxiosError, AxiosResponse } from "axios";
import "./App.css";

var debug: DebugCommand = new DebugCommand("localhost", 8080);
var qs = queryString.parse(window.location.search.substring(1));

function App() {
  const [data, setData] = useState<any>({});
  const [inputBefore, setInputBefore] = useState<string>("");
  const [macro, setMacro] = useState<string>("");
  const [output, setOutput] = useState<string>("");
  const [level, setLevel] = useState<string>("-");
  const [evalInput, setEvalInput] = useState<string>("");
  const [evalOutput, setEvalOutput] = useState<string>("");
  const [stateMessage, setStateMessage] = useState("");

  const port: string = qs.port
    ? "" + qs.port
    : new URL(window.location.href).port;

  debug = new DebugCommand("localhost", +port);

  const reloadActualSource = useCallback(() => {
    debug
      .all(
        "level&input&output&inputBefore&processing&macros&userDefined&state&output"
      )
      .then((response) => {
        setInputBefore(response.data?.inputBefore);
        setMacro(response.data?.processing);
        setOutput(response.data?.output);
        setStateMessage(response.data?.state);
        setLevel(response.data?.level);
        setData(response.data);
      })
      .catch((err: AxiosError) => {
        if (err?.response?.status === 503) {
          setStateMessage("RUN");
          setTimeout(reloadActualSource, 500);
        } else {
          console.log(err);
          setStateMessage("DISCONNECTED");
          setInputBefore("");
          setMacro("");
          setOutput("");
          //setTimeout(reloadActualSource, 1000);
        }
      });
  }, []);

  const postAndReload = (x: () => Promise<AxiosResponse<any>>) => {
    x().then(() => reloadActualSource());
  };
  const step = () => postAndReload(debug.step);
  const stepInto = () => postAndReload(debug.stepInto);
  const stepOut = () => postAndReload(debug.stepOut);
  const run = () => postAndReload(debug.run);
  const quit = () => postAndReload(debug.quit);
  const evaluate = () =>
    debug.execute(evalInput).then((response) => {
      setEvalOutput(response.data);
    });

  useEffect(() => {
    reloadActualSource();
  });

  const debugButtons = (
    <Grid container direction="column">
      <Grid
        container
        direction="row"
        justify="space-around"
        alignContent="center"
      >
        <Grid item>
          <Button variant="contained" onClick={run}>
            <Run />
          </Button>
          <div style={{ marginLeft: "20px", fontSize: "8pt" }}>{"Run"}</div>
        </Grid>
        <Grid item>
          <Button variant="contained" onClick={step}>
            <Step />
          </Button>
          <div style={{ marginLeft: "17px", fontSize: "8pt" }}>{"Step"} </div>
        </Grid>
        <Grid item>
          <Button variant="contained" onClick={stepInto}>
            <StepInto />
          </Button>
          <div style={{ marginLeft: "15px", fontSize: "8pt" }}>{"Step In"}</div>
        </Grid>
        <Grid item>
          <Button variant="contained" onClick={stepOut}>
            <StepOut />
          </Button>
          <div style={{ marginLeft: "10px", fontSize: "8pt" }}>
            {"Step Out"}
          </div>
        </Grid>
        <Grid item>
          <Button variant="contained" onClick={evaluate} color="primary">
            <Run />
          </Button>
          <div onClick={evaluate} style={{ fontSize: "8pt" }}>
            {"evaluate"}
          </div>
        </Grid>
      </Grid>
    </Grid>
  );

  const levelDisplay = (
    <>
      <Grid item>
        <Label message={"Level: " + level} />
      </Grid>
    </>
  );

  const quitIconDisplay = (
    <Grid item>
      <Button variant="contained" onClick={quit} color="secondary">
        <Quit />
      </Button>
    </Grid>
  );
  const commandRowDisplay = (
    <>
      <Grid container direction="column" xs={3}>
        {levelDisplay}
      </Grid>
      <Grid container direction="column" xs={6}>
        <Grid item>{debugButtons}</Grid>
      </Grid>
      <Grid
        container
        direction="row"
        xs={3}
        justify="space-around"
        alignItems="flex-end"
        alignContent="flex-end"
      >
        {quitIconDisplay}
      </Grid>
    </>
  );
  const macroLists = (
    <Grid
      container
      direction="column"
      spacing={1}
      xl={3}
      justify="space-around"
    >
      <Grid item>
        <Paper className="App_Paper, App_MacroList">
          <BuiltInMacrosDisplay data={data} />
        </Paper>
      </Grid>
      <Grid item>
        <Paper className="App_Paper, App_MacroList">
          <UserDefinedMacrosDisplay data={data} />
        </Paper>
      </Grid>
    </Grid>
  );

  const runInputOutput = (
    <Grid
      container
      direction="column"
      xs={6}
      justify="space-around"
      spacing={3}
    >
      <Grid item>
        <Paper className="App_Paper">
          <div style={{ marginLeft: "30px", fontSize: "12pt" }}>{"input"}</div>
          <Input text={inputBefore} macro={macro} />
        </Paper>
      </Grid>
      <Grid item>
        <Paper className="App_Paper">
          <div style={{ marginLeft: "30px", fontSize: "12pt" }}>{"output"}</div>
          <Input text={output} />
        </Paper>
      </Grid>
    </Grid>
  );

  const evaluateIoAndBreakpoints = (
    <Grid container direction="column" xs={3} justify="space-around">
      <Grid item>
        <Paper className="App_Paper">
          <SimpleTextInput
            text={evalInput}
            onChangeHandler={(e) => {
              setEvalInput("" + e.target.value);
            }}
          />
        </Paper>
      </Grid>
      <Grid item>
        <Paper className="App_Paper">
          <SimpleTextOutput text={evalOutput} />
        </Paper>
      </Grid>
      <Paper className="App_Paper">
        <Grid item>
          <SimpleTextInput text="breakpoints" onChangeHandler={(a) => {}} />
        </Grid>
      </Paper>
    </Grid>
  );

  const inputOutputDisplay = (
    <>
      {macroLists}
      {runInputOutput}
      {evaluateIoAndBreakpoints}
    </>
  );

  return (
    <div className="App">
      <header className="App-header">
        <Grid
          container
          direction="column"
          className="AppTopContainer"
          spacing={10}
          justify="space-around"
          alignContent="flex-start"
          alignItems="flex-start"
        >
          <Grid container direction="row">
            <TitleBar message={stateMessage} />
          </Grid>
          <Grid container direction="row">
            {commandRowDisplay}
          </Grid>
          <Grid container direction="row" alignItems="flex-start">
            {inputOutputDisplay}
          </Grid>
        </Grid>
      </header>
    </div>
  );
}

export default App;

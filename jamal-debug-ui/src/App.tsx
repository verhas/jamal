import { useCallback, useEffect, useState, useRef } from "react";
import Input from "./components/Input";
import SimpleTextInput from "./components/SimpleTextInput";
import SimpleTextOutput from "./components/SimpleTextOutput";
import Button from "@material-ui/core/Button";
import Grid from "@material-ui/core/Grid";
import Paper from "@material-ui/core/Paper";
import Evaluate from "@material-ui/icons/TrendingFlat";
import Run from "@material-ui/icons/DirectionsRun";
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
  const evalInput = useRef({ value: "" });
  const [evalOutput, setEvalOutput] = useState<string>("");
  const [stateMessage, setStateMessage] = useState("");
  const [isLoading, setIsloading] = useState(true);

  const port: string = qs.port
    ? "" + qs.port
    : new URL(window.location.href).port;

  debug.port = +port;

  const reloadActualSource = useCallback(() => {
    debug
      .all(
        "level&input&output&inputBefore&processing&macros&userDefined&state&output"
      )
      .then((response) => {
        setInputBefore(response.data?.inputBefore ?? '');
        setMacro(response.data?.processing ?? '');
        setOutput(response.data?.output ?? '');
        setStateMessage(response.data?.state ?? '');
        setLevel(response.data?.level ?? '');
        setData(response.data);
      })
      .catch((err: AxiosError) => {
        if (err?.response?.status === 503) {
          setStateMessage("RUN");
          setTimeout(reloadActualSource, 500);
        } else {
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
    debug.execute("" + evalInput?.current?.value).then((response) => {
      if (typeof response.data != "object") {
        if (response.data.length === 0) {
          setEvalOutput("OK");
        } else {
          setEvalOutput("" + response.data);
        }
        document.title = "Jamal Debugger";
      } else {
        setEvalOutput("" + response.data.trace);
        document.title = "Jamal Debugger (e)";
      }
      //reloadActualSource();
    });

  useEffect(() => {
    if (isLoading) {
      document.title = "Jamal Debugger";
      setIsloading(false);
      reloadActualSource();
    }
  });

const buttonCaption = (caption:string) =>{
  const m = (21-caption.length)+"px";
  return <div style={{ marginLeft: m, fontSize: "8pt" }}>{caption}</div>;
};

  const evaluateButton = (
    <Grid item>
      <Button variant="contained" onClick={evaluate} color="primary">
        <Evaluate />
      </Button>
      {buttonCaption("Evaluate")}
    </Grid>
  );

  const quitButton = (
    <Grid item>
      <Button variant="contained" onClick={quit} color="secondary">
        <Quit />
      </Button>
      {buttonCaption("Quit")}
    </Grid>
  );

  const runButton = (
    <Grid item>
      <Button variant="contained" onClick={run}>
        <Run />
      </Button>
      {buttonCaption("Run")}
    </Grid>
  );

  const stepButton = (
    <Grid item>
      <Button variant="contained" onClick={step}>
        <Step />
      </Button>
      {buttonCaption("Step")}
    </Grid>
  );

  const stepIntoButton = (
    <Grid item>
      <Button variant="contained" onClick={stepInto}>
        <StepInto />
      </Button>
      {buttonCaption("Step In")}
    </Grid>
  );

  const stepOutButton = (
    <Grid item>
      <Button variant="contained" onClick={stepOut}>
        <StepOut />
      </Button>
      {buttonCaption("Step Out")}
    </Grid>
  );

  const debugButtons = (
    <Grid container direction="column">
      <Grid
        container
        direction="row"
        justify="space-around"
        alignContent="center"
      >
        {runButton} {stepButton} {stepIntoButton} {stepOutButton} {quitButton}
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
        {evaluateButton}
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
      <Grid item style={{ paddingBottom: "6px" }}>
        <Paper className="App_Paper" style={{ height:"285px"}}>
          <div style={{ marginLeft: "30px", fontSize: "12pt" }}>{"input"}</div>
          <Input text={inputBefore} macro={macro} />
        </Paper>
      </Grid>
      <Grid item>
        <Paper className="App_Paper" style={{height: "285px"}}>
          <div style={{ marginLeft: "30px", fontSize: "12pt" }}>{"output"}</div>
          <Input text={output} />
        </Paper>
      </Grid>
    </Grid>
  );

  const evaluateIo = (
    <Grid container direction="column" xs={3} justify="space-around">
      <Grid item>
        <Paper className="App_Paper, App_Eval">
          <SimpleTextInput caption={"evaluate"} reference={evalInput}>
            {""}
          </SimpleTextInput>
        </Paper>
      </Grid>
      <Grid item>
        <Paper className="App_Paper, App_Eval">
          <SimpleTextOutput>{evalOutput}</SimpleTextOutput>
        </Paper>
      </Grid>
    </Grid>
  );

  const inputOutputDisplay = (
    <>
      {macroLists}
      {runInputOutput}
      {evaluateIo}
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

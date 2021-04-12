import { useCallback, useState, useRef } from "react";
import Input from "./components/Input";
import SimpleTextInput from "./components/SimpleTextInput";
import SimpleTextOutput from "./components/SimpleTextOutput";
import Button from "@material-ui/core/Button";
import Grid from "@material-ui/core/Grid";
import Paper from "@material-ui/core/Paper";
import Evaluate from "@material-ui/icons/TrendingFlat";
import Run from "@material-ui/icons/DirectionsRun";
import Step from "@material-ui/icons/TextRotationNone";
import Refresh from "@material-ui/icons/Refresh";
import StepInto from "@material-ui/icons/TextRotateVertical";
import StepOut from "@material-ui/icons/TextRotationAngleup";
import Quit from "@material-ui/icons/ExitToApp";
import Label from "./components/Label";
import TitleBar from "./components/TitleBar";
import queryString from "querystring";
import DebugCommand from "./utils/DebugCommand";
import VersionFetcher from "./utils/VersionFetcher";
import BuiltInMacrosDisplay from "./components/BuiltInMacrosDisplay";
import UserDefinedMacrosDisplay from "./components/UserDefinedMacrosDisplay";
import packageJson from "../package.json";

import { AxiosError, AxiosResponse } from "axios";
import "./App.css";

var debug: DebugCommand = new DebugCommand("localhost", 8080);
var qs = queryString.parse(window.location.search.substring(1));
const versionFetcher = new VersionFetcher();
var latestVersion = "";

function App() {
  const [data, setData] = useState<any>({});
  const [inputBefore, setInputBefore] = useState<string>("");
  const [macro, setMacro] = useState<string>("");
  const [output, setOutput] = useState<string>("");
  const [level, setLevel] = useState<string>("-");
  const [evalOutput, setEvalOutput] = useState<string>("");
  const [stateMessage, setStateMessage] = useState("");
  const [resultCaption, setResultCaption] = useState("no result");
  const [serverVersion, setServerVersion] = useState("unknown");
  const [isLoading, setIsloading] = useState(true);
  const evalInput = useRef({ value: "" });

  const port: string = qs.port
    ? "" + qs.port
    : new URL(window.location.href).port;

  debug.port = +port;

  const reloadActualSource = useCallback(() => {
    debug
      .all(
        "level&input&output&inputBefore&processing&macros&userDefined&state&output&version"
      )
      .then((response) => {
        setInputBefore(response.data?.inputBefore ?? "");
        setMacro(response.data?.processing ?? "");
        setOutput(response.data?.output ?? "");
        setStateMessage(response.data?.state ?? "");
        setLevel(response.data?.level ?? "");
        setServerVersion(response.data?.version?.version ?? "unknown");
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
          setEvalOutput("");
          setResultCaption("empty evaluation result");
        } else {
          setEvalOutput("" + response.data);
          setResultCaption("result");
        }
        document.title = "Jamal Debugger";
      } else {
        setEvalOutput("" + response.data.trace);
        setResultCaption("error result");
        document.title = "Jamal Debugger (e)";
      }
      reloadActualSource();
    });

  if (latestVersion === "") {
    latestVersion = versionFetcher.lastRelease;
  }

  if (isLoading) {
    document.title = "Jamal Debugger";
    setIsloading(false);
    reloadActualSource();
  }

  const buttonCaption = (caption: string) => {
    const m = 21 - caption.length + "px";
    return <div style={{ marginLeft: m, fontSize: "8pt" }}>{caption}</div>;
  };

  const refreshButton = (
    <Grid item>
      <Button variant="contained" onClick={reloadActualSource}>
        <Refresh />
      </Button>
      {buttonCaption("Refresh")}
    </Grid>
  );

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
        {refreshButton} {runButton} {stepButton} {stepIntoButton}
        {stepOutButton}
      </Grid>
    </Grid>
  );

  const levelDisplay = (
    <>
      <Grid item>
        <Label message={"" + level} />
      </Grid>
    </>
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
        {evaluateButton}
        {quitButton}
      </Grid>
    </>
  );

  const builtInMacroList = (
    <Grid item xs={3}>
      <Paper className="App_Paper, App_MacroList">
        <BuiltInMacrosDisplay data={data} />
      </Paper>
    </Grid>
  );
  const userDefinedMacroList = (
    <Grid item xs={3}>
      <Paper className="App_Paper, App_MacroList">
        <UserDefinedMacrosDisplay
          data={data}
          captionSetter={setResultCaption}
          contentSetter={setEvalOutput}
        />
      </Paper>
    </Grid>
  );

  const runInput = (
    <Grid item xs={6}>
      <Paper className="App_Paper">
        <div style={{ marginLeft: "30px", fontSize: "12pt" }}>{"input"}</div>
        <Input text={inputBefore} macro={macro} />
      </Paper>
    </Grid>
  );

  const runOutput = (
    <Grid item xs={6}>
      <Paper className="App_Paper">
        <div style={{ marginLeft: "30px", fontSize: "12pt" }}>{"output"}</div>
        <Input text={output} />
      </Paper>
    </Grid>
  );

  const evaluateInput = (
    <Grid item xs={3}>
      <Paper className="App_Paper, App_Eval">
        <SimpleTextInput caption={"evaluate"} reference={evalInput}>
          {""}
        </SimpleTextInput>
      </Paper>
    </Grid>
  );

  const evaluateOutput = (
    <Grid item xs={3}>
      <Paper className="App_Paper, App_Eval">
        <SimpleTextOutput caption={resultCaption}>
          {evalOutput}
        </SimpleTextOutput>
      </Paper>
    </Grid>
  );
  var versionMessage;
  if (serverVersion === packageJson.version) {
    versionMessage = "Version: " + serverVersion;
  } else {
    versionMessage =
      "Server version: " +
      serverVersion +
      ", Client version: " +
      packageJson.version;
  }
  if (
    latestVersion != "" &&
    (latestVersion != serverVersion || latestVersion != packageJson.version)
  ) {
    versionMessage += ", Latest release: " + latestVersion;
  }

  return (
    <div className="App">
      <header className="App-header">
        <Grid container direction="row">
          <TitleBar message={stateMessage} />
        </Grid>
        <Grid container direction="row">
          {commandRowDisplay}
        </Grid>
        <Grid
          container
          direction="row"
          spacing={2}
          style={{ width: "100%" }}
          justify="space-around"
        >
          {runInput}
          {builtInMacroList}
          {evaluateInput}
        </Grid>
        <Grid
          container
          direction="row"
          spacing={2}
          style={{ width: "100%" }}
          justify="space-around"
        >
          {runOutput}
          {userDefinedMacroList}
          {evaluateOutput}
        </Grid>
        <Grid
          container
          direction="row"
          spacing={2}
          style={{ width: "100%" }}
          justify="space-around"
        >
          <Grid item xs={12}>
            <div className="App_LicenseLine">
              {"v1.0.0, Apache License 2.0, "}
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

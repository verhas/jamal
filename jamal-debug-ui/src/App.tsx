import React, { useEffect, useState } from "react";
import Input from "./components/Input";
import PortInput from "./components/PortInput";
import IconButton from "@material-ui/core/Button";
import Grid from "@material-ui/core/Grid";
import Run from "@material-ui/icons/TrendingFlat";
import Step from "@material-ui/icons/TextRotationNone";
import StepInto from "@material-ui/icons/TextRotateVertical";
import StepOut from "@material-ui/icons/TextRotationAngleup";
import Quit from "@material-ui/icons/ExitToApp";
import Label from "./components/Label";
import TitleBar from "./components/TitleBar";
import "./App.css";
import DebugCommand from "./utils/DebugCommand";
import { AxiosError } from "axios";

function App() {
  const [inputBefore, setInputBefore] = useState<string>("");
  const [macro, setMacro] = useState<string>("");
  const [output, setOutput] = useState<string>("");
  const [level, setLevel] = useState<string>("-");
  const [port, setPort] = useState<string>("8080");
  const [stateMessage, setStateMessage] = useState("");
  
  const debug = () => new DebugCommand("localhost",+port);

  const reloadActualSource = () => {
    debug()
      .all(
        "level&input&output&inputBefore&processing&macros&userDefined&state&output"
      )
      .then((response) => {
        setInputBefore(response.data?.inputBefore);
        setMacro(response.data?.processing);
        setOutput(response.data?.output);
        setStateMessage(response.data?.state);
        setLevel(response.data?.level);
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
  };

  const step = () => debug().step().then(() => reloadActualSource());
  const stepInto = () => debug().stepInto().then(() => reloadActualSource());
  const stepOut = () => debug().stepOut().then(() => reloadActualSource());
  const run = () => debug().run().then(() => reloadActualSource());
  const quit = () => debug().quit().then(() => reloadActualSource());

  useEffect(reloadActualSource);

  return (
    <div className="App">
      <header className="App-header">
        <TitleBar message={stateMessage} />
        <Grid
          container
          direction="column"
          justify="space-around"
          alignItems="flex-start"
        >
          <Grid
            container
            spacing={0}
            direction="row"
            justify="space-around"
            alignItems="flex-start"
            className="App_GridContainer"
          >
            <Grid item>
              <PortInput
                onChangeHandler={(e) => {
                  setPort(""+e.target.value);
                  reloadActualSource();
                }}
                port={port}
              />
            </Grid>
            <Grid item>
              <Label message={"Level: " + level} />
            </Grid>
            <Grid item>
              <IconButton variant="contained" onClick={run}>
                <Run />
              </IconButton>
            </Grid>
            <Grid item>
              <IconButton variant="contained" onClick={step}>
                <Step />
              </IconButton>
            </Grid>
            <Grid item>
              <IconButton variant="contained" onClick={stepInto}>
                <StepInto />
              </IconButton>
            </Grid>
            <Grid item>
              <IconButton variant="contained" onClick={stepOut}>
                <StepOut />
              </IconButton>
            </Grid>
            <Grid item xs={2}></Grid>
            <Grid item>
              <IconButton variant="contained" onClick={quit} color="secondary">
                <Quit />
              </IconButton>
            </Grid>
          </Grid>

          <Grid
            container
            direction="column"
            justify="flex-start"
            alignItems="flex-start"
            spacing={0}
            className="App_GridContainer"
          >
            {"input"}
            <Input text={inputBefore} macro={macro} />
            {"output"}
            <Input text={output} />
          </Grid>
        </Grid>
      </header>
    </div>
  );
}

export default App;

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
import StateMessage from "./components/StateMessage";
import Label from "./components/Label";
import TitleBar from "./components/TitleBar";
import "./App.css";
import DebugCommand from "./utils/DebugCommand";

function App() {
  const [inputBefore, setInputBefore] = useState<string>("");
  const [macro, setMacro] = useState<string>("");
  const [output, setOutput] = useState<string>("");
  const [port, setPort] = useState<string>("8080");
  const [level, setLevel] = useState<string>("-");
  const [stateMessage, setStateMessage] = useState("");
  const [debug, setDebug] = useState<DebugCommand>(
    new DebugCommand("http://localhost:", +port)
  );

  const reloadActualSource = () => {
    debug
      .all(
        "level&input&output&inputBefore&processing&macros&userDefined&state&output"
      )
      .then((response) => {
        setInputBefore(response.data.inputBefore);
        setMacro(response.data.processing);
        setOutput(response.data.output);
        setStateMessage(response.data.state);
        setLevel(response.data.level.level);
      })
      .catch(() => setStateMessage("DISCONNECTED"));
  };

  const step = () => debug.step().then(() => reloadActualSource());
  const stepInto = () => debug.stepInto().then(() => reloadActualSource());
  const stepOut = () => debug.stepOut().then(() => reloadActualSource());

  useEffect(reloadActualSource);

  return (
    <div className="App">
      <header className="App-header">
        <TitleBar/>
        <Label message={level} />
        <StateMessage message={stateMessage} />
        <Grid
          container
          direction="row"
          justify="space-between"
          alignItems="flex-start"
        >
          <Grid
            item
            xs={2}
            direction="row"
            justify="flex-start"
            alignItems="flex-start"
            className="App-ConnectionLabel"
          >
            http://localhost:
            <PortInput
              port={port}
              onChangeHandler={(e) => {
                setPort(e.target.value);
                setDebug(
                  new DebugCommand("http://localhost:", +e.target.value)
                );
              }}
            />
          </Grid>

          <Grid
            item
            xs={12}
            direction="row"
            justify="flex-start"
            alignItems="flex-start"
            className="App_CommandButtons"
          >
            <IconButton variant="contained" onClick={debug.run}>
              <Run />
            </IconButton>
            {"  "}
            <IconButton variant="contained" onClick={step}>
              <Step />
            </IconButton>
            <IconButton variant="contained" onClick={stepInto}>
              <StepInto />
            </IconButton>
            <IconButton variant="contained" onClick={stepOut}>
              <StepOut />
            </IconButton>
            {"  "}
            <IconButton
              variant="contained"
              onClick={debug.quit}
              color="secondary"
            >
              <Quit />
            </IconButton>
          </Grid>

          <Grid
            container
            direction="column"
            justify="flex-start"
            alignItems="flex-start"
          >
            <Input text={inputBefore} macro={macro} />
            <Input text={output} />
          </Grid>
        </Grid>
      </header>
    </div>
  );
}

export default App;

import React, { useEffect, useState } from "react";
import Input from "./components/Input";
import PortInput from "./components/PortInput";
import CommandButton from "./components/CommandButton";
import StateMessage from "./components/StateMessage";
import "./App.css";
import DebugCommand from "./utils/DebugCommand";

function App() {
  const [inputBefore, setInputBefore] = useState<string>("");
  const [macro, setMacro] = useState<string>("");
  const [output, setOutput] = useState<string>("");
  const [port, setPort] = useState<string>("8080");
  const [debug, setDebug] = useState<DebugCommand>(
    new DebugCommand("http://localhost:", +port)
  );
  const [stateMessage, setStateMessage] = useState("");

  const reloadActualSource = () => {
    Promise.all([
      debug.inputBefore().then((response) => setInputBefore(response.data)),
      debug.processing().then((response) => setMacro(response.data)),
      debug.output().then((response) => setOutput(response.data)),
      debug.state().then((response) => setStateMessage(response.data)),
    ]).catch(() => setStateMessage("DISCONNECTED"));
  };

  const step = () => debug.step().then(() => reloadActualSource());
  const stepInto = () => debug.stepInto().then(() => reloadActualSource());

  useEffect(reloadActualSource);

  return (
    <div className="App">
      <header className="App-header">
        <StateMessage message={stateMessage} />
        http://localhost:
        <PortInput
          port={port}
          onChangeHandler={(e) => {
            setPort(e.target.value);
            setDebug(new DebugCommand("http://localhost:", +e.target.value));
          }}
        />
        <div>
        <CommandButton title="Run" className="run" onClickHandler={debug.run} />
        <CommandButton title="Step" className="step" onClickHandler={step} />
        <CommandButton title="StepInto" className="stepInto" onClickHandler={stepInto} />
        <CommandButton title="Quit" className="quit" onClickHandler={debug.quit} />
        </div>
        <Input text={inputBefore} macro={macro} />
        <p></p>
        <Input text={output} />
      </header>
    </div>
  );
}

export default App;

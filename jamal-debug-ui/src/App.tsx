import React, { useEffect, useState } from "react";
import Input from "./components/Input";
import CommandButton from "./components/CommandButton";
import "./App.css";
import axios from "axios";

function App() {
  const [inputBefore, setInputBefore] = useState<string>("");
  const [macro, setMacro] = useState<string>("");
  const [output, setOutput] = useState("");
  const dummyRun = () => {
    axios.post("http://localhost:8080/run").then(() => console.log("OK"));
  };
  const initDebuggerDisplay = () => {
    axios
      .get("http://localhost:8080/inputBefore")
      .then((response) => setInputBefore(response.data));
    axios
      .get("http://localhost:8080/processing")
      .then((response) => setMacro(response.data));
  };
  useEffect(initDebuggerDisplay);
  return (
    <div className="App">
      <header className="App-header">
        <CommandButton title="Run" onClickHandler={dummyRun} />
        <Input text={inputBefore} macro={macro} />
        <p></p>
        <Input text={output} />
        <a
          className="App-link"
          href="https://reactjs.org"
          target="_blank"
          rel="noopener noreferrer"
        >
          Learn React
        </a>
      </header>
    </div>
  );
}

export default App;

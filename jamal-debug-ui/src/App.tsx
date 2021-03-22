import React from 'react';
import logo from './logo.svg';
import Input from './components/Input';
import CommandButton from './components/CommandButton';
import './App.css';

function App() {
const dummyRun = () => {
        console.log("dummyRun executed");
        }
  return (
    <div className="App">
      <header className="App-header">
        <img src={logo} className="App-logo" alt="logo" />
        <CommandButton title="Run" onClickHandler={dummyRun}/>
          <Input
            text="alma van a fa alatt" macro="van a"
          />
          <p></p>
          <Input
                      text="alma van a fa alatt" macro=""
                    />
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

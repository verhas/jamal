import React, {FC} from "react";
import "./Input.css";
import showNewLine from "../utils/NewLineDisplay";
import {state} from '../utils/GlobalState';

type InputProps = {
    text: string;
    macro?: string;
};

const convertToJSX = (s: string, color: string) => {
    if (s.length === 0) {
        return <></>;
    }
    let lines = s.split("\n");
    return <>
        {lines.slice(0, -1).map(line => <span style={{color: color}}>{line}<br/></span>)}
        <span style={{color: color}}>{lines[lines.length - 1]}</span>
    </>;
};

const Input: FC<InputProps> = ({text, macro = ""}) => {
    const start = text.indexOf(macro);
    const end = start + macro.length;
    const startText = showNewLine(start === -1 ? text : text.substr(0, start));
    let middleText: string;
    if (state.stateMessage === "AFTER" && start === 0) {
        middleText = "";
    } else {
        middleText = showNewLine(start === -1 ? "" : macro);
    }
    const endText = showNewLine(start === -1 ? "" : text.substr(end));

    return (
        <div style={{overflow: "auto"}} className="Input_SourceCode">
            {convertToJSX(startText, "black")}
            {convertToJSX(middleText, "red")}
            {convertToJSX(endText, "black")}
        </div>
    );
};

export default Input;

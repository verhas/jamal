import React, {FC} from "react";
import Box from "@material-ui/core/Box";
import "./Input.css";
import showNewLine from "../utils/NewLineDisplay";

type InputProps = {
    text: string;
    macro?: string;
};

const convertToJSX = (s: string, color:string) => {
    if (s.length === 0) {
        return <></>;
    }
    let lines = s.split("\n");
    return <>
        {lines.slice(0, -1).map(line => <span style={{ color: color}}>{line}<br/></span>)}
        <span style={{ color: color}}>{lines[lines.length - 1]}</span>
    </>;
};

const Input: FC<InputProps> = ({text, macro = ""}) => {
    const start = text.indexOf(macro);
    const end = start + macro.length;
    const startText = showNewLine(start === -1 ? text : text.substr(0, start));
    const middleText = showNewLine(start === -1 ? "" : macro);
    const endText = showNewLine(start === -1 ? "" : text.substr(end));

    return (
        <Box overflow="auto" className="Input_SourceCode">
            {convertToJSX(startText,"black")}
            {convertToJSX(middleText,"red")}
            {convertToJSX(endText,"black")}
        </Box>
    );
};

export default Input;

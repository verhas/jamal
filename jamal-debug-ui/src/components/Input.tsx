import React, { FC } from "react";
import Box from "@material-ui/core/Box";
import "./Input.css";
import showNewLine from "../utils/NewLineDisplay";

type InputProps = {
  text: string;
  macro?: string;
};

const convertToJSX = (s: string) => {
  var jsx = <></>;
  if (s.length === 0) {
    return jsx;
  }
  var lines = s.split("\n");
  for (var i = 0; i < lines.length - 1; i++) {
    var line = lines[i];
    jsx = (
      <>
        {jsx}
        {line}
        <br />
      </>
    );
  }
  jsx = (
    <>
      {jsx}
      {lines[lines.length - 1]}
    </>
  );
  return jsx;
};

const Input: FC<InputProps> = ({ text, macro = "" }) => {
  const start = text.indexOf(macro);
  const end = start + macro.length;
  const startText = showNewLine(start === -1 ? text : text.substr(0, start));
  const middleText = showNewLine(start === -1 ? "" : macro);
  const endText = showNewLine(start === -1 ? "" : text.substr(end));

  return (
    <Box overflow="auto" className="Input_SourceCode">
      {convertToJSX(startText)}
      <div className="red">{convertToJSX(middleText)}</div>
      {convertToJSX(endText)}
    </Box>
  );
};

export default Input;

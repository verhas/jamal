import React, { FC } from "react";
import "./Input.css";

type InputProps = {
  text: string;
  macro?: string;
};

const showNewLine = (s:string) => s.replaceAll("\n","\u00b6\n");

const Input: FC<InputProps> = ({ text, macro = "" }) => {
  const start = text.indexOf(macro);
  const end = start + macro.length;
  const startText = showNewLine(start === -1 ? text : text.substr(0, start));
  const middleText = showNewLine(start === -1 ? "" : macro);
  const endText = showNewLine(start === -1 ? "" : text.substr(end));
  return (
    <pre className="Input_SourceCode">
      <span>{startText}</span>
      <span className="red">{middleText}</span>
      <span>{endText}</span>
    </pre>
  );
};

export default Input;

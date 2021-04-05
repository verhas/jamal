import React, { FC } from "react";
import "./Input.css";

type SimpleTextOutputProps = {
  text: string;
};

const showNewLine = (s: string) => s.replaceAll("\n", "\u00b6\n");

const SimpleTextOutput: FC<SimpleTextOutputProps> = ({ text }) => {
  console.log("SimpleTextOutput.text");
  console.log(text);
  const textConverted = showNewLine(""+text);
  return (
    <pre className="Input_SourceCode" style={{ minHeight: "100px" }}>
      <span>{textConverted}</span>
    </pre>
  );
};

export default SimpleTextOutput;

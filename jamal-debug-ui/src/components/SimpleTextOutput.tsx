import React, { FC } from "react";
import "./SimpleText.css";

type SimpleTextOutputProps = {
  text: string;
};

const showNewLine = (s: string) => s.replaceAll("\n", "\u00b6\n");

const SimpleTextOutput: FC<SimpleTextOutputProps> = ({ text }) => {
  const textConverted = showNewLine(""+text);
  return (
    <pre className="SimpleTextOutput_Pre">
      <span>{textConverted}</span>
    </pre>
  );
};

export default SimpleTextOutput;

import React, { FC } from "react";
import "./SimpleText.css";

type SimpleTextOutputProps = {
  children: string;
};

const showNewLine = (s: string) => s.replaceAll("\n", "\u00b6\n");

const SimpleTextOutput: FC<SimpleTextOutputProps> = ({ children }) => {
  console.log("SimpleTextOutput = " + children);
  const textConverted = showNewLine("" + children);
  console.log("SimpleTextOutput = " + textConverted);
  return (
    <div className="SimpleTextInput_Caption">
      {"result"}
      <textarea
        readOnly
        className="SimpleTextInput_TextArea"
        value={textConverted}
      />
    </div>
  );
};

export default SimpleTextOutput;

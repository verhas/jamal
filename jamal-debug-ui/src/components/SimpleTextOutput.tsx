import React, { FC } from "react";
import "./SimpleText.css";

type SimpleTextOutputProps = {
  children: string;
  caption: string;
};

const showNewLine = (s: string) => s.replaceAll("\n", "\u00b6\n");

const SimpleTextOutput: FC<SimpleTextOutputProps> = ({ children, caption }) => {
  const textConverted = showNewLine("" + children);
  return (
    <div className="SimpleTextInput_Caption">
      {caption}
      <textarea
        readOnly
        className="SimpleTextInput_TextArea"
        value={textConverted}
      />
    </div>
  );
};

export default SimpleTextOutput;

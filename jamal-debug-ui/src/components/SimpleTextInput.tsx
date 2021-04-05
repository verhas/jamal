import React, { FC } from "react";
import "./Input.css";

type SimpleTextInputProps = {
  text: string;
  onChangeHandler : (e:any) => void;
};

const showNewLine = (s: string) => s.replaceAll("\n", "\u00b6\n");

const SimpleTextInput: FC<SimpleTextInputProps> = ({ text , onChangeHandler }) => {
  const textConverted = showNewLine(text);
  return (
    <textarea className="Input_SourceCode" style={{ maxWidth: "90%", minHeight: "100px", fontSize: "10pt" }} onChange={onChangeHandler}>
      {textConverted}
    </textarea>
  );
};

export default SimpleTextInput;

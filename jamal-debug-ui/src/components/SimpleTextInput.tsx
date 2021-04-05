import React, { FC } from "react";
import "./SimpleText.css";

type SimpleTextInputProps = {
  text: string;
  caption: string;
  onChangeHandler: (e: any) => void;
};

const showNewLine = (s: string) => s.replaceAll("\n", "\u00b6\n");

const SimpleTextInput: FC<SimpleTextInputProps> = ({
  text,caption,
  onChangeHandler,
}) => {
  const textConverted = showNewLine("" + text);
  return (
    <div  className="SimpleTextInput_Caption" >
      {caption}
      <textarea className="SimpleTextInput_TextArea" onChange={onChangeHandler}>
        {textConverted}
      </textarea>
    </div>
  );
};

export default SimpleTextInput;

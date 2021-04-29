import React, { FC } from "react";
import "./SimpleText.css";

type SimpleTextInputProps = {
  children: string;
  caption: string;
  reference: any;
};

const SimpleTextInput: FC<SimpleTextInputProps> = ({
  children,
  caption,
  reference,
}) => {
  return (
    <div className="SimpleTextInput_Caption">
      <div className="textbox_caption">{caption}</div>
      <textarea className="SimpleTextInput_TextArea" ref={reference}>
        {children}
      </textarea>
    </div>
  );
};

export default SimpleTextInput;

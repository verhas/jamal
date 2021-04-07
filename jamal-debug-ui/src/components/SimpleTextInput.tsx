import React, { FC } from "react";
import "./SimpleText.css";

type SimpleTextInputProps = {
  children: string;
  caption: string;
  onBlurHandler: (e: any) => void;
};

const SimpleTextInput: FC<SimpleTextInputProps> = ({
  children,
  caption,
  onBlurHandler,
}) => {
  return (
    <div className="SimpleTextInput_Caption">
      {caption}
      <textarea className="SimpleTextInput_TextArea" onBlur={onBlurHandler}>
        {children}
      </textarea>
    </div>
  );
};

export default SimpleTextInput;

import React, { FC } from "react";
import "./Label.css";
import {state} from "../StateHandler"

type LabelProps = {
  message: string;
};

const Label: FC<LabelProps> = ({ message }) => {
  let errorClass = "";
  if( state.errors.length ){
    errorClass = "Label_When_Errors"
  }
  return <div className={`Label_Label ${errorClass}`}>{message}</div>;
};

export default Label;

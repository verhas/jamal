import React, { FC } from "react";
import "./Label.css";

type LabelProps = {
  message: string;
};

const Label: FC<LabelProps> = ({ message }) => {
  return <div className="Label_Label"><span>{message}</span></div>;
};

export default Label;

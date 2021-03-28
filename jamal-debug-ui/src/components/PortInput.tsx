import React, { FC } from "react";
import "./PortInput.css";

type InputProps = {
  port: string;
  onChangeHandler : (e:any) => void;
};


const PortInput: FC<InputProps> = ({ onChangeHandler, port }) => {
  return (
      <input type="text" value={port} onChange={onChangeHandler}/>
  );
};

export default PortInput;

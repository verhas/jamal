import React, { FC } from "react";
import "./PortInput.css";

type InputProps = {
  port: string;
  onChangeHandler : (e:any) => void;
};


const PortInput: FC<InputProps> = ({ onChangeHandler, port }) => {
  return (
      <span>http://localhost:<input type="text" value={port} onChange={onChangeHandler}/></span>
  );
};

export default PortInput;

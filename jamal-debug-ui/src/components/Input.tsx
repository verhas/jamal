import React, {FC} from 'react';
import './Input.css';

type InputProps={
    text: string;
    macro: string;
}

const Input:FC<InputProps> = ( {text,macro} ) => {
  const start = text.indexOf(macro);
  const end = start + macro.length;
  const startText = start === -1 ? text : text.substr(0,start) ;
  const middleText = start === -1 ? "" : macro ;
  const endText = start === -1 ? "" : text.substr(end);
  return (
  <>
    <span>{startText}</span><span className="red">{middleText}</span><span>{endText}</span>
  </>
  );
}

export default Input;

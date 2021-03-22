import React, {FC} from 'react';
import './CommandButton.css';

type CommandButtonProps = {
    title: string;
    onClickHandler : () => void;
}

const CommandButton:FC<CommandButtonProps> = ( {title, onClickHandler} ) => {
  return (
  <>
    <button onClick={onClickHandler}>{title}</button>
  </>
  );
}

export default CommandButton;

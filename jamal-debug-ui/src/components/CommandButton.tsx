import React, { FC } from "react";
import "./CommandButton.css";

type CommandButtonProps = {
  title: string;
  onClickHandler: () => void;
  className?: string;
};

const CommandButton: FC<CommandButtonProps> = ({
  title,
  onClickHandler,
  className,
}) => {
  return (
    <div className="block">
      <button className={className} onClick={onClickHandler}>
      </button>
      <span className="CommandButton_buttonName">{title}</span>
    </div>
  );
};

export default CommandButton;

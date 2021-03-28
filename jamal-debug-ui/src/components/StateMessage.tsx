import React, { FC, useMemo } from "react";
import "./StateMessage.css";
import disconnect from "../icons/wifi-off.svg";
import before from "../icons/chevron-right.svg";
import after from "../icons/chevrons-right.svg";
import unknownStatus from "../icons/help-circle.svg";

type StateMessageProps = {
  message: string;
};

const StateMessage: FC<StateMessageProps> = ({ message }) => {
  const icon = useMemo(() => {
    switch (message) {
      case "BEFORE":
        return before;
      case "AFTER":
        return after;
      case "DISCONNECTED":
        return disconnect;
      default:
        return unknownStatus;
    }
  }, [message]);

  return (
    <div className="statusIcon">
      <img src={icon} alt={message} />
    </div>
  );
};

export default StateMessage;

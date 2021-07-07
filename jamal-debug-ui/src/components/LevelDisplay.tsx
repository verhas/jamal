import {FC} from "react";
import {state} from "../utils/GlobalState"
import "./LevelDisplay.css";

const LevelDisplay: FC = () => {
    let errorClass = "";
    if( state.errors.length ){
        errorClass = "Label_When_Errors"
    }
    return <div className={`Label_Label ${errorClass}`}>{"" + state.level}</div>;
}

export default LevelDisplay;
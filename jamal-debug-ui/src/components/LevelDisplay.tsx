import {FC} from "react";
import {state} from "../utils/GlobalState"
import "./LevelDisplay.css";

const LevelDisplay: FC = () => {
    let errorClass = "";
    if (state.errors.length) {
        errorClass = "RedAsError"
    }
    return <>
        <div style={{

            display: "flex",
            flexDirection: "row",
            justifyContent: "left"
        }}>
            <div className={`LevelNumber ${errorClass}`}>{"" + state.displayedLevel}</div>
            <div>
                <div className={'LevelNumber LevelUpDown LevelUp'} onClick={() => {
                    if (state.displayedLevel < state.level) {
                        state.setDisplayedLevel(state.displayedLevel + 1);
                        state.setInputBefore(state.inputBeforeArray[state.displayedLevel] || '');
                        state.setOutput(state.outputArray[state.displayedLevel] || '');
                    }
                }}><div className={"LevelUpDownLabel LevelUpLabel"}>+</div>
                </div>
                <div className={'LevelNumber LevelUpDown'} onClick={() => {
                    if (state.displayedLevel > 1) {
                        state.setDisplayedLevel(state.displayedLevel - 1);
                        state.setInputBefore(state.inputBeforeArray[state.displayedLevel - 2] || '');
                        state.setOutput(state.outputArray[state.displayedLevel - 2] || '');
                    }
                }}><div className={"LevelUpDownLabel"}>-</div>
                </div>
            </div>
        </div>
        <div style={{fontSize: "8pt"}}>source level</div>
    </>;
}

export default LevelDisplay;
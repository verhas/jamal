import React, {FC} from "react";
import "./LevelDisplay.css";
import {Button as SemanticButton} from "semantic-ui-react";
import {state} from "../utils/GlobalState";
import {DISCONNECTED, RUN} from "../Constants";

type ButtonProps = {
    onClick: any;
    caption: string;
    color?: any;
    disabled?: boolean;
    style?: any;
    children: any;
};

const Button: FC<ButtonProps> = ({onClick, caption, color = "grey", disabled = false, children, style = {}}) => {
    return (
        <>
            <SemanticButton variant="contained" onClick={onClick} color={color}
                            disabled={disabled || state.stateMessage === RUN || state.stateMessage === DISCONNECTED}
                            style={style}>
                {children}
                <div style={{fontSize: "8pt"}}>{caption}</div>
            </SemanticButton>
        </>
    );
};

export default Button;

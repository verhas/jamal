import React, {FC} from "react";
import "./LevelDisplay.css";
import {Button as SemanticButton} from "semantic-ui-react";

type ButtonProps = {
    onClick: any;
    caption: string;
    color?: any;
    disabled?: boolean;
};

const Button: FC<ButtonProps> = ({onClick, caption, color = "grey", disabled = false, children}) => {
    return (
        <>
            <SemanticButton variant="contained" onClick={onClick} color={color} disabled={disabled}>
                {children}
                <div style={{fontSize: "8pt"}}>{caption}</div>
            </SemanticButton>
        </>
    );
};

export default Button;

import React, {FC} from "react";
import "./LevelDisplay.css";
import {Grid} from "@material-ui/core";
import {Button as MaterialButton} from "@material-ui/core";

type ButtonProps = {
    onClick: any;
    caption: string;
    color?: any;
    disabled?: boolean;
};

const Button: FC<ButtonProps> = ({onClick, caption, color = "default", disabled = false, children}) => {
    const m = (21 - caption.length) + "px";
    return (
        <Grid item>
            <MaterialButton variant="contained" onClick={onClick} color={color} disabled={disabled}>
                {children}
            </MaterialButton>
            {disabled ? <></> : <div style={{marginLeft: m, fontSize: "8pt"}} >{caption}</div>}
        </Grid>
    );
};

export default Button;

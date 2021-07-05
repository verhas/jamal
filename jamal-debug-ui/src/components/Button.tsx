import React, {FC} from "react";
import "./Label.css";
import {Grid, PropTypes} from "@material-ui/core";
import {Button as MaterialButton} from "@material-ui/core";

type ButtonProps = {
    onClick: any,
    caption: string,
    color?: any
};

const Button: FC<ButtonProps> = ({onClick, caption, color = "default", children}) => {
    const m = (21 - caption.length) + "px";
    return (
        <Grid item>
            <MaterialButton variant="contained" onClick={onClick} color={color}>
                {children}
            </MaterialButton>
            <div style={{marginLeft: m, fontSize: "8pt"}}>{caption}</div>
        </Grid>
    );
};

export default Button;

import React, {FC} from "react";
import "./SimpleText.css";
import showNewLine from "../utils/NewLineDisplay";

type SimpleTextOutputProps = {
    children: string;
    caption: string;
    color?: string;
    height?: number
};

const SimpleTextOutput: FC<SimpleTextOutputProps> = ({children, caption, color = "white", height = 260}) => {
    const textConverted = showNewLine("" + children);
    return (
        <div className="SimpleTextInput_Caption" style={{backgroundColor: color}}>
            <div className="textbox_caption" style={{backgroundColor: color}}>{caption}</div>
            <textarea
                readOnly
                className="SimpleTextInput_TextArea"
                value={textConverted}
                style={{width: "99%", backgroundColor: color, height}}
            />
        </div>
    );
};

export default SimpleTextOutput;

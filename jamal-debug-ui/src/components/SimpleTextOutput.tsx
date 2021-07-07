import React, {FC} from "react";
import "./SimpleText.css";
import showNewLine from "../utils/NewLineDisplay";

type SimpleTextOutputProps = {
    children: string;
    caption: string;
};

const SimpleTextOutput: FC<SimpleTextOutputProps> = ({children, caption}) => {
    const textConverted = showNewLine("" + children);
    return (
        <div className="SimpleTextInput_Caption">
            <div className="textbox_caption">{caption}</div>
            <textarea
                readOnly
                className="SimpleTextInput_TextArea"
                value={textConverted}
                style={{width: "99%"}}
            />
        </div>
    );
};

export default SimpleTextOutput;

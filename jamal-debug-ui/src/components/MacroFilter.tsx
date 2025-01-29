import React, {ChangeEvent, FC} from "react";
import "./MacroFilter.css";
import 'semantic-ui-css/semantic.min.css';
import Filter from "@material-ui/icons/Filter";


type MacroFilterProps = {
    filter: string;
    id: string;
    onFilterChange: (newFilter: string) => void;
};

const MacroFilter: FC<MacroFilterProps> = ({filter, id, onFilterChange}) => {
    const handleInputChange = (event: ChangeEvent<HTMLInputElement>) => {
        onFilterChange(event.target.value);
    };
    return (<>
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<Filter/>&nbsp;
            <input type="text" className="FilterInput"
                   id={id}
                   defaultValue=".*" value={filter} onChange={handleInputChange}/>
            <span className={"Label"}>&nbsp;&nbsp;&nbsp;regex filter</span>
        </>
    );
};

export default MacroFilter;

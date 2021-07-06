import {FC} from "react";
import {state} from "../StateHandler"
import Grid from "@material-ui/core/Grid";
import Label from "./Label";

const LevelDisplay: FC = () => {
    return <Grid item>
        <Label message={"" + state.level}/>
    </Grid>;
}

export default LevelDisplay;
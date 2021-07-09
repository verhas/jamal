import React, {FC, useMemo} from "react";
import Toolbar from "@material-ui/core/Toolbar";
import Typography from "@material-ui/core/Typography";
import Grid from "@material-ui/core/Grid";
import Disconnect from "@material-ui/icons/WifiOff";
import Before from "@material-ui/icons/PauseCircleOutline";
import After from "@material-ui/icons/CheckCircle";
import Run from "@material-ui/icons/DirectionsRun";
import UnknownStatus from "@material-ui/icons/TextRotationNone";
import "./TitleBar.css";

type TitleBarProps = {
  message: string;
};

const TitleBar: FC<TitleBarProps> = ({ message }) => {
  const icon = useMemo(() => {
    switch (message) {
      case "BEFORE":
        return <Before />;
      case "AFTER":
        return <After />;
      case "DISCONNECTED":
        return <Disconnect />;
      case "RUN":
        return <Run />;
      default:
        return <UnknownStatus />;
    }
  }, [message]);

  return (
    <Toolbar className="TitleBar">
      <Grid
        container
        direction="row"
        alignItems="flex-start"
        justify="space-between"
      >
        <Grid item>
          &nbsp;
        </Grid>
        <Grid item>
          <Typography variant="h6" className="title">
            Jamal Debug
          </Typography>
        </Grid>
        <Grid item><span style={{fontSize: "8pt"}}>{message}&nbsp;</span>{icon}</Grid>
      </Grid>
    </Toolbar>
  );
};

export default TitleBar;

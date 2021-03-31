import React, { FC, useMemo } from "react";
import AppBar from "@material-ui/core/AppBar";
import Toolbar from "@material-ui/core/Toolbar";
import Typography from "@material-ui/core/Typography";
import IconButton from "@material-ui/core/IconButton";
import Grid from "@material-ui/core/Grid";
import MenuIcon from "@material-ui/icons/Menu";
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
    <div className="root">
      <AppBar position="fixed">
        <Toolbar>
          <Grid
            container
            direction="row"
            alignItems="flex-start"
            justify="space-between"
          >
              <MenuIcon />
            <Typography variant="h6" className="title">
              Jamal Debug
            </Typography>
            {icon}
          </Grid>
        </Toolbar>
      </AppBar>
    </div>
  );
};

export default TitleBar;

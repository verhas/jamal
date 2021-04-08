import React, { FC } from "react";
import { DataGrid, GridRowSelectedParams } from "@material-ui/data-grid";
import "./UserDefinedMacrosDisplay.css";

type UserDefinedMacrosDisplayProps = {
  data: any;
  captionSetter: (caption: string) => void;
  contentSetter: (caption: string) => void;
};

const UserDefinedMacrosDisplay: FC<UserDefinedMacrosDisplayProps> = ({
  data,captionSetter,contentSetter
}) => {
  const columns = [
    { field: "id", headerName: "n", width: 10 },
    { field: "level", headerName: "Level", width: 25 },
    { field: "name", headerName: "macro", width: 100 },
    { field: "params", headerName: "params", width: 100 },
    { field: "content", headerName: "content" },
  ];

  const rows = [];

  var i: number = 0;
  var j: number = 0;
  for (var macros of data?.userDefined?.scopes || []) {
    i++;
    for (var macro of macros || []) {
      j++;
      rows.push({
        id: j,
        level: i,
        name: macro.id,
        params: macro.parameters.join(","),
        content: macro.content,
      });
    }
  }
  return (
    <div style={{ height: "310px", width: "100%", marginTop: "10px" }}>
      <DataGrid
        className="UserDefinedMacrosDisplay"
        headerHeight={33}
        rowHeight={33}
        rows={rows}
        columns={columns}
        density="compact"
        pageSize={rows.length}
        hideFooter={true}
        onRowSelected={(row: GridRowSelectedParams) => {
          const text =
            "{@define " +
            row.data.name +
            "(" +
            row.data.params +
            ")=" +
            row.data.content +
            "}";
            captionSetter("macro definition");
            contentSetter(text);
        }}
      />
    </div>
  );
};

export default UserDefinedMacrosDisplay;

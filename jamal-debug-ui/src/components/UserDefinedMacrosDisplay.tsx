import React, { FC } from "react";
import { DataGrid } from "@material-ui/data-grid";
import "./UserDefinedMacrosDisplay.css"

type UserDefinedMacrosDisplayProps = {
  data: any;
};

const UserDefinedMacrosDisplay: FC<UserDefinedMacrosDisplayProps> = ({
  data,
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
    console.log(macros);
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
    <div style={{ height: 400, width: "100%" }}>
      <DataGrid
        className="UserDefinedMacrosDisplay"
        headerHeight={33}
        rowHeight={33}
        rows={rows}
        columns={columns}
        pageSize={5}
        density="compact"
        autoPageSize={true}
      />
    </div>
  );
};

export default UserDefinedMacrosDisplay;

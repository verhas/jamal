import React, { FC } from "react";
import { DataGrid } from "@material-ui/data-grid";
import "./BuiltInMacrosDisplay.css";

type BuiltInMacrosDisplayProps = {
  data: any;
};

const BuiltInMacrosDisplay: FC<BuiltInMacrosDisplayProps> = ({ data }) => {
  const columns = [
    { field: "id", headerName: "n", width: 10 },
    { field: "level", headerName: "L", width: 25 },
    { field: "name", headerName: "macro", width: 100 },
  ];

  const rows = [];

  var i: number = 0;
  var j: number = 0;
  for (var macros of data?.macros?.macros || []) {
    i++;
    for (var macro of macros?.macros || []) {
      j++;
      rows.push({ id: j, level: i, name: macro });
    }
  }
  return (
    <div style={{ height: "310px", width: "100%", marginTop: "10px" }}>
      <DataGrid
        className="BuiltInMacrosDisplay"
        headerHeight={33}
        rowHeight={33}
        rows={rows}
        columns={columns}
        density="compact"
        pageSize={rows.length}
        hideFooter={true}
      />
    </div>
  );
};

export default BuiltInMacrosDisplay;
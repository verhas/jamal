import React, { FC } from "react";

type TabPanelProps = {
  children: any;
  hidden: boolean;
  id: string;
  other: any;
};

const TabPanel: FC<TabPanelProps> = ({ children, hidden, id, other="" }) => {
  return (
    <div role="tabpanel" hidden={hidden} id={id} {...other}>
      {children}
    </div>
  );
};
export default TabPanel;

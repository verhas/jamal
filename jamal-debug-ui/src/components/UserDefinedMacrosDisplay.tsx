import React, {FC} from "react";
import "./UserDefinedMacrosDisplay.css";
import {Table} from 'semantic-ui-react'
import 'semantic-ui-css/semantic.min.css';

type UserDefinedMacrosDisplayProps = {
    data: any;
    captionSetter: (caption: string) => void;
    contentSetter: (caption: string) => void;
};

const UserDefinedMacrosDisplay: FC<UserDefinedMacrosDisplayProps> = ({
                                                                         data, captionSetter, contentSetter
                                                                     }) => {

    const rows: Array<Record<string, any>> = [];

    for (let macros of data?.userDefined?.scopes || []) {
        for (let macro of macros || []) {
            rows.push({
                name: macro.id,
                params: macro?.parameters?.join(",") ?? "",
                content: macro.content,
            });
        }
    }

    let j = 0;
    let i = 0;
    return (
        <div style={{height: "310px", width: "100%", marginTop: "10px", overflowY: "auto", backgroundColor: "#d2eaff"}}>
            <Table celled size="small" sortable striped style={{fontSize: "12px", backgroundColor: "#d2eaff"}}>
                <Table.Header>
                    <Table.Row key={0}>
                        <Table.HeaderCell>n</Table.HeaderCell>
                        <Table.HeaderCell>L</Table.HeaderCell>
                        <Table.HeaderCell>macro</Table.HeaderCell>
                        <Table.HeaderCell>parameters</Table.HeaderCell>
                        <Table.HeaderCell>body</Table.HeaderCell>
                    </Table.Row>
                </Table.Header>
                {(data?.userDefined?.scopes || []).map((macros: Record<string, any>) => {
                        i++;
                        return macros.map((macro: Record<string, any>) => {
                                j++;
                                return <Table.Row key={j} onClick={((rowNr: number) => () => {
                                    const row = rows[rowNr];
                                    const text = "{@define " + row.name + "(" + row.params + ")=" + row.content + "}";
                                    captionSetter("macro definition");
                                    contentSetter(text);
                                })(j - 1)}>
                                    <Table.Cell style={{width: 30}}>{j}</Table.Cell>
                                    <Table.Cell style={{width: 30}}>{i}</Table.Cell>
                                    <Table.Cell style={{width: 100}}>{macro.id}</Table.Cell>
                                    <Table.Cell style={{
                                        width: 200,
                                        overflowX: "auto"
                                    }}>{macro?.parameters?.join(",") ?? ""}</Table.Cell>
                                    <Table.Cell style={{width: "100%"}}>{macro.content}</Table.Cell>
                                </Table.Row>;
                            }
                        );
                    }
                )}
            </Table>
        </div>
    );
};

export default UserDefinedMacrosDisplay;

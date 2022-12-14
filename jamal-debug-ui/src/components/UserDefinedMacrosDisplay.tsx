import React, {FC} from "react";
import "./UserDefinedMacrosDisplay.css";
import {Table} from 'semantic-ui-react'
import 'semantic-ui-css/semantic.min.css';
import type Data from '../server/Data';
import type { UserDefinedMacro } from '../server/Data';

type UserDefinedMacrosDisplayProps = {
    data: Data;
    captionSetter: (caption: string) => void;
    contentSetter: (caption: string) => void;
};

type Macro = {
    name: string;
    params: string;
    content?: string
}

const UserDefinedMacrosDisplay: FC<UserDefinedMacrosDisplayProps> = ({
                                                                         data, captionSetter, contentSetter
                                                                     }) => {

    const rows: Macro[] = [];

    for (let macros of data.userDefined?.scopes || []) {
        for (let macro of macros || []) {
            rows.push({
                name: macro.id,
                params: macro.parameters?.join(",") ?? "",
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
                <Table.Body>
                    {(data?.userDefined?.scopes || []).map((macros: { [key: string]: any }) => {
                            i++;
                            return macros.map((macro: UserDefinedMacro) => {
                                    j++;
                                    return <Table.Row key={j} onClick={((rowNr: number) => () => {
                                        const row = rows[rowNr];
                                        const text = "{@define " + row.name + "(" + row.params + ")=" + row.content + "}";
                                        captionSetter("macro definition");
                                        contentSetter(text);
                                    })(j - 1)} warning={macro.content === undefined}>
                                        <Table.Cell style={{width: 30}}>{j}</Table.Cell>
                                        <Table.Cell style={{width: 30}}>{i}</Table.Cell>
                                        <Table.Cell style={{width: 100}}>{macro.id}</Table.Cell>
                                        <Table.Cell style={{
                                            width: 200,
                                            overflowX: "auto"
                                        }}>{macro?.parameters?.join(",") ?? ""}</Table.Cell>
                                        <Table.Cell style={{width: "100%"}}>{macro.content !== undefined ? macro.content : macro.type}</Table.Cell>
                                    </Table.Row>;
                                }
                            );
                        }
                    )}
                </Table.Body>
            </Table>
        </div>
    );
};

export default UserDefinedMacrosDisplay;

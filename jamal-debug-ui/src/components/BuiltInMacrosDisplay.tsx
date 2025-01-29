import React, {FC} from "react";
import "./BuiltInMacrosDisplay.css";
import {Table} from 'semantic-ui-react'
import 'semantic-ui-css/semantic.min.css';
import type Data from '../server/Data'

type BuiltInMacrosDisplayProps = {
    data: Data,
    filter: string
};

const BuiltInMacrosDisplay: FC<BuiltInMacrosDisplayProps> = ({data, filter}) => {

    let j = 0;
    let i = 0;
    const rx = filter.length === 0 ? new RegExp(".*", "") : new RegExp(filter, "i");
    return (
        <div style={{height: "310px"}}>

            <div style={{
                height: "620px",
                width: "100%",
                marginTop: "10px",
                overflowY: "auto",
                backgroundColor: "lightyellow"
            }}>
                <Table celled size="small" sortable striped
                       style={{fontSize: "12px", backgroundColor: "lightyellow"}}>
                    <Table.Header>
                        <Table.Row key={0}>
                            <Table.HeaderCell style={{width: "30px"}}>n</Table.HeaderCell>
                            <Table.HeaderCell style={{width: "30px"}}>L</Table.HeaderCell>
                            <Table.HeaderCell style={{width: "100px"}}>macro</Table.HeaderCell>
                        </Table.Row>
                    </Table.Header>
                    <Table.Body>
                        {(data?.macros?.macros || [])
                            .map((macros: any) => {
                                    i++;
                                    return macros.macros
                                        .filter((macro: string): boolean => rx.test(macro))
                                        .map((macro: string) => {
                                                j++;
                                                return <Table.Row key={j}>
                                                    <Table.Cell style={{width: 30}}>{j}</Table.Cell>
                                                    <Table.Cell style={{width: 30}}>{i}</Table.Cell>
                                                    <Table.Cell style={{width: "100%"}}>{macro}</Table.Cell>
                                                </Table.Row>;
                                            }
                                        );
                                }
                            )}
                    </Table.Body>
                </Table>
            </div>
        </div>
    );
};

export default BuiltInMacrosDisplay;

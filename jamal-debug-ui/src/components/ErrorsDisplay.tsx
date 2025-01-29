import React, {FC} from "react";
import "./BuiltInMacrosDisplay.css";
import {Table} from 'semantic-ui-react';
import {state} from '../utils/GlobalState';
import 'semantic-ui-css/semantic.min.css';

type ErrorsDisplayProps = {};

const ErrorsDisplay: FC<ErrorsDisplayProps> = () => {

    const errors = state.errors;
    let j = 0;
    return (
        <div style={{height: "310px"}}>

            <div style={{
                height: "620px",
                width: "100%",
                marginTop: "10px",
                overflowY: "auto",
                backgroundColor: "whitesmoke"
            }}>
                <Table celled size="small" sortable striped
                       style={{fontSize: "12px", backgroundColor: "whitesmoke"}}>
                    <Table.Header>
                        <Table.Row key={0}>
                            <Table.HeaderCell style={{width: "100px"}}>Error Message</Table.HeaderCell>
                        </Table.Row>
                    </Table.Header>
                    <Table.Body>
                        {errors.map((error: { [key: string]: any }) => {
                            j++;
                            return <Table.Row key={j} >
                                <Table.Cell style={{width: "100%",}} warning verticalAlign="top">{errors}</Table.Cell>
                            </Table.Row>;
                        })}
                    </Table.Body>
                </Table>
            </div>
        </div>
    );
};

export default ErrorsDisplay;

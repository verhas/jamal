import {useState} from "react";

/**
 * Using this function you can manage the state variables. Upon calling you have to provide a record with the keys and
 * the initial values. The return value is a record that contains all the state variables as fields and also all
 * setXXX functions.
 *
 * @param init the record with the initial values
 */

export let state: { [x: string]: any } = {};

const initState = (init: { [key: string]: any }) => {
    const loopUseState = useState; // or else the linter screams and fails
    for (let key in init) {
        [state[key], state["set" + key.charAt(0).toUpperCase() + key.slice(1)]] = loopUseState(init[key]);
    }
    return state;
}


export default initState;
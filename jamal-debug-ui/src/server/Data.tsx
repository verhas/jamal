export type UserDefinedMacro = {
    content: string,
    open: string,
    close: string,
    parameters: [string],
    id: string,
    type: string
};

type Data = {
    output: string,
    input: string,
    inputBefore: string,
    macros: {
        macros: [{
            macros: [string],
            delimiters: { open: string, close: string }
        }]
    },
    level: string,
    userDefined: { scopes: [[UserDefinedMacro]] },

    processing: string,
    state: string,
    version: {
        version: string
    },
    errors: [string]
};

export default Data;
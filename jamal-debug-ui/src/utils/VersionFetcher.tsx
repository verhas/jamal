import axios from "axios";
import packageJson from "../../package.json";
import {state} from "./GlobalState"
/**
 * This singleton fetches the version string from GitHub. This way the application can how in the footer the
 * available latest version of the application. The latest version is what appears in the file version.jim
 * declaring the macro LATEST_VERSION.
 */
class VersionFetcher {
    url: string =
        "https://raw.githubusercontent.com/verhas/jamal/master/version.jim";
    lastRelease: string = "";

    constructor() {
        axios.get(this.url).then((response) => {
            for (let s of ("" + response.data).split("\n")) {
                if (s.search(/{@define\sLAST_RELEASE=(.*)}/) === 0) {
                    this.lastRelease = s.substring(22, s.length - 1);
                }
            }
        });
    }
}

/**
 * A singleton instance. When created it fetches the version string and stores in the `lastRelease` property.
 */
const versionFetcher = new VersionFetcher();

/**
 * Calculate the version message for the footer. This message displays the version of the debugger UI, the version of
 * the server it is connecting to and also the latest version.
 */
const getVersionMessage = (): string => {
    let versionMessage;
    if (state.serverVersion === packageJson.version) {
        versionMessage = "Version: " + state.serverVersion;
    } else {
        versionMessage =
            "Server version: " +
            state.serverVersion +
            ", Client version: " +
            packageJson.version;
    }

    if (
        versionFetcher.lastRelease !== "" &&
        (versionFetcher.lastRelease !== state.serverVersion || versionFetcher.lastRelease !== packageJson.version)
    ) {
        versionMessage += ", Latest release: " + versionFetcher.lastRelease;
    }
    return versionMessage;
}

export default getVersionMessage;

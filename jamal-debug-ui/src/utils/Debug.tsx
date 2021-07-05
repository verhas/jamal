import axios from "axios";
import queryString from "querystring";

/**
 * Implement communication with the Jamal debug server via HTTP GET and POST requests.
 * The class has methods for each command in addition to post() and get().
 */
class Debug {
  port: number = 8080;
  host: string = "localhost";

  /**
   * The constructor should specify the default host and post number.
   * Later the host and the port number can be changed.
   *
   * @param {string} host
   * @param {string} port
   * @constructor
   */
  constructor(host: string, port: number = 8080) {
    this.host = host; 
    this.port = port;
  }

  connection= (): string => {
    return "http://" + this.host + ":" + this.port;
  }

  post = (command: string, data?: string) => {
    return axios.post(this.connection() + command, data);
  }
  
  get = (command: string) => {
    return axios.get(this.connection() + command);
  }

  run = (breakpoints:string) => this.post("/run",breakpoints);
  step = () => this.post("/step");
  stepInto = () => this.post("/stepInto");
  stepOut = () => this.post("/stepOut");
  quit = () => this.post("/quit");
  execute = (data: string) => this.post("/execute", data);

  level = () => this.get("/level");
  state = () => this.get("/state");
  input = () => this.get("/input");
  inputBefore = () => this.get("/inputBefore");
  output = () => this.get("/output");
  processing = () => this.get("/processing");
  macros = () => this.get("/macros");
  userDefinedMacros = () => this.get("/userDefinedMacros");
  all = (queryParams: string) => this.get("/all?" + queryParams);
}

const debug = new Debug("localhost", 8080);

const qs = queryString.parse(window.location.search.substring(1));

const port: string = qs.port
    ? "" + qs.port
    : new URL(window.location.href).port;

debug.port = +port;

export default debug;

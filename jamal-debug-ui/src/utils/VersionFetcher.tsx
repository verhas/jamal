import axios from "axios";
class VersionFetcher {
  url: string =
    "https://raw.githubusercontent.com/verhas/jamal/master/version.jim";
  lastRelease: string = "";

  constructor() {
    axios.get(this.url).then((response) => {
      for (var s of ("" + response.data).split("\n")) {
        if (s.search(/\{@define\sLAST_RELEASE=(.*)\}/) === 0) {
          this.lastRelease = s.substring(22, s.length - 1);
        }
      }
    });
  }
}

export default VersionFetcher;

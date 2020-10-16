import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


    String OPTION_FILE = "./jamal.options";

    File options = new File(OPTION_FILE);
    Map commandLineOptions = new HashMap();
    List extraJars = new ArrayList<>();

    void loadOptions() throws IOException {
        if (!options.exists()) {
            return;
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(options)), StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.trim().startsWith("#")) {
                // skip comment lines
                continue;
            }
            int spaceIndex = line.indexOf(" ");
            if (spaceIndex == -1) {
                throw new RuntimeException("The file 'jamal.options' contains a line without space");
            }
            String key = line.substring(0, spaceIndex).trim();
            String value = line.substring(spaceIndex + 1).trim();

            switch (key) {

                case "to":
                case "from":
                case "open":
                case "close":
                case "pattern":
                case "exclude":
                case "source":
                case "target":
                    commandLineOptions.put(key, value);
                    break;
                case "jar":
                    extraJars.add(value);
                    break;
                default:
                    break;
            }

        }
    }

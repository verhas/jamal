import java.io.File;
import java.util.Map;
import java.nio.charset.StandardCharsets;

    String OPTION_FILE = "./jamal.options";

    File options = new File(OPTION_FILE);
    Map <String, String>commandLineOptions=new HashMap<>();
    List<String>extraJars=new ArrayList<>();

    void loadOptions()throws IOException{
        boot.info("processing jamal.options");
        if(!options.exists()){
            boot.info("no options");
            return;
         }

        BufferedReader reader=new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(options)),StandardCharsets.UTF_8));

        String line;
        while((line=reader.readLine())!=null){
            if(line.trim().startsWith("#")||line.trim().length()==0){
            // skip comment lines
            continue;
            }

            int spaceIndex=line.indexOf(" ");
            String key;
            String value;
            if(spaceIndex==-1){
                key=line;
                value="";
            }else{
                key=line.substring(0,spaceIndex).trim();
                value=line.substring(spaceIndex+1).trim();
            }

            switch(key){

                case "to"
                case "from":
                case "open":
                case "close":
                case "pattern":
                case "exclude":
                case "source":
                case "target":
                    if(commandLineOptions.get(key)!=null){
                        throw new RuntimeException("The key '"+key+"' appears more than one time in the jamal.options file");
                    }
                    commandLineOptions.put(key,value);
                    break;
                case "version":
                    VERSION=value;
                    break;
                case "cp":
                    boot.jar(value);
                break;
                case "jar":
                    extraJars.add(value);
                    break;
                default:
                throw new RuntimeException("Jamal option '"+key+"' is not supported");
            }

        }
    }
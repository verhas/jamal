
/open https://raw.githubusercontent.com/verhas/jamal/master/jamal-cmd/scripts/version.jsh
/open https://raw.githubusercontent.com/verhas/jamal/master/jamal-cmd/scripts/jarfetcher.jsh
/open https://raw.githubusercontent.com/verhas/jamal/master/jamal-cmd/scripts/executor.jsh
/open https://raw.githubusercontent.com/verhas/jamal/master/jamal-cmd/scripts/optionloader.jsh
/open https://raw.githubusercontent.com/verhas/jamal/master/jamal-cmd/scripts/defaultoptions.jsh

download("01engine/jamal-engine")
download("02api/jamal-api")
download("03tools/jamal-tools")
download("04core/jamal-core")
download("08cmd/jamal-cmd")

loadOptions()

for(String jarUrl:extraJars){
    LOCAL_CACHE.mkdirs();
    downloadUrl(jarUrl,LOCAL_CACHE);
    }

execute()

/exit
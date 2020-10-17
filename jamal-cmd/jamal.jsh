
/open scripts/version.jsh
/open scripts/jarfetcher.jsh
/open scripts/executor.jsh
/open scripts/optionloader.jsh
/open scripts/defaultoptions.jsh

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
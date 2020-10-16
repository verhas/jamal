
/open scripts/version.jsh
/open scripts/jarfetcher.jsh
/open scripts/executor.jsh
/open scripts/optionloader.jsh

download("01engine/jamal-engine")
download("02api/jamal-api")
download("03tools/jamal-tools")
download("04core/jamal-core")
download("05extensions/jamal-extensions")
download("08cmd/jamal-cmd")
loadOptions()

execute()


/exit
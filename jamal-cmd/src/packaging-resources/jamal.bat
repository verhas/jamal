@echo off
set "INSTALL_DIR=%~dp0.."
"%INSTALL_DIR%\runtime\bin\java.exe" -jar "%INSTALL_DIR%\app\jamal-cmd-2.8.3-SNAPSHOT.jar" %*

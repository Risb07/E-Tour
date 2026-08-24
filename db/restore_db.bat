@echo off
REM ===========================================================================
REM  eTour - RESTORE the database (run this on the TARGET PC)
REM
REM  Loads a .sql dump into a MySQL server that is already installed on this
REM  machine. For the Docker route you do NOT need this - see docker-compose.
REM
REM  Usage:
REM    - double-click, then type/paste the path to the .sql file, OR
REM    - drag the .sql file onto this .bat file
REM ===========================================================================
setlocal enabledelayedexpansion

set DB_USER=root
set DB_HOST=127.0.0.1
set DB_PORT=3306

echo.
echo  ==========================================================
echo   eTour database restore
echo  ==========================================================
echo.

REM --- 1. Which file? --------------------------------------------------------
set "DUMPFILE=%~1"
if "!DUMPFILE!"=="" (
    REM No file dragged on - offer whatever is in dumps\
    if exist "%~dp0dumps\*.sql" (
        echo  Dumps found in this folder:
        echo.
        for %%F in ("%~dp0dumps\*.sql") do echo    %%~nxF
        echo.
    )
    set /p "DUMPFILE=Full path to the .sql file: "
)

REM Strip surrounding quotes if the user pasted a quoted path.
REM  %%~A does this reliably; the !VAR:"=! trick breaks on some paths.
for /f "delims=" %%A in ("!DUMPFILE!") do set "DUMPFILE=%%~A"

if not exist "!DUMPFILE!" (
    echo.
    echo  [X] File not found: !DUMPFILE!
    echo.
    pause
    exit /b 1
)

REM --- 2. Find mysql.exe -----------------------------------------------------
set "MYSQL="

where mysql >nul 2>&1
if %ERRORLEVEL%==0 (
    set "MYSQL=mysql"
    goto :found
)

for %%V in ("MySQL Server 8.4" "MySQL Server 8.3" "MySQL Server 8.2" "MySQL Server 8.1" "MySQL Server 8.0" "MySQL Server 5.7") do (
    if exist "C:\Program Files\MySQL\%%~V\bin\mysql.exe" (
        set "MYSQL=C:\Program Files\MySQL\%%~V\bin\mysql.exe"
        goto :found
    )
    if exist "C:\Program Files (x86)\MySQL\%%~V\bin\mysql.exe" (
        set "MYSQL=C:\Program Files (x86)\MySQL\%%~V\bin\mysql.exe"
        goto :found
    )
)

if exist "C:\xampp\mysql\bin\mysql.exe" set "MYSQL=C:\xampp\mysql\bin\mysql.exe" & goto :found

echo  [X] Could not find mysql.exe automatically.
echo      Edit this file and set MYSQL to its full path.
echo.
pause
exit /b 1

:found
echo  [1/2] Using: !MYSQL!
echo        File : !DUMPFILE!
echo.
echo  ----------------------------------------------------------
echo   WARNING: the dump contains DROP DATABASE IF EXISTS etour.
echo   Any existing 'etour' database on THIS machine will be
echo   replaced by the one in the file.
echo  ----------------------------------------------------------
echo.
set /p "CONFIRM=Type YES to continue: "
if /I not "!CONFIRM!"=="YES" (
    echo  Cancelled. Nothing was changed.
    pause
    exit /b 0
)

echo.
echo  [2/2] Enter the MySQL password for user '%DB_USER%' on THIS machine
echo        (if there is no password, just press Enter)
echo.

"!MYSQL!" --host=%DB_HOST% --port=%DB_PORT% --user=%DB_USER% -p ^
          --default-character-set=utf8mb4 < "!DUMPFILE!"

if %ERRORLEVEL% neq 0 (
    echo.
    echo  [X] Restore FAILED. See the message above.
    echo.
    pause
    exit /b 1
)

echo.
echo  Done. Quick check - this should list your tables.
echo  (it asks for the password once more)
echo.
"!MYSQL!" --host=%DB_HOST% --port=%DB_PORT% --user=%DB_USER% -p ^
          -e "USE etour; SHOW TABLES; SELECT COUNT(*) AS tours FROM tour;"

echo.
echo   REMINDER: uploaded images live in Backend\uploads\ on the FILESYSTEM,
echo             not in the database. Copy that folder across too.
echo.
pause

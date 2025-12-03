@echo off
SETLOCAL

echo Generating secrets...
echo.

REM --- Generate CURRENT Secret ---
FOR /F "delims=" %%A IN ('openssl rand -base64 64') DO (
    SET "JWT_SECRET_CURRENT=%%A"
)

REM --- Generate OLD Secret ---
FOR /F "delims=" %%A IN ('openssl rand -base64 64') DO (
    SET "JWT_SECRET_OLD=%%A"
)

echo Secrets have been set:
echo ---------------------------------
echo JWT_SECRET_CURRENT: %JWT_SECRET_CURRENT%
echo JWT_SECRET_OLD:   %JWT_SECRET_OLD%
echo ---------------------------------
echo.

ENDLOCAL
pause
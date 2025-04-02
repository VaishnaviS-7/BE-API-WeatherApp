@echo off
del /s /q out
mkdir out

setlocal EnableDelayedExpansion
set "SOURCES="
for /R src %%f in (*.java) do (
    set "SOURCES=!SOURCES! %%f"
)
javac -cp gson-2.10.1.jar -d out !SOURCES!
endlocal

if %ERRORLEVEL% neq 0 (
    echo ❌ Compilation failed.
    exit /b
)

echo ✅ Compilation successful
java -cp "out;gson-2.10.1.jar" Main

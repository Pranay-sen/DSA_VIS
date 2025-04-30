@echo off
cd /d "%~dp0\code visualiser a\code visualiser a"
echo Starting Code Visualizer...
javac -cp . CodeVisualizer.java
if ERRORLEVEL 1 (
    echo Compilation failed!
    pause
    exit /b 1
)
echo Running Code Visualizer...
java -cp . CodeVisualizer
pause 
@echo off
echo Starting Wuziqi Game Services...

echo.
echo Starting Backend Service (Spring Boot)...
start "Backend Service" cmd /k "cd /d e:\MyWork\fiveqi\wuziqi\backend && mvnw.cmd spring-boot:run"

echo.
echo Installing Frontend Dependencies...
cd /d e:\MyWork\fiveqi\wuziqi\frontend
call npm install

echo.
echo Starting Frontend Service (React)...
start "Frontend Service" cmd /k "cd /d e:\MyWork\fiveqi\wuziqi\frontend && npm start"

echo.
echo Both services are starting in separate windows.
echo Backend will be available at: http://localhost:8081
echo Frontend will be available at: http://localhost:3000
echo.
pause
@echo off
echo 🚀 Starting Daana Web Local Server...
echo 📁 Project Directory: %CD%
echo.

REM Check if Node.js is available (preferred for dynamic routing)
node --version >nul 2>&1
if %errorlevel% == 0 (
    echo ✅ Node.js found - Starting Express server with dynamic routing support
    echo 🌐 Open your browser and go to: http://localhost:3000
    echo 🔗 Dynamic program URLs: http://localhost:3000/program.html/{slug}
    echo ⏹️  Press Ctrl+C to stop the server
    echo.
    
    REM Check if node_modules exists, if not install dependencies
    if not exist "node_modules" (
        echo 📦 Installing dependencies...
        npm install
    )
    
    node server.js
) else (
    echo ❌ Node.js not found!
    echo Please install Node.js for the best experience with dynamic routing
    echo.
    echo Alternative: Use Python for basic static file serving
    python --version >nul 2>&1
    if %errorlevel% == 0 (
        echo ✅ Python found - Starting basic server on port 3000
        echo ⚠️  Note: Dynamic program URLs will not work with this server
        echo 🌐 Open your browser and go to: http://localhost:3000
        echo ⏹️  Press Ctrl+C to stop the server
        echo.
        python -m http.server 3000
    ) else (
        echo ❌ Neither Node.js nor Python found!
        echo Please install Node.js for the best experience
        pause
        exit /b 1
    )
)

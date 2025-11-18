@echo off
echo Starting simple HTTP server for WebSocket test...
echo.
echo Open your browser and go to: http://localhost:3000/websocket-test.html
echo.
echo Press Ctrl+C to stop the server
echo.
python -m http.server 3000

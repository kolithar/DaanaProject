const express = require('express');
const path = require('path');
const app = express();
const PORT = process.env.PORT || 3000;

// Enable CORS for API calls
app.use((req, res, next) => {
    res.header('Access-Control-Allow-Origin', '*');
    res.header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
    res.header('Access-Control-Allow-Headers', 'Content-Type, Authorization');
    
    if (req.method === 'OPTIONS') {
        res.sendStatus(200);
    } else {
        next();
    }
});

// Serve static files from the current directory
app.use(express.static(__dirname));

// Handle dynamic program URLs
app.get('/program.html/:slug', (req, res) => {
    const slug = req.params.slug;
    console.log(`Serving program page for slug: ${slug}`);
    
    // Redirect to query parameter format
    res.redirect(`/program.html?slug=${encodeURIComponent(slug)}`);
});

// Alternative route format
app.get('/program/:slug', (req, res) => {
    const slug = req.params.slug;
    console.log(`Serving program page for slug: ${slug}`);
    
    // Redirect to query parameter format
    res.redirect(`/program.html?slug=${encodeURIComponent(slug)}`);
});

// Fallback: serve program.html for any unmatched routes that contain program
app.get('/program*', (req, res) => {
    console.log(`Fallback: serving program.html for path: ${req.path}`);
    res.sendFile(path.join(__dirname, 'program.html'));
});

// Serve index.html for root path
app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'index.html'));
});

// Handle 404 errors
app.use((req, res) => {
    console.log(`404 - File not found: ${req.path}`);
    res.status(404).send(`
        <!DOCTYPE html>
        <html>
        <head>
            <title>404 - Page Not Found</title>
            <style>
                body { font-family: Arial, sans-serif; text-align: center; padding: 50px; }
                h1 { color: #f51a2d; }
                a { color: #f51a2d; text-decoration: none; }
                a:hover { text-decoration: underline; }
            </style>
        </head>
        <body>
            <h1>404 - Page Not Found</h1>
            <p>The page you're looking for doesn't exist.</p>
            <p><a href="/">Go back to Home</a></p>
            <p><a href="/search-programs.html">Browse Programs</a></p>
        </body>
        </html>
    `);
});

// Start server
app.listen(PORT, () => {
    console.log(`🚀 Server running on http://localhost:${PORT}`);
    console.log(`📁 Serving static files from: ${__dirname}`);
    console.log(`🔗 Dynamic program URLs supported: /program.html/{slug} and /program/{slug}`);
    console.log(`🌐 CORS enabled for API calls`);
});

// Graceful shutdown
process.on('SIGTERM', () => {
    console.log('SIGTERM received, shutting down gracefully');
    process.exit(0);
});

process.on('SIGINT', () => {
    console.log('SIGINT received, shutting down gracefully');
    process.exit(0);
});


const express = require("express");

const app = express();

app.use(express.json());

app.get("/", (req, res) => {
    res.json({
        name: "Vortex SMP",
        status: "online",
        memory: "4GB",
        address: "play.vortexsmp.net"
    });
});

app.get("/status", (req, res) => {
    res.json({
        server: "Vortex SMP",
        status: "API online"
    });
});

const PORT = process.env.PORT || 3000;

app.listen(PORT, () => {
    console.log(`Vortex SMP API na porta ${PORT}`);
});

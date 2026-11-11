
const express = require("express");
const app = express();

app.use(express.json());

app.get("/", (req, res) => {
  res.json({
    name: "Vortex SMP",
    status: "configured",
    address: "br-eco-23.reis.host",
    javaPort: 25749,
    bedrockPort: "AGUARDANDO_PORTA_GEYSER",
    crossplay: true,
    geyser: true,
    floodgate: true
  });
});

app.listen(process.env.PORT || 3000);

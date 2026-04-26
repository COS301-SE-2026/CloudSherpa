import express from "express";
import { swaggerUiServe, swaggerUiSetup } from "./config/swagger";
import healthRoutes from "./routes/health.routes";

const app = express();

app.use(express.json());

app.use("/api-docs", swaggerUiServe, swaggerUiSetup);
app.use("/health", healthRoutes);

export default app;
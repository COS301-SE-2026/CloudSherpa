import swaggerJsdoc from "swagger-jsdoc";
import swaggerUi from "swagger-ui-express";

const swaggerSpec = swaggerJsdoc({
  definition: {
    openapi: "3.0.0",
    info: {
      title: "Alert Engine API",
      version: "1.0.0",
      description: "API documentation for the Alert Engine service",
    },
  },
  apis: ["./src/routes/*.ts"],
});

export const swaggerUiServe = swaggerUi.serve;
export const swaggerUiSetup = swaggerUi.setup(swaggerSpec);
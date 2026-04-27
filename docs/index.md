# CloudSherpa Documentation

Welcome to the CloudSherpa developer documentation.

## Main sections

!!! warning
    This documentation is intended as **development documentation**. It is automatically deployed on `push` to the `dev` and `main` branches of the _CloudSherpa_ repository. It is thus possible and highly likely that this documentation documents implementation done on the `dev` branch that **does not exist** on the `main` branch. For the current stage of _CloudSherpa_ development, this is intentional. It is still something to take note of and keep in mind as you are reading this documentation.

??? note "Developer Guides"
    [Developer Guides](dev/CheatSheet.md)

    Core development resources, including workflows, conventions, and quick-reference material for working within CloudSherpa.

??? note "Service Documentation"
    [Service Documentation](services/alert-engine.md)

    CloudSherpa follows a microservices architecture with event-driven communication. Each service is documented in detail, covering responsibilities and dependencies.

??? note "REST API Documentation"
    [REST API Documentation](api/api-placeholder.md)

    Swagger (OpenAPI) provides interactive documentation for all REST endpoints exposed across services.

??? note "Doxygen Code Reference"
    [Doxygen Documentation](doxygen/doxygen-placeholder.md)

    Inline code documentation is generated using Doxygen, providing detailed reference material for classes, functions, and internal logic.

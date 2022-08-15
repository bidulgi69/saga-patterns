rootProject.name = "microservice-saga-orchestration"

include(
    ":api",
    ":services:customer-service",
    ":services:restaurant-service",
    ":services:order-service",
    ":services:payment-service"
)

rootProject.name = "microservice-saga-choreography"

include(
    ":api",
    ":services:ticket-service",
    ":services:customer-service",
    ":services:airline-service",
    ":services:payment-service",
)
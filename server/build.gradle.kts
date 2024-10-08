extra["environmentVariables"] = mapOf(
        "RABBIT_HOST" to (System.getenv("RABBIT_HOST") ?: "localhost"),
        "MONGO_CONN" to (System.getenv("MONGO_CONN") ?: "mongodb://localhost:27017"),
        "ACCESS_TOKEN_SECRET" to (System.getenv("ACCESS_TOKEN_SECRET") ?: "replace with a real passworD"),
        "REFRESH_TOKEN_SECRET" to (System.getenv("REFRESH_TOKEN_SECRET") ?: "Replace with a real password"),
        "ACCESS_TOKEN_EXPIRATION" to (System.getenv("ACCESS_TOKEN_EXPIRATION")?.toIntOrNull() ?: 20), // 20 minutes
        "REFRESH_TOKEN_EXPIRATION" to (System.getenv("REFRESH_TOKEN_EXPIRATION")?.toIntOrNull() ?: 1440) // 24 hours
)

package env

import "os"

func getEnv(key, fallback string) string {
	if value, ok := os.LookupEnv(key); ok {
		return value
	}
	return fallback
}

func MongoHost() string {
	return getEnv("MONGO_HOST", "localhost:27017")
}

func MongoPassword() string {
	return getEnv("MONGO_PASSWORD", "admin")
}

func MongoUsername() string {
	return getEnv("MONGO_USERNAME", "admin")
}

type DBConstants struct {
	MongoAuthParams    string
	CodeListCollection string
	MongoDatabase      string
}

var DBConstantValues = DBConstants{
	MongoAuthParams:    "authSource=admin&authMechanism=SCRAM-SHA-1",
	CodeListCollection: "codelists",
	MongoDatabase:      "catalog-admin-service",
}

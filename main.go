package main

import (
	"github.com/Informasjonsforvaltning/catalog-admin-service/config"
	"github.com/Informasjonsforvaltning/catalog-admin-service/config/logging"
)

func main() {
	logging.LoggerSetup()
	router := config.SetupRouter()
	router.Run(":8080")
}

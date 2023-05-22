package main

import (
	"github.com/Informasjonsforvaltning/catalog-admin-service/config"
)

func main() {
	config.LoggerSetup()
	router := config.SetupRouter()
	router.Run(":8080")
}

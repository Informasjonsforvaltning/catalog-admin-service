package main

import (
	"github.com/Informasjonsforvaltning/catalog-admin-service/config"
)

func main() {
	router := config.SetupRouter()
	router.Run(":8080")
}

package main

import (
	"github.com/Informasjonsforvaltning/catalog-admin-service/handlers"
	"github.com/gin-gonic/gin"
)

func main() {
    router := gin.Default()
	InitializeRoutes(router)
	router.Run(":8080")
}

func InitializeRoutes(e *gin.Engine) {
	e.SetTrustedProxies(nil)
	e.GET("ping", handlers.PingHandler())
	e.GET("ready", handlers.ReadyHandler())
}

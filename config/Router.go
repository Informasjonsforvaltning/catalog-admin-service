package config

import (
	"github.com/Informasjonsforvaltning/catalog-admin-service/handlers"

	"github.com/gin-gonic/gin"
)

func SetupRouter() *gin.Engine {
	router := gin.New()
	router.Use(gin.Recovery())
	InitializeRoutes(router)
	return router
}

func InitializeRoutes(e *gin.Engine) {
	e.SetTrustedProxies(nil)
	e.GET("ping", handlers.PingHandler())
	e.GET("ready", handlers.ReadyHandler())
}

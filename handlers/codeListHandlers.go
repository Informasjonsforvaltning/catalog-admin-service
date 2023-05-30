package handlers

import (
	"net/http"

	"github.com/Informasjonsforvaltning/catalog-admin-service/service"
	"github.com/gin-gonic/gin"
)

func GetCodeListsHandler() func(c *gin.Context) {
	service := service.InitService()
	return func(c *gin.Context) {
		// conceptId := c.Param("conceptId")

		codeLists, status := service.GetCodeLists(c.Request.Context())
		if status == http.StatusOK {
			c.JSON(status, codeLists)
		} else {
			c.Status(status)
		}
	}
}

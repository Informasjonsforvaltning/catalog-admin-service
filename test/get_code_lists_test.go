package test

import (
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/Informasjonsforvaltning/catalog-admin-service/config"
	"github.com/Informasjonsforvaltning/catalog-admin-service/model"
	"github.com/stretchr/testify/assert"
)

func TestCodeLists(t *testing.T) {
	router := config.SetupRouter()

	w := httptest.NewRecorder()
	req, _ := http.NewRequest("GET", "/code-lists", nil)
	router.ServeHTTP(w, req)

	assert.Equal(t, http.StatusOK, w.Code)
	var actualResponse model.CodeLists
	err := json.Unmarshal(w.Body.Bytes(), &actualResponse)

	assert.Nil(t, err)
	assert.True(t, len(actualResponse.CodeLists) > 0)
}

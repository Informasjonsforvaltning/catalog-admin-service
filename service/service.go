package service

import (
	"context"
	"net/http"

	"github.com/Informasjonsforvaltning/catalog-admin-service/config/logging"
	"github.com/Informasjonsforvaltning/catalog-admin-service/model"
	"github.com/Informasjonsforvaltning/catalog-admin-service/repository"
	"github.com/sirupsen/logrus"
	"go.mongodb.org/mongo-driver/bson"
)

type CodeListsService interface {
	GetCodeLists(ctx context.Context)
}

type CodeListsServiceImpl struct {
	CodeListRepository repository.CodeListRepositoryImpl
}

func InitService() *CodeListsServiceImpl {
	service := CodeListsServiceImpl{
		CodeListRepository: *repository.InitRepository(),
	}
	return &service
}

func (service *CodeListsServiceImpl) GetCodeLists(ctx context.Context) (*model.CodeLists, int) {
	query := bson.D{}
	// query = append(query, bson.E{Key: "resourceId", Value: conceptId})

	databaseCodeLists, err := service.CodeListRepository.GetCodeLists(ctx, query)
	if err != nil {
		logrus.Error("Get code lists failed")
		logging.LogAndPrintError(err)
		return nil, http.StatusInternalServerError
	}

	if databaseCodeLists == nil {
		logrus.Error("No code lists found")
		logging.LogAndPrintError(err)
		return &model.CodeLists{CodeLists: []*model.CodeList{}}, http.StatusOK
	} else {
		logrus.Info("Returning code lists")
		return &model.CodeLists{CodeLists: databaseCodeLists}, http.StatusOK
	}
}

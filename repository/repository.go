package repository

import (
	"context"

	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"

	"github.com/Informasjonsforvaltning/catalog-admin-service/config/db"
	"github.com/Informasjonsforvaltning/catalog-admin-service/config/logging"
	"github.com/Informasjonsforvaltning/catalog-admin-service/model"
)

// codeListRepository is a struct that holds a reference to a MongoDB collection
type CodeListRepositoryImpl struct {
	collection *mongo.Collection
}

type CodeListRepository interface {
	GetCodeLists(ctx context.Context, query bson.D) ([]*model.CodeList, error)
}

var codeListRepository *CodeListRepositoryImpl

func InitRepository() *CodeListRepositoryImpl {
	if codeListRepository == nil {
		codeListRepository = &CodeListRepositoryImpl{collection: db.CodeListCollection()}
	}
	return codeListRepository
}

func (r CodeListRepositoryImpl) GetCodeLists(ctx context.Context, query bson.D) ([]*model.CodeList, error) {

	current, err := r.collection.Find(ctx, query)
	if err != nil {
		logging.LogAndPrintError(err)
		return nil, err
	}
	defer current.Close(ctx)

	var codeLists []*model.CodeList

	for current.Next(ctx) {
		var codeList model.CodeList
		err := bson.Unmarshal(current.Current, &codeList)
		if err != nil {
			logging.LogAndPrintError(err)
			return nil, err
		}
		codeLists = append(codeLists, &codeList)
	}
	if err := current.Err(); err != nil {
		logging.LogAndPrintError(err)
		return nil, err
	}
	return codeLists, nil
}

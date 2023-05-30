package db

import (
	"context"

	"github.com/sirupsen/logrus"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"

	"github.com/Informasjonsforvaltning/catalog-admin-service/config/env"
)

func ConnectionString() string {
	authParams := env.DBConstantValues.MongoAuthParams
	dbName := env.DBConstantValues.MongoDatabase
	host := env.MongoHost()
	password := env.MongoPassword()
	user := env.MongoUsername()

	connectionString := "mongodb://" + user + ":" + password + "@" + host + "/" + dbName + "?" + authParams

	return connectionString
}

func MongoCollection(collectionName string) *mongo.Collection {
	mongoOptions := options.Client().ApplyURI(ConnectionString())
	client, err := mongo.Connect(context.Background(), mongoOptions)
	if err != nil {
		logrus.Error("mongo client failed", err)
	}
	if err != nil {
		logrus.Error("mongo client connection failed", err)
	}
	collection := client.Database(env.DBConstantValues.MongoDatabase).Collection(collectionName)

	return collection
}

func CodeListCollection() *mongo.Collection {
	return MongoCollection(env.DBConstantValues.CodeListCollection)
}

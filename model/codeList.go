package model

type CodeList struct {
	ID   string `bson:"_id" json:"id"`
	Name string `bson:"name" json:"name"`
}

type CodeLists struct {
	CodeLists []*CodeList `json:"codeLists"`
}

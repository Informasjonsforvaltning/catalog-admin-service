# catalog-admin-service

This service is responsible for settings in concept catalogs. This includes code lists, internal fields and editable fields. It is also responsible for settings across different catalogs including design and responsible users. 

This is a REST API. The Open-API spec can be found [here](https://raw.githubusercontent.com/Informasjonsforvaltning/catalog-admin-service/main/openapi.yaml). 

## Requirements
- maven
- java 21
- docker
- docker-compose

## Run tests
```
mvn test
```

## Run locally

### Start catalog admin service
Start catalog admin service locally using maven. Use Spring profile **develop**.
```
mvn spring-boot:run -Dspring-boot.run.profiles=develop
```


## Usage examples
Obs! Remember to use correct catalogId. 

### Example: create code list

Method: POST

URL: https://catalog-admin.staging.fellesdatakatalog.digdir.no/api/code-lists/[catalogId]

Payload:
```
{
    "codeList": {
        "id": "",
        "catalogId": [catalogId],
        "name": "Oppretter kodeliste",
        "description": "Beskrivelse",
        "codes": [
            {
                "id": "2eecd911-580b-47b9-b305-532ca68d29ca",
                "name": {
                    "nb": "Ny kode 1",
                    "nn": "",
                    "en": ""
                },
                "parentID": null
            },
            {
                "id": "9ddcf0b1-dafb-4a34-89be-b9c50343023c",
                "name": {
                    "nb": "Ny kode 2",
                    "nn": "",
                    "en": ""
                },
                "parentID": null
            }
        ]
    }
}
```

### Example: Update design

URL: https://catalog-admin.staging.fellesdatakatalog.digdir.no/api/design/[catalogId]/design

Method: PATCH

Payload:

```
[
    {
        "op": "replace",
        "path": "/logoDescription",
        "value": "qt-dawg"
    },
    {
        "op": "replace",
        "path": "/backgroundColor",
        "value": "#8087ff"
    }
]

```

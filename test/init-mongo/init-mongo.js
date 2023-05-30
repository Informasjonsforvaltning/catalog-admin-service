db = db.getSiblingDB('catalog-admin-service');
db.createCollection('codelists');
db.codelists.insert({_id: '1234', name: 'Hege'});

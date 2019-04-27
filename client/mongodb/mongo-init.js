db.createUser(
    {
      user: "evgerher",
      pwd: "evgerher",
      roles: [
        {
          role: "readWrite",
          db: "university"
        }
      ]
    }
);
db.get('university', function(err, client) { // todo: test it working
  client.universities.createIndex({"name": 1}, {unique: true});
  client.speciality.createIndex({"name": 1, "university": 1}, {unique: true});
});
// db.university.universities.createIndex({"name": 1}, {unique: true});
// db.university.speciality.createIndex({"name": 1, "university": 1}, {unique: true});

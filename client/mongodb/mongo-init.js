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

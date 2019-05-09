# iroha-universities
## Project description
Project is a system for __applicants' application process__. There exist two sides: universities and applicants.  
Students do not receive __private/public__ keys, we decided to store it in MongoDB with other relevant information. It can be improved, the solution would require transaction sign in the web (iroha-js library).   

1) Each of the universities is registered before the blockchain network is started.
The `genesis block` initialization:  
 - `wild-university` token is created
 - each of the university is registered as a domain
 - `university-specific-wild-speciality` token is created as an asset with university domain
 - each of the `university-specific-speciality-%s` is created as an asset with university domain
2) Student can:
 - register in a system in order to obtain 5 `university-wild` tokens   
 - select up to 5 universities (exchange `university-wild` to `university-specific-wild-speciality` tokens)  
 - select at most 3 specialities in each of the university (exchange `university-specific-wild-speciality` to `university-specific-speciality-A` token)  
 - deselect those (exchange `university-specific-speciality-A` to `university-specific-wild-speciality` or 5x`university-specific-wild-speciality` into `university-wild`)  
 - exhange specialities (exchange `university-specific-speciality-A` to `university-specific-speciality-B`)  
 
 __The pipeline:__
 1) Universities register in a system (their information about university and its specialities is stored in MongoDB)  
 2) The `/iroha` POST request is called (iroha chain starts)
 3) Students can register in a system and select interested specialities

__Internal:__
1) `Spring-boot` server + `iroha-java` client
2) `MongoDB`
3) `iroha-nodes` in docker-compose file

__Rests:__
1) `university/` - registration and description retrival methods  
2) `applicant/` - registration, description and exchange methods  
3) Take a look at `.../resources/postman-iroha-universities-collection.json` for detailed information  


## How to run a project
Project is splitted into two units:
1) `blockchain/IrohaMain` - testing custom Iroha communication (can be rewritten as tests)  
2) `server/SpringMain` - initializes Spring web app  
3) `server/TestMain` - testing custom Iroha communication with already instantiated Iroha nodes and stored data in Mongo db (can be rewritten as tests)  

## Project structure
* `client/` - java maven project
  * `blockchain/` - module with iroha logic
  * `models/` - shared module
  * `server/` - web Spring application
  * `docker-compose.yml` - MongoDB instance
  * `mongodb/` - initialization file for Mongo DB
* `docker/` - folder with required files for iroha instances  
  * `genesis-*/` - concrete folder for university, contains only `config.docker`  
  * `genesis-university-example/` - shows four files required for university to be instantiated as Iroha node.   
    - `genesis.block` - defines used ports and postgres instance (mirrors content of `docker-compose.yml` file)  
    - `node.priv` - private key for university, generated each time when Iroha is instantiated manually or from web client.  
    - `node.pub` - public key for university, generated each time when Iroha is instantiated manually or from web client.  
    - `genesis.block` - genesis block, generated automatically from universities collected by web client or manually (see `IrohaMain` in _client/blockchain_)    
  * `docker-compose.yml` - dc file for instantiation of 3 instances, creates network per each  
  * `docker-compose-single.yml` - currently nonrelevant dc file (was used to run one instance)  
  

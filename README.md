# iroha-universities

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
  

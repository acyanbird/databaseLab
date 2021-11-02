DROP TABLE IF EXISTS anage CASCADE;
create table anage(
    Species VARCHAR(100) NOT NULL PRIMARY KEY,
    CommonName VARCHAR(100) NOT NULL,
    GestationIncubation INTEGER,
    LitterOrClutchSize NUMERIC(12,2) CHECK (LitterOrClutchSize < 300000000),
    LittersOrClutchesPerYear DECIMAL(3,2) CHECK (LittersOrClutchesPerYear <= 10),
    BirthWeight DECIMAL(7,2) CHECK (BirthWeight <= 2000000),
    AdultWeight DECIMAL(9,2) CHECK (AdultWeight <= 136000000),
    MaximumLongevity DECIMAL(5,1) CHECK (MaximumLongevity <= 15000)
);

/*DROP TABLE IF EXISTS species_details;
CREATE TABLE species_details (
    NBNID VARCHAR(16) NOT NULL PRIMARY KEY,
    TaxRank --TODO,
    ScientificName --TODO,
    CommonName --TODO,
    EstablishmentMeans --TODO,
    Habitat --TODO
);*/

/*DROP TABLE IF EXISTS observations;
CREATE TABLE observations(
    ObservationID SERIAL PRIMARY KEY,
    NBNID --TODO,
    Latitude --TODO,
    Longitude --TODO,
    ObsDate DATE NOT NULL,
    DataSetID --TODO,
    Licence VARCHAR(100) NOT NULL,
    RightsHolder --TODO,
    Recorder --TODO,
    Determiner --TODO
);*/

/*DROP TABLE IF EXISTS providers;
CREATE TABLE providers(
    providerID VARCHAR(5) PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);*/

/*DROP TABLE IF EXISTS datasets;
CREATE TABLE datasets(
    datasetID VARCHAR(6) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    provider VARCHAR(5) NOT NULL
);*/

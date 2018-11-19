CREATE TABLE transactions(
    hash VARCHAR(100) NOT NULL ,
    time_added DATETIME DEFAULT NOW(),
    details JSON NOT NULL ,
    PRIMARY KEY(hash)
);
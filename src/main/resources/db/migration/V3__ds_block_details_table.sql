CREATE TABLE dsblocks(
    block_num INTEGER NOT NULL ,
    time_added DATETIME DEFAULT NOW(),
    details JSON NOT NULL ,
    PRIMARY KEY(block_num)
);

CREATE TABLE blockchain_summary(
    transaction_rate DECIMAL NOT NULL ,
    tx_block_num INTEGER,
    time_added DATETIME DEFAULT NOW(),
    day_added DATE AS (DATE(time_added)),
    PRIMARY KEY(time_added)
);

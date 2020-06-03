CREATE UNIQUE INDEX UX_acct_type ON AcctType(acct_type);
CREATE INDEX IX_bank_id ON Account(bank_id);

SELECT COUNT(email) AS edu_email_count FROM Member WHERE email LIKE "%edu";

SELECT (
	(SELECT CAST(COUNT(email) AS REAL) FROM Member WHERE email LIKE "%edu") /
	(SELECT CAST(COUNT(*) AS REAL) FROM Member) * 100.00
) AS percentage;

SELECT state, COUNT(*) AS count FROM Bank GROUP BY state;

SELECT SUM(balance) / 100.00 AS total_balance FROM ATM WHERE location_name LIKE '%Publix%';

SELECT MIN(balance) AS min, MAX(balance) AS max FROM ATM;

SELECT (
	(SELECT CAST(SUM(num_of_tran) AS REAL) FROM ATM INNER JOIN Bank ON ATM.bank_id = Bank.bank_id WHERE Bank.state = 'FL') /
	(SELECT CAST(SUM(num_of_tran) AS REAL) FROM ATM) * 100.00
) AS atm_table_transaction_percentage;

SELECT (
	(SELECT CAST(COUNT(*) AS REAL) AS fl_transaction_count FROM ATM_transaction
	 INNER JOIN ATM ON ATM_transaction.atm_id = ATM.atm_id
	 INNER JOIN Bank ON ATM.bank_id = Bank.bank_id WHERE Bank.state = 'FL') /
	(SELECT CAST(COUNT(*) AS REAL) FROM ATM_transaction) * 100.00
) AS atm_transaction_table_transaction_percentage;

SELECT atm_id, AVG(num_of_tran) FROM ATM GROUP BY atm_id;
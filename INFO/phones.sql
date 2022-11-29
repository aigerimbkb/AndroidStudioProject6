/* 
/data/data/kz.talipovsn.database/databases/phones
*/
DROP TABLE IF EXISTS emergency_service
CREATE TABLE emergency_service(id INTEGER PRIMARY KEY,name TEXT,name_lc TEXT,phone_number TEXT)
SELECT  * FROM emergency_service WHERE (name_lc LIKE '%11%' OR phone_number LIKE '%11%') ORDER BY name

SELECT * FROM
    t20,
    t15,
    t09
WHERE 1=1 
    AND t15_F_t20 = t20_P
    AND t15_F_t09 = t09_P
    AND (t09_c001 = 46) OR (t09_c001 = 62) OR (t09_c001 = 8) OR (t09_c001 = 26) OR (t09_c001 = 18) OR (t09_c001 = 22) OR (t09_c001 = 32)
;
SELECT * FROM
    t15,
    t20,
    t10,
    t09
WHERE 1=1 
    AND t15_F_t09 = t09_P
    AND t15_F_t20 = t20_P
    AND t20_F_t10 = t10_P
    AND (t20_c008 > 14) AND (t10_c001 = 4) AND ((t09_c001 = 50) OR (t09_c001 = 36) OR (t09_c001 = 2) OR (t09_c001 = 20))
;
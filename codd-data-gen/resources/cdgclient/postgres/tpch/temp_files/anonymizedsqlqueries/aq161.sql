SELECT * FROM
    t15,
    t09,
    t20,
    t10
WHERE 1=1 
    AND t15_F_t20 = t20_P
    AND t15_F_t09 = t09_P
    AND t20_F_t10 = t10_P
    AND ((t09_c001 = 46) OR (t09_c001 = 48) OR (t09_c001 = 8) OR (t09_c001 = 62)) AND (t20_c008 > 14) AND ((t10_c001 = 4) OR (t10_c001 = 2))
;
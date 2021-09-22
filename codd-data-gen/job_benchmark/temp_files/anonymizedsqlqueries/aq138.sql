SELECT * FROM
    t15,
    t09,
    t20,
    t10
WHERE 1=1 
    AND t15_F_t20 = t20_P
    AND t15_F_t09 = t09_P
    AND t20_F_t10 = t10_P
    AND ((t09_c001 = 58) OR (t09_c001 = 44) OR (t09_c001 = 4) OR (t09_c001 = 60) OR (t09_c001 = 24) OR (t09_c001 = 62) OR (t09_c001 = 38) OR (t09_c001 = 64) OR (t09_c001 = 12) OR (t09_c001 = 34)) AND (t20_c008 > 14) AND (t10_c001 = 4)
;
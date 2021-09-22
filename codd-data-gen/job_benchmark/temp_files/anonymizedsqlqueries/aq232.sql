SELECT * FROM
    t20,
    t15,
    t09
WHERE 1=1 
    AND t15_F_t20 = t20_P
    AND t15_F_t09 = t09_P
    AND (t20_c008 > 28) AND ((t09_c001 = 58) OR (t09_c001 = 56) OR (t09_c001 = 54) OR (t09_c001 = 44) OR (t09_c001 = 4) OR (t09_c001 = 60) OR (t09_c001 = 24) OR (t09_c001 = 62))
;
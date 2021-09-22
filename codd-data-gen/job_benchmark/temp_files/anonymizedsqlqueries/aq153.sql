SELECT * FROM
    t15,
    t09,
    t20
WHERE 1=1 
    AND t15_F_t20 = t20_P
    AND t15_F_t09 = t09_P
    AND (t09_c001 = 56) AND (t20_c008 = 12)
;
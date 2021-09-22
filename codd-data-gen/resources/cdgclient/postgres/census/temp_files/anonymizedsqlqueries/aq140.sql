SELECT * FROM
    t20,
    t10,
    t14,
    t08
WHERE 1=1 
    AND t14_F_t20 = t20_P
    AND t20_F_t10 = t10_P
    AND t14_F_t08 = t08_P
    AND (t20_c008 > 16) AND (t10_c001 = 4) AND (t14_c001 > 16) AND (t08_c001 = 14)
;
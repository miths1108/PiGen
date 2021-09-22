SELECT * FROM
    t20,
    t08,
    t14
WHERE 1=1 
    AND t14_F_t20 = t20_P
    AND t14_F_t08 = t08_P
    AND (t20_c008 > 26) AND (t08_c001 = 14) AND (t14_c001 > 20)
;
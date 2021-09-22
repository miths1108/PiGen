SELECT * FROM
    t01,
    t20
WHERE 1=1 
    AND t01_F_t20 = t20_P
    AND t20_c008 > 8
;
SELECT * FROM
    t20,
    t07,
    t04,
    t10
WHERE 1=1 
    AND t20_F_t10 = t10_P
    AND t07_F_t20 = t20_P
    AND t07_F_t04 = t04_P
    AND (t20_c008 > 14) AND (t04_c001 = 6) AND (t10_c001 = 4)
;
SELECT * FROM
    t18,
    t07,
    t16
WHERE 1=1 
    AND t18_F_t16 = t16_P
    AND t18_F_t07 = t07_P
    AND (t07_c027 = 6) AND (t16_c010 = 2)
;
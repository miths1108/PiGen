SELECT * FROM
    t18,
    t07,
    t12
WHERE 1=1 
    AND t18_F_t12 = t12_P
    AND t18_F_t07 = t07_P
    AND (t07_c027 = 6) OR ((t07_c027 = 4) AND (t07_c019 = 16)) OR ((t07_c027 = 8) AND (t07_c019 = 2))
;
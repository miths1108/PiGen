SELECT * FROM
    t12,
    t18,
    t07,
    t05
WHERE 1=1 
    AND t18_F_t12 = t12_P
    AND t18_F_t05 = t05_P
    AND t18_F_t07 = t07_P
    AND ((t07_c027 = 2) AND (t07_c019 = 2)) AND (t05_c005 = 4)
;
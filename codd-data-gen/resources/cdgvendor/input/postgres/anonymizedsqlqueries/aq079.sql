SELECT * FROM
    t18,
    t07,
    t16,
    t12,
    t13
WHERE 1=1 
    AND t18_F_t13 = t13_P
    AND t18_F_t12 = t12_P
    AND t18_F_t16 = t16_P
    AND t18_F_t07 = t07_P
    AND ((t07_c027 = 6) AND (t07_c019 = 14)) AND (t16_c010 = 2) AND (t12_c002 = 6) AND ((t13_c003 = 2) OR (t13_c004 = 4) OR (t13_c008 = 2))
;
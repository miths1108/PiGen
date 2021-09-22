SELECT * FROM
    t12,
    t18,
    t07,
    t06,
    t16
WHERE 1=1 
    AND t18_F_t12 = t12_P
    AND t18_F_t16 = t16_P
    AND t18_F_t06 = t06_P
    AND t18_F_t07 = t07_P
    AND (t07_c027 = 4) AND ((t06_c006 = 2) AND (t06_c007 = 6) AND (t06_c005 = 8)) AND ((t16_c019 = 2) OR (t16_c019 = 2) OR (t16_c019 = 2) OR (t16_c019 = 2) OR (t16_c019 = 2) OR (t16_c019 = 2))
;
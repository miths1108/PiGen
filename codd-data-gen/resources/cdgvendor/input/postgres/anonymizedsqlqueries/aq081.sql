SELECT * FROM
    t18,
    t07,
    t16,
    t09
WHERE 1=1 
    AND t18_F_t09 = t09_P
    AND t18_F_t16 = t16_P
    AND t18_F_t07 = t07_P
    AND ((t07_c009 >= 2) AND (t07_c009 <= 4) AND ((t07_c027 = 2) OR (t07_c027 = 4) OR (t07_c027 = 6))) AND ((t16_c000 = 4) OR (t16_c000 = 2)) AND ((t09_c002 = 14) OR (t09_c004 = 10))
;
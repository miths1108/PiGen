SELECT * FROM
    t18,
    t07,
    t09,
    t16
WHERE 1=1 
    AND t18_F_t16 = t16_P
    AND t18_F_t09 = t09_P
    AND t18_F_t07 = t07_P
    AND (((t07_c027 = 2) OR (t07_c027 = 4) OR (t07_c027 = 6)) AND (((t07_c009 >= 2) AND (t07_c009 <= 6)) OR ((t07_c009 >= 10) AND (t07_c009 <= 12)))) AND ((t09_c004 > 4) AND ((t09_c000 = 6) OR (t09_c000 = 2))) AND ((t16_c005 = 2) OR (t16_c005 = 2) OR (t16_c005 = 2) OR (t16_c005 = 2) OR (t16_c005 = 2) OR (t16_c005 = 2) OR (t16_c005 = 2) OR (t16_c005 = 2))
;
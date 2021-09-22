SELECT * FROM
    t18,
    t16,
    t19,
    t09
WHERE 1=1 
    AND t18_F_t09 = t09_P
    AND t18_F_t19 = t19_P
    AND t18_F_t16 = t16_P
    AND (t16_c021 = 2) AND ((t19_c003 < 2) AND (t19_c001 = 8)) AND (((t09_c002 = 8) AND (t09_c004 <= 12)) OR ((t09_c002 = 2) AND (t09_c004 <= 6)) OR ((t09_c002 = 4) AND (t09_c004 <= 8)))
;
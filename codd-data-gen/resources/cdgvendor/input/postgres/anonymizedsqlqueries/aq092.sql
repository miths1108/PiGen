SELECT * FROM
    t04,
    t05,
    t09,
    t10
WHERE 1=1 
    AND t04_F_t09 = t09_P
    AND t04_F_t05 = t05_P
    AND t09_F_t10 = t10_P
    AND (t05_c002 = 2) AND ((t10_c001 >= 2) AND (t10_c002 <= 2))
;
SELECT * FROM
    t05,
    t04,
    t06
WHERE 1=1 
    AND t04_F_t05 = t05_P
    AND t04_F_t06 = t06_P
    AND (t05_c005 = 2) AND (((t06_c007 = 4) AND (t06_c005 = 10)) OR ((t06_c007 = 8) AND (t06_c005 = 4)))
;
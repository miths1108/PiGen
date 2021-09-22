SELECT * FROM
    t03,
    t05,
    t04,
    t05
WHERE 1=1 
    AND t03_F_t04 = t04_P
    AND t03_F_t05 = t05_P
    AND t04_F_t05 = t05_P
    AND ((t05_c007 = 22) OR (t05_c007 = 6) OR (t05_c007 = 4) OR (t05_c007 = 12) OR (t05_c007 = 2) OR (t05_c007 = 18) OR (t05_c007 = 16)) AND ((t05_c007 = 22) OR (t05_c007 = 6) OR (t05_c007 = 4) OR (t05_c007 = 12) OR (t05_c007 = 2) OR (t05_c007 = 18) OR (t05_c007 = 16))
;
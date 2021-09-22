SELECT * FROM
    t04,
    t05
WHERE 1=1 
    AND t04_F_t05 = t05_P
    AND (t05_c007 = 20) OR (t05_c007 = 8) OR (t05_c007 = 12)
;
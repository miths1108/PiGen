SELECT * FROM
    t03,
    t07,
    t05,
    t00
WHERE 1=1 
    AND t03_F_t00 = t00_P
    AND t03_F_t05 = t05_P
    AND t03_F_t07 = t07_P
    AND ((t07_c005 >= 18) AND (t07_c005 <= 24)) AND (t05_c007 = 10) AND ((t00_c008 = 2) OR (t00_c008 = 2) OR (t00_c008 = 2) OR (t00_c008 = 2) OR (t00_c008 = 2))
;
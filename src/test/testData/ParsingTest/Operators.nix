[
(e.attrpath)
(e.attrpath or def)
(e1.attrpath e2.attrpath)
(-e1 e2)
(-e ? attrpath)
(e1 ? attrpath ++ e2 ? attrpath)
(e11 ++ e12 * e21 ++ e22)
(e11 ++ e12 / e21 ++ e22)
(e11 * e12 + e21 / e22)
(e11 / e12 - e21 * e22)
(!e1 + e2)
(!e1 // !e2)
(e11 // e12 < e21 // e22)
(e11 // e12 <= e21 // e22)
(e11 // e12 > e21 // e22)
(e11 // e12 >= e21 // e22)
(e11 < e12 == e21 <= e22)
(e11 > e12 != e21 >= e22)
(e11 == e12 && e21 != e22)
(e11 && e12 || e21 && e22)
(e11 || e12 -> e21 || e22)
]

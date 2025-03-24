grammar Expressions;

expressions: expression+;
expression: mult | add;
mult: number MULTIPLY number (MULTIPLY number)*;
add: (number | mult) ADDITIVE (number | mult) (ADDITIVE (number | mult))*;
number: INT;

WS: [ \t\r\n]+ -> skip;
MULTIPLY: '*';
ADDITIVE: '+';
INT: [0-9]+;
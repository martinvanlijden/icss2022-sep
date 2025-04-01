    grammar ICSS;

    //--- LEXER: ---

    // IF support:
    IF: 'if';
    ELSE: 'else';
    BOX_BRACKET_OPEN: '[';
    BOX_BRACKET_CLOSE: ']';


    //Literals
    TRUE: 'TRUE';
    FALSE: 'FALSE';
    PIXELSIZE: [0-9]+ 'px';
    PERCENTAGE: [0-9]+ '%';
    SCALAR: [0-9]+;


    //Color value takes precedence over id idents
    COLOR: '#' [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f];

    //Specific identifiers for id's and css classes
    ID_IDENT: '#' [a-z0-9\-]+;
    CLASS_IDENT: '.' [a-z0-9\-]+;

    //General identifiers
    LOWER_IDENT: [a-z] [a-z0-9\-]*;
    CAPITAL_IDENT: [A-Z] [A-Za-z0-9_]*;

    //All whitespace is skipped
    WS: [ \t\r\n]+ -> skip;

    //
    OPEN_BRACE: '{';
    CLOSE_BRACE: '}';
    SEMICOLON: ';';
    COLON: ':';
    PLUS: '+';
    MIN: '-';
    MUL: '*';
    ASSIGNMENT_OPERATOR: ':=';


    //--- PARSER: ---
    stylesheet: (styleRule | variableAssignment)*;

    styleRule: tagselector OPEN_BRACE styleOption* CLOSE_BRACE;

    tagselector: ID_IDENT | CLASS_IDENT | LOWER_IDENT;

    declaration: property COLON propertyValue SEMICOLON;

    styleOption: declaration | variableAssignment | ifClause;

    propertyValue: literalValue | variableReference | expression;

    property: 'background-color' | 'width' | 'color' | 'height';

    variableAssignment: variableReference ASSIGNMENT_OPERATOR propertyValue SEMICOLON;

    variableReference: CAPITAL_IDENT;

    literalValue: TRUE | FALSE | PIXELSIZE | COLOR | PERCENTAGE| SCALAR;

    expression :
        expression MUL expression |
        expression (PLUS | MIN) expression |
        types ;

    types: literalValue | variableReference;

    ifClause: IF BOX_BRACKET_OPEN ifExpression BOX_BRACKET_CLOSE OPEN_BRACE styleOption* CLOSE_BRACE elseClause?;

    elseClause: ELSE OPEN_BRACE styleOption CLOSE_BRACE;

    ifExpression: variableReference | booleanValue;

    booleanValue: TRUE | FALSE;



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
    stylesheet: variableAssignment* styleRule+ EOF;

    variableAssignment: CAPITAL_IDENT ASSIGNMENT_OPERATOR value SEMICOLON;

    styleRule:selector OPEN_BRACE declaration+ CLOSE_BRACE;

    selector: ID_IDENT | CLASS_IDENT | LOWER_IDENT;  // Selectors: #id, .class, tag

    declaration: property COLON value SEMICOLON;  // property: value;

    property: 'background-color' | 'width' | 'color';  // Allowed properties in Level 0

    value: COLOR | PIXELSIZE | PERCENTAGE | SCALAR | TRUE | FALSE | CAPITAL_IDENT;
package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.HashMap;



public class Checker {

    private IHANLinkedList<HashMap<String, ExpressionType>> variableTypes;

    public void check(AST ast) {
        // variableTypes = new HANLinkedList<>();
        checkStyleheet(ast.root);
    }

    private void checkStyleheet(Stylesheet node) {
        for (ASTNode child : node.getChildren()) {
            if (child instanceof Declaration) {
                checkDeclaration((Declaration) child);
            }
        }
    }

    private void checkDeclaration(Declaration node) {
        if (node.property.name.equals("width")) {
            // Check if the value of 'width' is a ColorLiteral, which is invalid
            if (node.expression instanceof ColorLiteral) {
                node.setError("Width can't be a color");
            }
            // Check if the width is of valid PixelLiteral type (you can add more validation logic here)
            else if (!(node.expression instanceof PixelLiteral)) {
                node.setError("Width must be a valid pixel size");
            }
        } else if (node.property.name.equals("background-color")) {
            // Ensure background-color is assigned a valid ColorLiteral
            if (!(node.expression instanceof ColorLiteral)) {
                node.setError("Background-color must be a valid color");
            }
        } else if (node.property.name.equals("color")) {
            // Ensure color is assigned a valid ColorLiteral
            if (!(node.expression instanceof ColorLiteral)) {
                node.setError("Color must be a valid color");
            }
        }
    }


}

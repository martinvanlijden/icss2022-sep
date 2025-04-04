package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.types.ExpressionType;
import nl.han.ica.linkedList.HANLinkedList;
import java.util.HashMap;

public class Checker {

    private IHANLinkedList<HashMap<String, ExpressionType>> variableTypes;

    public void check(AST ast) {
        variableTypes = new HANLinkedList<>();
        loopThruNodes(ast.root);

    }
    private void loopThruNodes(ASTNode node) {
        if(node instanceof Stylesheet | node instanceof Stylerule | node instanceof IfClause ) {
            variableTypes.addFirst(new HashMap<>());
        }


        checkVariable(node);
        checkDeclaration(node);
        checkIfStatement(node);

        for (ASTNode child : node.getChildren()) {
            loopThruNodes(child);
        }

        if(node instanceof Stylesheet | node instanceof Stylerule | node instanceof IfClause ) {
            variableTypes.removeFirst();
        }
    }

    private void checkDeclaration(ASTNode node) {
        if (node instanceof Declaration) {
            Declaration declaration = (Declaration) node;
            PropertyName propertyName = declaration.property;
            Expression expression = declaration.expression;

            if (propertyName.name.equals("width") || propertyName.name.equals("height")) {
                if(expression instanceof VariableReference){
                    VariableReference variableReference = (VariableReference) expression;
                    ExpressionType variableType = getExpressionFromVariableReference(variableReference);
                    if (variableType != ExpressionType.PIXEL && variableType != ExpressionType.PERCENTAGE && variableType != ExpressionType.SCALAR) {
                        ((Declaration) node).property.setError("Variable in width / height must be a pixel or scalar literal");
                    }
                }else {
                    ExpressionType expressionType = getExpressionType(expression);
                    if (expressionType != ExpressionType.PIXEL && expressionType != ExpressionType.PERCENTAGE) {
                        ((Declaration) node).property.setError("Width and height must be a pixel or percentage literal");
                    }
                }
            }
            if (propertyName.name.equals("color") || propertyName.name.equals("background-color")) {
                if(expression instanceof VariableReference){
                    VariableReference variableReference = (VariableReference) expression;
                    ExpressionType variableType = getExpressionFromVariableReference(variableReference);
                    if (variableType != ExpressionType.COLOR) {
                        ((Declaration) node).property.setError("Variable in color / background-color must be a color literal");
                    }
                }else {
                    ExpressionType expressionType = getExpressionType(expression);
                    if (expressionType != ExpressionType.COLOR) {
                        ((Declaration) node).property.setError("Color and background-color must be a color literal");
                    }
                }
            }
        }
    }

    private void checkVariable(ASTNode node){
        if(node instanceof VariableAssignment) {
            VariableAssignment variableAssignment = (VariableAssignment) node;
            if (variableAssignment.expression != null) {
                ExpressionType expressionType = getExpressionType(variableAssignment.expression);
                if(variableExistsWithOtherType(variableAssignment.name.name, expressionType)){
                    variableAssignment.setError("Variabele bestaat al met een andere type");
                }else{
                    variableTypes.getFirst().put(variableAssignment.name.name, expressionType);
                }
            }
        }
        if(node instanceof VariableReference) {
            VariableReference variableReference = (VariableReference) node;
            ExpressionType expressionType = getExpressionFromVariableReference(variableReference);
            if (expressionType == ExpressionType.UNDEFINED) {
                node.setError("Variabele bestaat niet");
            }
        }
    }

    private boolean variableExistsWithOtherType(String name, ExpressionType expressionType) {
        for (int i = 0; i < variableTypes.getSize(); i++){
            if(variableTypes.get(i).containsKey(name) && variableTypes.get(i).get(name) != expressionType) {
                return true;
            }else{
                return false;
            }
        }
        return false;
    }

    private ExpressionType getExpressionFromVariableReference(VariableReference variableReference){
        ExpressionType expressionType = ExpressionType.UNDEFINED;
        for (int i = 0; i < variableTypes.getSize(); i++){
            if(variableTypes.get(i).containsKey(variableReference.name)) {
                expressionType = variableTypes.get(i).get(variableReference.name);
                break;
            }
        }
        return expressionType;
    }

    private ExpressionType getExpressionType(Expression expression) {
        if(expression instanceof Literal) {
            return getExpressionTypeFromLiteral((Literal) expression);
        } else if (expression instanceof Operation) {
            return getOperationExpression((Operation) expression);
        } else if (expression instanceof VariableReference) {
            return getExpressionFromVariableReference((VariableReference) expression);
        }else{
            return ExpressionType.UNDEFINED;
        }
    }

    private ExpressionType getOperationExpression(Operation expression) {

        Operation operation = (Operation) expression;
        ExpressionType left = getExpressionType(operation.lhs);
        ExpressionType right = getExpressionType(operation.rhs);

        if(left.equals(ExpressionType.COLOR) || right.equals(ExpressionType.COLOR)){
            operation.setError("Een van de waarden van de operatie mag is een kleur, dit mag niet");
            return ExpressionType.UNDEFINED;
        }

        if(operation instanceof AddOperation || operation instanceof SubtractOperation) {
            if (left != right){
                operation.setError("waarden van plus of min som moet van het gelijke type zijn");
                return ExpressionType.UNDEFINED;
            }
        }

        if (operation instanceof MultiplyOperation){
            if(left != ExpressionType.SCALAR && right != ExpressionType.SCALAR){
                operation.setError("minimaal 1 waarde van keer som moet van het type scalar zijn");
                return ExpressionType.UNDEFINED;
            }else{
                if(left == ExpressionType.SCALAR){
                    return right;
                }else {
                    return left;
                }
            }
        }

        if (left == right) {
            return left;
        } else {
            return ExpressionType.UNDEFINED;
        }
    }

    private ExpressionType getExpressionTypeFromLiteral(Literal literal) {
        if (literal instanceof PixelLiteral) {
            return ExpressionType.PIXEL;
        } else if (literal instanceof ColorLiteral) {
            return ExpressionType.COLOR;
        } else if (literal instanceof PercentageLiteral) {
            return ExpressionType.PERCENTAGE;
        } else if (literal instanceof ScalarLiteral) {
            return ExpressionType.SCALAR;
        } else if (literal instanceof BoolLiteral) {
            return ExpressionType.BOOL;
        }else{
            return ExpressionType.UNDEFINED;
        }
    }

    private void checkIfStatement(ASTNode node) {
        if (node instanceof IfClause) {
            IfClause ifClause = (IfClause) node;

            ExpressionType expressionType;
            if(ifClause.conditionalExpression instanceof VariableReference){
                VariableReference variableReference = (VariableReference) ifClause.conditionalExpression;
                expressionType = getExpressionFromVariableReference(variableReference);
            }else{
                expressionType = getExpressionType(ifClause.conditionalExpression);
            }

            if (expressionType != ExpressionType.BOOL) {
                ifClause.conditionalExpression.setError("If statement expression must be a boolean");
            }
        }
    }

}

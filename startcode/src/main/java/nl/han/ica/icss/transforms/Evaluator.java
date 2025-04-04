package nl.han.ica.icss.transforms;

import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.linkedList.HANLinkedList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Evaluator implements Transform {
    private IHANLinkedList<HashMap<String, Literal>> variableValues;

    public Evaluator() {
        variableValues = new HANLinkedList<>();
    }

    @Override
    public void apply(AST ast) {
        variableValues = new HANLinkedList<>();
        setStylesheet(ast.root);
    }

    private void setStylesheet(Stylesheet stylesheet) {
        variableValues.addFirst(new HashMap<>());
        List<ASTNode> removeNodes = new ArrayList<>();

        for (ASTNode nodeInStylesheet : stylesheet.getChildren()) {
            if (nodeInStylesheet instanceof VariableAssignment) {
                setVariableValue(nodeInStylesheet);
                removeNodes.add(nodeInStylesheet);
            }
            if (nodeInStylesheet instanceof Stylerule) {
                setStyleruleValue((Stylerule) nodeInStylesheet);
            }
        }

        for (ASTNode removeNode : removeNodes) {
            stylesheet.removeChild(removeNode);
        }

        variableValues.removeFirst();
    }

    private void setStyleruleValue(Stylerule stylerule) {
        variableValues.addFirst(new HashMap<>());

        ArrayList<ASTNode> newBody = new ArrayList<>();

        for (ASTNode node : stylerule.body) {
            if (node instanceof VariableAssignment) {
                setVariableValue(node);
            } else if (node instanceof Declaration) {
                setDeclarationValue(node);
                newBody.add(node);
            } else if (node instanceof IfClause) {
                newBody.addAll(setIfClause(node));
            } else {
                newBody.add(node);
            }
        }

        stylerule.body = newBody;
        variableValues.removeFirst();
    }

    private void setDeclarationValue(ASTNode nodeInStylerule) {
        Declaration declaration = (Declaration) nodeInStylerule;
        declaration.expression = getResultFromExpression(declaration.expression);
    }

    private void setVariableValue(ASTNode nodeInStylesheet) {
        VariableAssignment variableAssignment = (VariableAssignment) nodeInStylesheet;
        Literal variableValue = getResultFromExpression(variableAssignment.expression);
        variableValues.getFirst().put(variableAssignment.name.name, variableValue);
    }

    private List<ASTNode> setIfClause(ASTNode nodeInStylesheet) {
        IfClause ifClause = (IfClause) nodeInStylesheet;
        boolean condition = evaluateCondition(ifClause.conditionalExpression);
        List<ASTNode> resultNodes = new ArrayList<>();

        if (condition) {
            Stylerule tempStylerule = new Stylerule();
            tempStylerule.body = ifClause.body;
            setStyleruleValue(tempStylerule);
            resultNodes.addAll(tempStylerule.body);
        } else if (ifClause.elseClause != null) {
            Stylerule tempStylerule = new Stylerule();
            tempStylerule.body = ifClause.elseClause.body;
            setStyleruleValue(tempStylerule);
            resultNodes.addAll(tempStylerule.body);
        }

        return resultNodes;
    }

    private boolean evaluateCondition(Expression conditionalExpression) {
        Literal result = getResultFromExpression(conditionalExpression);
        return result instanceof BoolLiteral && ((BoolLiteral) result).value;
    }

    private Literal getResultFromExpression(Expression expression){
        if (expression instanceof Literal) {
            return (Literal) expression;
        } else if (expression instanceof MultiplyOperation) {
            return getValueFromMultiplyOperation((MultiplyOperation) expression);
        } else if (expression instanceof AddOperation) {
            return getValueFromAddOperation((AddOperation) expression);
        } else if (expression instanceof SubtractOperation) {
            return getValueFromSubtractOperation((SubtractOperation) expression);
        } else if (expression instanceof VariableReference) {
            return getVariableValueFromVariableReference((VariableReference) expression);
        }
        return null;
    }

    private Literal getValueFromMultiplyOperation(MultiplyOperation multiplyOperation) {
        Literal left = getResultFromExpression(multiplyOperation.lhs);
        Literal right = getResultFromExpression(multiplyOperation.rhs);

        if (left instanceof ScalarLiteral && right instanceof ScalarLiteral) {
            return new ScalarLiteral(getValueFromLiteral(left) * getValueFromLiteral(right));
        }else if (left instanceof ScalarLiteral && right instanceof PixelLiteral) {
            return new PixelLiteral(getValueFromLiteral(left) * getValueFromLiteral(right));
        } else if (left instanceof ScalarLiteral && right instanceof PercentageLiteral) {
            return new PercentageLiteral(getValueFromLiteral(left) * getValueFromLiteral(right));
        } else if (left instanceof PixelLiteral && right instanceof ScalarLiteral) {
            return new PixelLiteral(getValueFromLiteral(left) * getValueFromLiteral(right));
        } else if (left instanceof PercentageLiteral && right instanceof ScalarLiteral) {
            return new PercentageLiteral(getValueFromLiteral(left) * getValueFromLiteral(right));
        }

        return null;
    }

    private Literal getValueFromAddOperation(AddOperation operation) {
        Literal left = getResultFromExpression(operation.lhs);
        Literal right = getResultFromExpression(operation.rhs);
        if (left instanceof PixelLiteral && right instanceof PixelLiteral) {
            return new PixelLiteral(getValueFromLiteral(left) + getValueFromLiteral(right));
        } else if (left instanceof PercentageLiteral && right instanceof PercentageLiteral) {
            return new PercentageLiteral(getValueFromLiteral(left) + getValueFromLiteral(right));
        }else if (left instanceof ScalarLiteral && right instanceof ScalarLiteral){
            return new ScalarLiteral(getValueFromLiteral(left) + getValueFromLiteral(right));
        }
        return null;
    }

    private Literal getValueFromSubtractOperation(SubtractOperation operation) {
        Literal left = getResultFromExpression(operation.lhs);
        Literal right = getResultFromExpression(operation.rhs);
        if (left instanceof PixelLiteral && right instanceof PixelLiteral) {
            return new PixelLiteral(getValueFromLiteral(left) - getValueFromLiteral(right));
        } else if (left instanceof PercentageLiteral && right instanceof PercentageLiteral) {
            return new PercentageLiteral(getValueFromLiteral(left) - getValueFromLiteral(right));
        }else if (left instanceof ScalarLiteral && right instanceof ScalarLiteral){
            return new ScalarLiteral(getValueFromLiteral(left) - getValueFromLiteral(right));
        }
        return null;
    }

    private int getValueFromLiteral(Literal literal) {
        if (literal instanceof PixelLiteral) {
            return ((PixelLiteral) literal).value;
        } else if (literal instanceof PercentageLiteral) {
            return ((PercentageLiteral) literal).value;
        } else if (literal instanceof ScalarLiteral) {
            return ((ScalarLiteral) literal).value;
        }
        return 0;
    }

    private Literal getVariableValueFromVariableReference(VariableReference variableReference) {
        for (HashMap<String, Literal> variable : variableValues) {
            if (variable.containsKey(variableReference.name)) {
                return variable.get(variableReference.name);
            }
        }
        return null;
    }
}

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

    private void setStylesheet(Stylesheet node) {
        variableValues.addFirst(new HashMap<>());
        List<ASTNode> removalNodes = new ArrayList<>();

        for (ASTNode astNode : node.getChildren()) {
            if (astNode instanceof VariableAssignment) {
                setVariableValue(astNode);
                removalNodes.add(astNode);
            }
            if (astNode instanceof Stylerule) {
                setStyleruleValue((Stylerule) astNode);
            }
        }

        for (ASTNode removeNode : removalNodes) {
            node.removeChild(removeNode);
        }

        variableValues.removeFirst();
    }

    private void setStyleruleValue(Stylerule node) {
        variableValues.addFirst(new HashMap<>());

        ArrayList<ASTNode> newBody = new ArrayList<>();

        for (ASTNode astNode : node.body) {
            if (astNode instanceof VariableAssignment) {
                setVariableValue(astNode);
            } else if (astNode instanceof Declaration) {
                setDeclarationValue(astNode);
                newBody.add(astNode);
            } else if (astNode instanceof IfClause) {
                newBody.addAll(setIfClause(astNode));
            } else {
                newBody.add(astNode);
            }
        }

        node.body = newBody;
        variableValues.removeFirst();
    }

    private void setDeclarationValue(ASTNode node) {
        Declaration declaration = (Declaration) node;
        declaration.expression = getResultExpression(declaration.expression);
    }

    private void setVariableValue(ASTNode node) {
        VariableAssignment variableAssignment = (VariableAssignment) node;
        Literal variableValue = getResultExpression(variableAssignment.expression);
        variableValues.getFirst().put(variableAssignment.name.name, variableValue);
    }

    private List<ASTNode> setIfClause(ASTNode node) {
        IfClause ifClause = (IfClause) node;
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

    private boolean evaluateCondition(Expression expression) {
        Literal result = getResultExpression(expression);
        return result instanceof BoolLiteral && ((BoolLiteral) result).value;
    }

    private Literal getResultExpression(Expression expression){
        if (expression instanceof Literal) {
            return (Literal) expression;
        } else if (expression instanceof MultiplyOperation) {
            return getValueMultiplyOperation((MultiplyOperation) expression);
        } else if (expression instanceof AddOperation) {
            return getValueAddOperation((AddOperation) expression);
        } else if (expression instanceof SubtractOperation) {
            return getValueSubtractOperation((SubtractOperation) expression);
        } else if (expression instanceof VariableReference) {
            return getVariableValueVariableReference((VariableReference) expression);
        }
        return null;
    }

    private Literal getValueMultiplyOperation(MultiplyOperation operation) {
        Literal left = getResultExpression(operation.lhs);
        Literal right = getResultExpression(operation.rhs);

        if (left instanceof ScalarLiteral && right instanceof ScalarLiteral) {
            return new ScalarLiteral(getValueLiteral(left) * getValueLiteral(right));
        }else if (left instanceof ScalarLiteral && right instanceof PixelLiteral) {
            return new PixelLiteral(getValueLiteral(left) * getValueLiteral(right));
        } else if (left instanceof ScalarLiteral && right instanceof PercentageLiteral) {
            return new PercentageLiteral(getValueLiteral(left) * getValueLiteral(right));
        } else if (left instanceof PixelLiteral && right instanceof ScalarLiteral) {
            return new PixelLiteral(getValueLiteral(left) * getValueLiteral(right));
        } else if (left instanceof PercentageLiteral && right instanceof ScalarLiteral) {
            return new PercentageLiteral(getValueLiteral(left) * getValueLiteral(right));
        }

        return null;
    }

    private Literal getValueAddOperation(AddOperation operation) {
        Literal left = getResultExpression(operation.lhs);
        Literal right = getResultExpression(operation.rhs);
        if (left instanceof PixelLiteral && right instanceof PixelLiteral) {
            return new PixelLiteral(getValueLiteral(left) + getValueLiteral(right));
        } else if (left instanceof PercentageLiteral && right instanceof PercentageLiteral) {
            return new PercentageLiteral(getValueLiteral(left) + getValueLiteral(right));
        }else if (left instanceof ScalarLiteral && right instanceof ScalarLiteral){
            return new ScalarLiteral(getValueLiteral(left) + getValueLiteral(right));
        }
        return null;
    }

    private Literal getValueSubtractOperation(SubtractOperation operation) {
        Literal left = getResultExpression(operation.lhs);
        Literal right = getResultExpression(operation.rhs);
        if (left instanceof PixelLiteral && right instanceof PixelLiteral) {
            return new PixelLiteral(getValueLiteral(left) - getValueLiteral(right));
        } else if (left instanceof PercentageLiteral && right instanceof PercentageLiteral) {
            return new PercentageLiteral(getValueLiteral(left) - getValueLiteral(right));
        }else if (left instanceof ScalarLiteral && right instanceof ScalarLiteral){
            return new ScalarLiteral(getValueLiteral(left) - getValueLiteral(right));
        }
        return null;
    }

    private int getValueLiteral(Literal literal) {
        if (literal instanceof PixelLiteral) {
            return ((PixelLiteral) literal).value;
        } else if (literal instanceof PercentageLiteral) {
            return ((PercentageLiteral) literal).value;
        } else if (literal instanceof ScalarLiteral) {
            return ((ScalarLiteral) literal).value;
        }
        return 0;
    }

    private Literal getVariableValueVariableReference(VariableReference variableReference) {
        for (HashMap<String, Literal> variable : variableValues) {
            if (variable.containsKey(variableReference.name)) {
                return variable.get(variableReference.name);
            }
        }
        return null;
    }
}

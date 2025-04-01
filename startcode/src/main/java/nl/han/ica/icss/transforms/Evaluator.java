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

    private void setStylesheet(Stylesheet root) {
        variableValues.addFirst(new HashMap<>());
        List<ASTNode> removalnodes = new ArrayList<>();
        for (ASTNode nodWithinStylesheets : root.getChildren()) {
            if (nodWithinStylesheets instanceof VariableAssignment) {
                setVarValue(nodWithinStylesheets);
                removalnodes.add(nodWithinStylesheets);
            }
            if(nodWithinStylesheets instanceof Stylerule) {
                setStyleRuleVar((Stylerule) nodWithinStylesheets);
            }
        }

        for (ASTNode removalNode : removalnodes) {
            root.removeChild(removalNode);
        }
    }

    private void setStyleRuleVar(Stylerule styleNode) {
        variableValues.addFirst(new HashMap<>());
        ArrayList<ASTNode> newBody = new ArrayList<>();

        for (ASTNode node : styleNode.body) {
            if (node instanceof VariableAssignment) {
                setVarValue(node);
            } else if (node instanceof Declaration) {
                setDeclarationValue(node);
                newBody.add(node);
            } else if (node instanceof IfClause) {
                newBody.addAll(setIfClause(node));
            } else {
                newBody.add(node);
            }
        }
    }

    private List<ASTNode> setIfClause(ASTNode node) {
        IfClause ifClause = (IfClause) node;
        boolean condition = eveluateCondition(ifClause.conditionalExpression);
        List<ASTNode> resultNodes = new ArrayList<>();

        if (condition) {
            Stylerule stylerule = new Stylerule();
            stylerule.body = ifClause.body;
            setStyleRuleVar(stylerule);
            resultNodes.addAll(stylerule.body);
        } else if (ifClause.conditionalExpression != null) {
            Stylerule stylerule = new Stylerule();
            stylerule.body = ifClause.elseClause.body;
            setStyleRuleVar(stylerule);
            resultNodes.addAll(stylerule.body);
        }
        return resultNodes;
    }

    private boolean eveluateCondition(Expression conditionalExpression) {
        Literal literal = getExpressionResult(conditionalExpression);
        return literal instanceof BoolLiteral && ((BoolLiteral) literal).value;
    }

    private void setDeclarationValue(ASTNode node) {
        Declaration declaration = (Declaration) node;
        declaration.expression = getExpressionResult(declaration.expression);
    }

    private Literal getExpressionResult(Expression expression) {
        if (expression instanceof Literal) {
            return (Literal) expression;
        } else if (expression instanceof MultiplyOperation) {
            return getMultiplyOperationValue((MultiplyOperation) expression);
        } else if (expression instanceof AddOperation) {
            return getAddOperationValue((AddOperation) expression);
        } else if (expression instanceof SubtractOperation) {
            return getSubstractOperationValue((SubtractOperation) expression);
        } else if (expression instanceof VariableReference) {
            return getVariableReferenceValue((VariableReference) expression);
        }
        return null;
    }

    private Literal getVariableReferenceValue(VariableReference variableReference) {
        for (HashMap<String, Literal> variable : variableValues) {
            if (variable.containsKey(variableReference.name)) {
                return variable.get(variableReference.name);
            }
        }
        return null;
    }

    private Literal getSubstractOperationValue(SubtractOperation expression) {
        Literal left = getExpressionResult(expression.lhs);
        Literal right = getExpressionResult(expression.rhs);

        if (left instanceof PixelLiteral && right instanceof PixelLiteral) {
            return new PixelLiteral(getLiteralValue(left) - getLiteralValue(right));
        } else if (left instanceof PercentageLiteral && right instanceof  PercentageLiteral) {
            return new PercentageLiteral(getLiteralValue(left) - getLiteralValue(right));
        } else if (left instanceof ScalarLiteral && right instanceof ScalarLiteral) {
            return new ScalarLiteral(getLiteralValue(left) - getLiteralValue(right));
        }
        return null;
    }

    private Literal getAddOperationValue(AddOperation expression) {
        Literal left = getExpressionResult(expression.lhs);
        Literal right = getExpressionResult(expression.rhs);

        if(left instanceof ScalarLiteral && right instanceof ScalarLiteral) {
            return new ScalarLiteral(getLiteralValue(left) + getLiteralValue(right));
        } else if (left instanceof ScalarLiteral && right instanceof PixelLiteral) {
            return new PixelLiteral(getLiteralValue(left) + getLiteralValue(right));
        } else if (left instanceof ScalarLiteral && right instanceof PercentageLiteral) {
            return new PercentageLiteral(getLiteralValue(left) + getLiteralValue(right));
        } else if (left instanceof PixelLiteral && right instanceof  ScalarLiteral) {
            return  new PixelLiteral(getLiteralValue(left) + getLiteralValue(right));
        } else if (left instanceof PercentageLiteral && right instanceof ScalarLiteral) {
            return new PercentageLiteral(getLiteralValue(left) +  getLiteralValue(right));
        }
        return null;
    }

    private Literal getMultiplyOperationValue(MultiplyOperation expression) {
        Literal left = getExpressionResult(expression.lhs);
        Literal right = getExpressionResult(expression.rhs);

        if(left instanceof ScalarLiteral && right instanceof ScalarLiteral) {
            return new ScalarLiteral(getLiteralValue(left) * getLiteralValue(right));
        } else if (left instanceof ScalarLiteral && right instanceof PixelLiteral) {
            return new PixelLiteral(getLiteralValue(left) * getLiteralValue(right));
        } else if (left instanceof ScalarLiteral && right instanceof PercentageLiteral) {
            return new PercentageLiteral(getLiteralValue(left) * getLiteralValue(right));
        } else if (left instanceof PixelLiteral && right instanceof  ScalarLiteral) {
            return  new PixelLiteral(getLiteralValue(left) * getLiteralValue(right));
        } else if (left instanceof PercentageLiteral && right instanceof ScalarLiteral) {
            return new PercentageLiteral(getLiteralValue(left) * getLiteralValue(right));
        }
        return null;
    }

    private int getLiteralValue(Literal literal) {
        if (literal instanceof PixelLiteral) {
            return ((PixelLiteral) literal).value;
        } else if (literal instanceof PercentageLiteral) {
            return ((PercentageLiteral)literal).value;
        } else if (literal instanceof ScalarLiteral) {
            return ((ScalarLiteral)literal).value;
        }
        return 0;
    }

    private void setVarValue(ASTNode nodWithinStylesheets) {
        VariableAssignment variableAssignment = (VariableAssignment) nodWithinStylesheets;
        Literal literal = getExpressionResult(variableAssignment.expression);
        variableValues.getFirst().put(variableAssignment.name.name, literal);
    }


}

package nl.han.ica.icss.parser;

import java.util.Stack;


import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;
import nl.han.ica.linkedList.HANStack;

/**
 * This class extracts the ICSS Abstract Syntax Tree from the Antlr Parse tree.
 */
public class ASTListener extends nl.han.ica.icss.parser.ICSSBaseListener {
	
	//Accumulator attributes:
	private AST ast;

	//Use this to keep track of the parent nodes when recursively traversing the ast
	private HANStack<ASTNode> currentContainer;

	public ASTListener() {
		ast = new AST();
		currentContainer = new HANStack<>();
	}
    public AST getAST() {
        return ast;
    }

	@Override
	public void enterStylesheet(nl.han.ica.icss.parser.ICSSParser.StylesheetContext ctx) {
		Stylesheet stylesheet = new Stylesheet();
		ast.setRoot(stylesheet);
		currentContainer.push(stylesheet);
	}

	@Override
	public void exitStylesheet(nl.han.ica.icss.parser.ICSSParser.StylesheetContext ctx) {
		currentContainer.pop();
	}

	@Override
	public void enterDeclaration(nl.han.ica.icss.parser.ICSSParser.DeclarationContext ctx) {
		Declaration declaration = new Declaration();
		declaration.property = new PropertyName(ctx.getText());
		if (ctx.value().COLOR() != null) {
			declaration.expression = new ColorLiteral(ctx.value().COLOR().getText());
		} else if (ctx.value().PIXELSIZE() != null) {
			declaration.expression = new PixelLiteral(ctx.value().PIXELSIZE().getText());
		} else if (ctx.value().PERCENTAGE() != null) {
			declaration.expression = new PercentageLiteral(ctx.value().PERCENTAGE().getText());
		} else if (ctx.value().SCALAR() != null) {
			declaration.expression = new ScalarLiteral(ctx.value().SCALAR().getText());
		} else if (ctx.value().TRUE() != null || ctx.value().FALSE() != null) {
			declaration.expression = new BoolLiteral(ctx.value().getText());
		} else if (ctx.value().CAPITAL_IDENT() != null) {
			declaration.expression = new VariableReference(ctx.value().CAPITAL_IDENT().getText());
		}

		// Attach to parent node
		if (!currentContainer.isEmpty()) {
			((Stylesheet) currentContainer.peek()).body.add(declaration);
		}
	}

}
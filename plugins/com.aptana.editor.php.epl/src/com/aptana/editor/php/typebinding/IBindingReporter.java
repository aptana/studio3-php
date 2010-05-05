package com.aptana.editor.php.typebinding;

import org.eclipse.php.internal.core.ast.nodes.ASTNode;

public interface IBindingReporter {

	void report(ASTNode node,IBinding binding);
}

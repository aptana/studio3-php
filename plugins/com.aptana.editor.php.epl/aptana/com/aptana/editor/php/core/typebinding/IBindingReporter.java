package com.aptana.editor.php.core.typebinding;

import org2.eclipse.php.internal.core.ast.nodes.ASTNode;

public interface IBindingReporter {

	void report(ASTNode node,IBinding binding);
}

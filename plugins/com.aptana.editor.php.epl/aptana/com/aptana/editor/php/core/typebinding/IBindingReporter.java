package com.aptana.editor.php.core.typebinding;

import org.eclipse.php.internal.core.ast.nodes.ASTNode;
import org.eclipse.php.internal.core.ast.nodes.IBinding;

public interface IBindingReporter {

	void report(ASTNode node,IBinding binding);
}

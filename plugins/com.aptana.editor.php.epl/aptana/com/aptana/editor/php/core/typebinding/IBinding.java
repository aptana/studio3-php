package com.aptana.editor.php.core.typebinding;

import com.aptana.editor.php.core.model.IModelElement;

public interface IBinding extends org.eclipse.php.internal.core.ast.nodes.IBinding{

	String getName();

	IModelElement getPHPElement();

}

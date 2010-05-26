package com.aptana.editor.php.core.typebinding;

import org.eclipse.php.internal.core.ast.nodes.IBinding;

import com.aptana.editor.php.core.model.IType;

public interface ITypeBinding extends IBinding{

	boolean isClass();

	IType getPHPElement();

}

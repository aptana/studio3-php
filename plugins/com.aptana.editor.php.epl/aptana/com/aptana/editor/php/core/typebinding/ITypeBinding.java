package com.aptana.editor.php.core.typebinding;

import com.aptana.editor.php.core.model.IType;

public interface ITypeBinding extends IBinding
{
	boolean isClass();
	IType getPHPElement();
}

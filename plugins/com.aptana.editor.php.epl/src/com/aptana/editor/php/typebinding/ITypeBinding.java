package com.aptana.editor.php.typebinding;

import com.aptana.editor.php.model.IType;

public interface ITypeBinding extends IBinding{

	boolean isClass();

	IType getPHPElement();

}

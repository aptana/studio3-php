package com.aptana.editor.php.core.typebinding;

import com.aptana.editor.php.core.model.IType;

public class TypeBinding implements ITypeBinding {

	private IType type;
	
	public TypeBinding(IType next) {
		this.type=next;
	}

	public IType getPHPElement() {
		return type;
	}

	public boolean isClass() {
		return !type.isInterface();
	}

	public String getName() {
		return type.getElementName();
	}


}

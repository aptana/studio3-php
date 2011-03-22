package com.aptana.editor.php.internal.core.typebinding;

import java.util.List;

import org2.eclipse.php.internal.core.ast.nodes.IBinding;

import com.aptana.editor.php.core.model.IMethod;
import com.aptana.editor.php.core.model.IModelElement;
import com.aptana.editor.php.core.model.ISourceModule;
import com.aptana.editor.php.core.model.IType;
import com.aptana.editor.php.core.typebinding.IMethodBinding;

public class MethodBinding implements IMethodBinding{

	ISourceModule module;
	String className;
	String methodName;
	int modifiers;

	public MethodBinding(String className, String methodName, int modifiers,
			ISourceModule module) {
		super();
		this.className = className;
		this.methodName = methodName;
		this.modifiers = modifiers;
		this.module = module;
	}
	
	public int getModifiers() {
		return modifiers;
	}
	public String getName() {
		return methodName;
	}
	public IModelElement getPHPElement() {
		IType type = module.getType(className);
		if (type!=null){
			List<IMethod> methods = type.getMethods(methodName);
			for (IMethod m:methods){
				return m;
			}
		}
		return null;
	}

	public String getKey()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public int getKind()
	{
		return IBinding.METHOD;
	}

	public boolean isDeprecated()
	{
		// TODO Auto-generated method stub
		return false;
	}
}

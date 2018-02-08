package com.aptana.editor.php.internal.core.typebinding;

import java.util.HashSet;
import java.util.List;

import com.aptana.editor.php.core.model.IMethod;
import com.aptana.editor.php.core.model.ISourceModule;
import com.aptana.editor.php.core.model.IType;
import com.aptana.editor.php.core.typebinding.IMethodBinding;
import com.aptana.editor.php.core.typebinding.ITypeBinding;

public class Bindings {

	public static IMethodBinding findOverriddenMethodInHierarchy(
			ITypeBinding binding, IMethodBinding methodBinding) {
		String name = methodBinding.getName();
		IType element = binding.getPHPElement();
		HashSet<IType> ts = new HashSet<IType>();
		IMethodBinding m = findMethod(element, ts, name);
		return m;
	}

	private static IMethodBinding findMethod(IType element, HashSet<IType> ts,
			String name) {
		if (!ts.contains(element)) {
			ts.add(element);
			
			List<IMethod> methods = element.getMethods(name);
			if (methods != null && !methods.isEmpty()) {
				IMethod next = methods.iterator().next();
				ISourceModule sourceModule = element.getSourceModule();
				return new MethodBinding(element.getElementName(), name, next
						.getModifiers(), sourceModule);
			}
			List<IType> superClasses = element.getSuperClasses();
			if (superClasses != null)
			{
				for (IType t : superClasses)
				{
					IMethodBinding findMethod = findMethod(t, ts, name);
					if (findMethod != null)
					{
						return findMethod;
					}
				}
			}
			for (IType t:element.getInterfaces()){
				IMethodBinding findMethod = findMethod(t, ts, name);
				if (findMethod!=null){
					return findMethod;
				}
			}
		}
		return null;
	}

}

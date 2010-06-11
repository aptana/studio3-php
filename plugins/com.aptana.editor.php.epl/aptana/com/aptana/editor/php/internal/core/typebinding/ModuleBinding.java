package com.aptana.editor.php.internal.core.typebinding;

import org.eclipse.php.internal.core.ast.nodes.IBinding;

import com.aptana.editor.php.core.model.IModelElement;
import com.aptana.editor.php.core.model.ISourceModule;

public class ModuleBinding implements IBinding
{

	private ISourceModule module;

	public ModuleBinding(ISourceModule module)
	{
		this.module = module;
	}

	public String getName()
	{
		return module.getPath();
	}

	public IModelElement getPHPElement()
	{
		return module;
	}

	@Override
	public String getKey()
	{
		return null;
	}

	@Override
	public int getKind()
	{
		return IBinding.SOURCE;
	}

	@Override
	public int getModifiers()
	{
		return 0;
	}

	@Override
	public boolean isDeprecated()
	{
		return false;
	}
}

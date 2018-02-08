package com.aptana.editor.php.internal.core.typebinding;

import com.aptana.editor.php.core.model.IModelElement;
import com.aptana.editor.php.core.model.ISourceModule;
import com.aptana.editor.php.core.typebinding.IBinding;

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

	public String getKey()
	{
		return null;
	}

	public int getKind()
	{
		return IBinding.SOURCE;
	}

	public int getModifiers()
	{
		return 0;
	}

	public boolean isDeprecated()
	{
		return false;
	}
}

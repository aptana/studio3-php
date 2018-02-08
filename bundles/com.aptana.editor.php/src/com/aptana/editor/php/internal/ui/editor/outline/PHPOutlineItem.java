package com.aptana.editor.php.internal.ui.editor.outline;

import com.aptana.editor.common.outline.CommonOutlineItem;
import com.aptana.parsing.ast.ILanguageNode;
import com.aptana.parsing.ast.IParseNode;
import com.aptana.parsing.lexer.IRange;

public class PHPOutlineItem extends CommonOutlineItem implements ILanguageNode
{
	public PHPOutlineItem(IRange sourceRange, IParseNode referenceNode)
	{
		super(sourceRange, referenceNode);
	}

	@Override
	public String getLanguage()
	{
		return getReferenceNode().getLanguage();
	}
}

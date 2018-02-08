package com.aptana.editor.php.internal.contentAssist;

/**
 * Proposal context.
 * 
 * @author Denis Denisenko
 */
class ProposalContext
{
	private static final int[] EMPTY_TYPES = new int[0];

	/**
	 * Context filter.
	 */
	private IContextFilter contextFilter;

	/**
	 * Whether filter accepts built-ins.
	 */
	private boolean acceptsBuiltins;

	/**
	 * Whether filter accepts model elements.
	 */
	private boolean acceptsModelElements;

	/**
	 * Accepted types.
	 */
	private int[] types;

	/**
	 * Proposal context type.
	 */
	private String type;

	/**
	 * Whether to auto-activate content assist after proposals apply.
	 */
	private boolean autoActivateCAAfterApply = false;

	/**
	 * ProposalContext constructor.
	 * 
	 * @param filter
	 *            - filter.
	 * @param acceptBuiltins
	 *            - whether to accept built-ins.
	 * @param acceptModelElements
	 *            - whether to accept model elements.
	 * @param types
	 *            - types of model elements to accept, null means all types are accepted (no type check).
	 */
	protected ProposalContext(IContextFilter filter, boolean acceptBuiltins, boolean acceptModelElements, int[] types)
	{
		this.types = (types == null) ? EMPTY_TYPES : types;
		this.contextFilter = filter;
		this.acceptsBuiltins = acceptBuiltins;
		this.acceptsModelElements = acceptModelElements;
	}

	/**
	 * Gets context filter.
	 * 
	 * @return context filter or null.
	 */
	public IContextFilter getContextFilter()
	{
		return contextFilter;
	}

	/**
	 * Whether context accepts built-ins. Hint only, filter MUST filter accordingly.
	 * 
	 * @return whether context accepts built-ins.
	 */
	public boolean acceptBuiltins()
	{
		return acceptsBuiltins;
	}

	/**
	 * Whether context accepts model elements. Hint only, filter MUST filter accordingly.
	 * 
	 * @return whether context model elements.
	 */
	public boolean acceptModelsElements()
	{
		return acceptsModelElements;
	}

	/**
	 * Whether the context allows us to show external proposals from Rubles/Snippets.
	 */
	public boolean acceptExternalProposals()
	{
		// delegate this to the filter
		return contextFilter.acceptExternalProposals();
	}

	/**
	 * Whether certain model element type is accepted. Hint only, filter MUST filter accordingly.
	 * 
	 * @param type
	 *            - type to check.
	 * @return true if accepted, false otherwise.
	 */
	public boolean acceptModelElementType(int type)
	{
		if (!acceptsModelElements)
		{
			return false;
		}

		if (types == null || types.length == 0)
		{
			return true;
		}

		for (int i = 0; i < types.length; i++)
		{
			if (types[i] == type)
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Gets model element types accepted.
	 * 
	 * @return model element types accepted.
	 */
	public int[] getModelTypesAccepted()
	{
		return types;
	}

	/**
	 * Gets whether to auto-activate content assist after proposals are applied.
	 * 
	 * @return whether to auto-activate content assist after proposals are applied.
	 */
	public boolean isAutoActivateCAAfterApply()
	{
		return autoActivateCAAfterApply;
	}

	/**
	 * Sets whether to auto-activate content assist after proposals are applied.
	 * 
	 * @param autoActivateCAAfterApply
	 *            - whether to auto-activate content assist after proposals are applied.
	 */
	public void setAutoActivateCAAfterApply(boolean autoActivateCAAfterApply)
	{
		this.autoActivateCAAfterApply = autoActivateCAAfterApply;
	}

	/**
	 * Gets type.
	 * 
	 * @return type.
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * Sets type.
	 * 
	 * @param type
	 *            - type to set.
	 */
	public void setType(String type)
	{
		this.type = type;
	}
}
